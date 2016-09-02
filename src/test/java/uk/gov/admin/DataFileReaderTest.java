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

    private static final Optional<String> FIELDS_JSON = Optional.of("file:src/test/resources/fields.json");
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
    public void should_read_cardinality_n_field_as_array_from_tsv() throws Exception {

        Iterator<Map> entriesIterator = new DataFileReader("src/test/resources/semi-colon.tsv", "tsv", FIELDS_JSON).getFileEntriesIterator();

        String json0 = new ObjectMapper().writeValueAsString(entriesIterator.next());

        assertEquals("{\"food-premises\":\"123\",\"food-premises-types\":[\"Restaurant\",\"Cafe\",\"Canteen\"]}", json0);

        String json1 = new ObjectMapper().writeValueAsString(entriesIterator.next());

        assertEquals("{\"food-premises\":\"456\",\"food-premises-types\":[\"Cafe\"]}", json1);

    }

    @Test
    public void should_read_cardinality_n_field_as_array_from_csv() throws Exception {

        Iterator<Map> entriesIterator = new DataFileReader("src/test/resources/semi-colon.csv", "csv", FIELDS_JSON).getFileEntriesIterator();

        String json0 = new ObjectMapper().writeValueAsString(entriesIterator.next());

        assertEquals("{\"food-premises\":\"123\",\"food-premises-types\":[\"Restaurant\",\"Cafe\",\"Canteen\"]}", json0);

        String json1 = new ObjectMapper().writeValueAsString(entriesIterator.next());

        assertEquals("{\"food-premises\":\"456\",\"food-premises-types\":[\"Cafe\"]}", json1);

    }

    @Test
    public void should_include_and_escape_quotes_from_tsv() throws Exception {

        List<Map> entries = mapFrom(new DataFileReader("src/test/resources/special-chars.tsv", "tsv", FIELDS_JSON).getFileEntriesIterator());
        // 1	"go" cafe	Cafe
        String json = new ObjectMapper().writeValueAsString(entries.get(0));

        assertEquals("{\"food-premises\":\"1\",\"name\":\"\\\"go\\\" cafe\",\"food-premises-types\":[\"Cafe\"]}", json);
    }

    @Test
    public void should_include_and_escape_quotes_around_whole_field_from_tsv() throws Exception {

        List<Map> entries = mapFrom(new DataFileReader("src/test/resources/special-chars.tsv", "tsv", FIELDS_JSON).getFileEntriesIterator());
        // 2	"go cafe"	Cafe
        String json = new ObjectMapper().writeValueAsString(entries.get(1));

        assertEquals("{\"food-premises\":\"2\",\"name\":\"\\\"go cafe\\\"\",\"food-premises-types\":[\"Cafe\"]}", json);
    }

    @Test
    public void should_escape_backslash_from_tsv() throws Exception {

        List<Map> entries = mapFrom(new DataFileReader("src/test/resources/special-chars.tsv", "tsv", FIELDS_JSON).getFileEntriesIterator());
        // 3	the \ backslash	Cafe
        String json = new ObjectMapper().writeValueAsString(entries.get(2));

        assertEquals("{\"food-premises\":\"3\",\"name\":\"the \\\\ backslash\",\"food-premises-types\":[\"Cafe\"]}", json);
    }

    @Test
    public void should_ignore_surrounding_quotes_from_csv() throws Exception {

        List<Map> entries = mapFrom(new DataFileReader("src/test/resources/special-chars.csv", "csv", FIELDS_JSON).getFileEntriesIterator());
        // "go, cafe",Cafe - whole field should be wrapped in quotes if it contains quotes or commas
        String json = new ObjectMapper().writeValueAsString(entries.get(0));

        assertEquals("{\"food-premises\":\"1\",\"name\":\"go, cafe\",\"food-premises-types\":[\"Cafe\"]}", json);
    }

    @Test
    public void should_read_escaped_quotes_from_csv() throws Exception {

        List<Map> entries = mapFrom(new DataFileReader("src/test/resources/special-chars.csv", "csv", FIELDS_JSON).getFileEntriesIterator());
        // 2,"""go"" cafe",Cafe - whole field should be wrapped in quotes if it contains quotes or commas
        String json = new ObjectMapper().writeValueAsString(entries.get(1));

        assertEquals("{\"food-premises\":\"2\",\"name\":\"\\\"go\\\" cafe\",\"food-premises-types\":[\"Cafe\"]}", json);
    }

    @Test
    public void should_read_backslash_from_csv() throws Exception {

        List<Map> entries = mapFrom(new DataFileReader("src/test/resources/special-chars.csv", "csv", FIELDS_JSON).getFileEntriesIterator());
        // 3,the \ backslash,Cafe - backslash is not special character in csv but must be escaped in json
        String json = new ObjectMapper().writeValueAsString(entries.get(2));

        assertEquals("{\"food-premises\":\"3\",\"name\":\"the \\\\ backslash\",\"food-premises-types\":[\"Cafe\"]}", json);
    }


    protected List<Map> mapFrom(Iterator<Map> entriesIterator) {
        List<Map> data = new ArrayList<>();

        while (entriesIterator.hasNext()) {
            data.add(entriesIterator.next());
        }
        return data;
    }
}


