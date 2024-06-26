package com.github.davidcarboni.httpino;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.JsonSyntaxException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.channels.OverlappingFileLockException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class Serialiser {

    private static GsonBuilder builder;
    public static boolean debug = false;

    /**
     * Serialises the given object to Json.
     *
     * @param object The object be serialised.
     * @return The Json as a String.
     */
    public static String serialise(Object object) {
        Gson gson = getBuilder().create();
        return gson.toJson(object);
    }

    /**
     * Deserialises the given json String.
     *
     * @param json The Json to deserialise.
     * @param type The type to deserialise into.
     * @param <O>  The type to deserialise to.
     * @return A new instance of the given type.
     */
    public static <O> O deserialise(String json, Class<O> type) {
        Gson gson = getBuilder().create();
        return gson.fromJson(json, type);
    }

    /**
     * Serialises the given object to Json and writes it to the given
     * {@link OutputStream}.
     *
     * @param output          The output stream to serialise to.
     * @param responseMessage The message to be serialised.
     * @throws IOException If an error occurs in writing the output.
     */
    public static void serialise(OutputStream output, Object responseMessage)
            throws IOException {

        Gson gson = getBuilder().create();
        try (OutputStreamWriter writer = new OutputStreamWriter(output, StandardCharsets.UTF_8)) {
            gson.toJson(responseMessage, writer);
        }
    }

    /**
     * Deserialises the given {@link InputStream} to a JSON String.
     *
     * @param input              The stream to deserialise.
     * @param requestMessageType The message type to deserialise into.
     * @param <O>                The type to deserialise to.
     * @return A new instance of the given type.
     * @throws IOException If an error occurs in reading from the input stream.
     */
    public static <O> O deserialise(InputStream input,
                                    Class<O> requestMessageType) throws IOException {

        Gson gson = getBuilder().create();
        try (Reader reader = DebugReader.newInstance(input)) {
            return gson.fromJson(reader, requestMessageType);
        }
    }

    /**
     * Deserialises the given {@link InputStream} to a JSON String.
     *
     * @param input The stream to deserialise.
     * @param type  The message type to deserialise into.
     * @param <O>   The type to deserialise to.
     * @return A new instance of the given type.
     * @throws IOException If an error occurs in reading from the input stream.
     */
    public static <O> O deserialise(InputStream input,
                                    Type type) throws IOException {

        Gson gson = getBuilder().create();
        try (Reader reader = DebugReader.newInstance(input)) {
            return gson.fromJson(reader, type);
        }
    }

    /**
     * Serialises the given object to Json and writes it to the given
     * {@link Path}. This method will acquire a filesystem lock on the
     * given path in order to avoid corruption in the event that multple
     * threads attempt to write to the same file at the same time.
     *
     * @param output The Path to serialise to.
     * @param json   The Json to be serialised.
     * @throws IOException If an error occurs in writing the output.
     */
    public static void serialise(Path output, Object json) throws IOException {

        // First serialise to a temp file:
        Gson gson = getBuilder().create();
        Path temp = Files.createTempFile(json.getClass().getSimpleName(), ".json");
        try (Writer writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
            gson.toJson(json, writer);
        }

        // Now do an optimised Channel-to-Channel transfer to the output file:
        long size = Files.size(temp);
        try (FileChannel tempChannel = FileChannel.open(temp, StandardOpenOption.READ);
             FileChannel outputChannel = FileChannel.open(output, StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
            // NB the lock will be released when the channel is closed:
            writeLock(outputChannel);
            outputChannel.truncate(0);
            tempChannel.transferTo(0, size, outputChannel);
            outputChannel.truncate(size);
        }
    }

    /**
     * Deserialises the given {@link InputStream} to a JSON String.
     *
     * @param input    The stream to deserialise.
     * @param jsonType The object type to deserialise into.
     * @param <O>      The type to deserialise to.
     * @return A new instance of the given type.
     * @throws IOException If an error occurs in reading from the input stream.
     */
    public static <O> O deserialise(Path input, Class<O> jsonType) throws IOException {
        return deserialise(input, jsonType, 0);
    }

    /**
     * Deserialises the given {@link InputStream} to a JSON String.
     *
     * @param input    The stream to deserialise.
     * @param jsonType The object type to deserialise into.
     * @param attempt  The retry attempt. This has an arbitrary maximum of 5.
     * @param <O>      The type to deserialise to.
     * @return A new instance of the given type.
     * @throws IOException If an error occurs in reading from the input stream.
     */
    public static <O> O deserialise(Path input, Class<O> jsonType, int attempt) throws IOException {
        O result = null;
        //if (attempt > 0)
        //    System.out.println("Retrying deserialisation.. (" + attempt + ")");

        Gson gson = getBuilder().create();
        try (FileChannel inputChannel = FileChannel.open(input, StandardOpenOption.READ)) {
            // NB the lock will be released when the channel is closed:
            readLock(inputChannel);
            try (Reader reader = new BufferedReader(Channels.newReader(inputChannel, StandardCharsets.UTF_8.name()))) {
                result = gson.fromJson(reader, jsonType);
            } catch (JsonSyntaxException | JsonIOException | NumberFormatException e) {
                // Very occasionally, with 1000 threads, the content comes back invalid, so we'll retry.
                if (attempt >= 5) {
                    throw e;
                }
            }
        }

        // Very occasionally, with 1000 threads, no content is read so we'll retry if the result is null.
        if (result == null && attempt < 5) {
            result = deserialise(input, jsonType, ++attempt);
        }

        return result;
    }

    /**
     * Serialises the given object to Json and writes it to the given
     * {@link HttpServletResponse}.
     *
     * @param response        The http response to serialise to.
     * @param responseMessage The message to be serialised.
     * @throws IOException If an error occurs in writing the output.
     */
    public static void serialise(HttpServletResponse response,
                                 Object responseMessage) throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        serialise(response.getOutputStream(), responseMessage);
    }

    /**
     * Deserialises the given {@link HttpServletRequest} to a JSON String.
     *
     * @param request            The request to deserialise.
     * @param requestMessageType The message type to deserialise into.
     * @param <O>                The type to deserialise to.
     * @return A new instance of the given type.
     * @throws IOException If an error occurs in reading from the request input stream.
     */
    public static <O> O deserialise(HttpServletRequest request,
                                    Class<O> requestMessageType) throws IOException {

        return deserialise(request.getInputStream(), requestMessageType);
    }

    /**
     * @return A lazily instantiated and cached {@link GsonBuilder}.
     */
    public static GsonBuilder getBuilder() {
        if (builder == null) {
            // Set the date pattern to be compatible with Javascript:
            builder = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        }
        return builder;
    }

    private static FileLock readLock(FileChannel channel) throws IOException {

        // Be lenient in getting a lock:
        FileLock lock = null;
        do {
            try {
                // Get a shared lock for reading:
                lock = channel.tryLock(0L, Long.MAX_VALUE, true);
            } catch (OverlappingFileLockException e) {
                pause();
            }
        } while (lock == null);

        return lock;
    }

    private static FileLock writeLock(FileChannel channel) throws IOException {

        // Be lenient in getting a lock:
        FileLock lock = null;
        do {
            try {
                // Get an exclusive lock for writing
                lock = channel.tryLock();
            } catch (OverlappingFileLockException e) {
                pause();
            }
        } while (lock == null);

        return lock;
    }

    private static void pause() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e1) {
            // Meh.
        }
    }

    private static class DebugReader extends Reader {

        Reader reader;

        public static Reader newInstance(InputStream input) {
            if (debug)
                return new DebugReader(input);
            else
                return new InputStreamReader(input, StandardCharsets.UTF_8);
        }

        DebugReader(InputStream input) {
            reader = new InputStreamReader(input, StandardCharsets.UTF_8);
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            int read = reader.read(cbuf, off, len);
            if (debug && read != -1) {
                System.out.print(String.valueOf(cbuf, off, read));
            }
            return read;
        }

        @Override
        public void close() throws IOException {
            reader.close();
        }
    }
}
