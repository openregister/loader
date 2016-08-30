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

    public static final Optional<String> FIELDS_JSON = Optional.of("file:src/test/resources/fields.json");
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

    @Test
    public void should_read_cardinality_n_field_as_array() throws Exception {

        Iterator<Map> entriesIterator = new DataFileReader("src/test/resources/tsv-semi-colon.tsv", "tsv", FIELDS_JSON).getFileEntriesIterator();

        String json0 = new ObjectMapper().writeValueAsString(entriesIterator.next());

        assertEquals("{\"food-premises\":\"123\",\"food-premises-types\":[\"Restaurant\",\"Cafe\",\"Canteen\"]}", json0);

        String json1 = new ObjectMapper().writeValueAsString(entriesIterator.next());

        assertEquals("{\"food-premises\":\"456\",\"food-premises-types\":[\"Cafe\"]}", json1);

    }

    @Test
    public void should_read_name_containing_quote() throws Exception {

        Iterator<Map> entriesIterator = new DataFileReader("src/test/resources/tsv-quote.tsv", "tsv", FIELDS_JSON).getFileEntriesIterator();

        String json0 = new ObjectMapper().writeValueAsString(entriesIterator.next());

        System.out.println(json0);

        assertEquals("{\"food-premises\":\"123\",\"name\":\"\\\"go\\\" cafe\",\"food-premises-types\":[\"Cafe\"]}", json0);


    }

    protected List<Map> mapFrom(Iterator<Map> entriesIterator) {
        List<Map> data = new ArrayList<>();

        while (entriesIterator.hasNext()) {
            data.add(entriesIterator.next());
        }
        return data;
    }
}


