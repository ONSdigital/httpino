package com.github.davidcarboni.httpino;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

public class SerialiserTest {

    private static class Sample {
        String name;
        int count;

        Sample(String name, int count) {
            this.name = name;
            this.count = count;
        }
    }

    @Test
    public void serialisesAndDeserialisesString() {
        Sample sample = new Sample("alpha", 3);
        String json = Serialiser.serialise(sample);

        Sample result = Serialiser.deserialise(json, Sample.class);

        assertEquals("alpha", result.name);
        assertEquals(3, result.count);
    }

    @Test
    public void serialisesAndDeserialisesStreams() throws Exception {
        Sample sample = new Sample("bravo", 7);
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        Serialiser.serialise(output, sample);

        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
        Sample result = Serialiser.deserialise(input, Sample.class);

        assertEquals("bravo", result.name);
        assertEquals(7, result.count);
    }

    @Test
    public void serialisesAndDeserialisesPaths() throws Exception {
        Sample sample = new Sample("charlie", 11);
        Path temp = Files.createTempFile("httpino", ".json");

        Serialiser.serialise(temp, sample);
        Sample result = Serialiser.deserialise(temp, Sample.class);

        assertEquals("charlie", result.name);
        assertEquals(11, result.count);

        Files.deleteIfExists(temp);
    }

    @Test
    public void deserialisesInputStreamWithUtf8() throws Exception {
        String json = "{\"name\":\"delta\",\"count\":5}";
        ByteArrayInputStream input = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));

        Sample result = Serialiser.deserialise(input, Sample.class);

        assertEquals("delta", result.name);
        assertEquals(5, result.count);
    }
}
