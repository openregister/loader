package uk.gov.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("ConstantConditions")
public class DataFileReaderTest {
    private List<Map> expectedData;

    Path testFilePath;

    @Before
    public void setUp() throws Exception {
        final String testData = "{\"address\":\"0000001\",\"postcode\":\"01010101\"}";
        expectedData = Collections.singletonList(new ObjectMapper().readValue(testData, Map.class));

        testFilePath = Files.createTempFile("test-load", "");
    }

    @After
    public void cleanup() throws IOException {
        Files.deleteIfExists(testFilePath);
    }

    @Test
    public void should_be_able_to_read_local_file_contains_json_entries() throws IOException, URISyntaxException {
        Files.write(testFilePath, "{\"address\":\"0000001\",\"postcode\":\"01010101\"}".getBytes());

        Iterator<Map> entriesIterator = new DataFileReader(testFilePath.toString(), "jsonl").getFileEntriesIterator();

        List<Map> response = mapFrom(entriesIterator);

        assertEquals(expectedData, response);
    }

    @Test
    public void should_be_able_to_read_local_file_contains_csv_entries() throws IOException, URISyntaxException {

        Files.write(testFilePath, "address,postcode\n0000001,01010101".getBytes());

        Iterator<Map> entriesIterator = new DataFileReader(testFilePath.toString(), "csv").getFileEntriesIterator();

        assertEquals(expectedData, mapFrom(entriesIterator));
    }

    @Test
    public void should_be_able_to_read_local_file_contains_tsv_entries() throws IOException, URISyntaxException {
        Files.write(testFilePath, "address\tpostcode\n0000001\t01010101".getBytes());

        Iterator<Map> entriesIterator = new DataFileReader(testFilePath.toString(), "tsv").getFileEntriesIterator();

        assertEquals(expectedData, mapFrom(entriesIterator));
    }


    protected List<Map> mapFrom(Iterator<Map> entriesIterator) {
        List<Map> data = new ArrayList<>();

        while (entriesIterator.hasNext()) {
            data.add(entriesIterator.next());
        }
        return data;
    }
}


