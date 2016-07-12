package uk.gov.admin;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.Iterators;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Stream;

public class DataFileReader {
    private final URI dataSourceURI;
    private final String type;
    private final ObjectMapper yamlObjectMapper = new ObjectMapper(new YAMLFactory());

    public DataFileReader(String dataSource, String type) throws URISyntaxException {
        this.type = type;
        this.dataSourceURI = createDataUri(dataSource);
    }

    public Iterator<Map> getFileEntriesIterator() throws IOException {
        switch (type) {
            case "yaml_dir":
                return readYamlFiles();
            case "yaml":
                return Iterators.forArray(convertToJsonEntry(Paths.get(dataSourceURI)));
            case "jsonl":
                return reader().lines().map(this::convertToMap).iterator();
            case "csv":
                return csvIterator(',');
            default:
                return csvIterator('\t');
        }
    }

    private Map convertToMap(String input) {
        try {
            return yamlObjectMapper.readValue(input, Map.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Iterator<Map> readYamlFiles() throws IOException {
        Iterator<Path> filesIterator = Files.newDirectoryStream(Paths.get(dataSourceURI)).iterator();
        return Stream.of(Iterators.toArray(filesIterator, Path.class))
                .filter(path -> path.toString().endsWith(".yaml"))
                .map(this::convertToJsonEntry)
                .iterator();
    }

    private Map convertToJsonEntry(Path path) {
        try {
            return yamlObjectMapper.readValue(path.toFile().toURI().toURL().openStream(), Map.class);
        } catch (IOException e) {
            throw new RuntimeException("Error reading yaml file " + path.toString(), e);
        }
    }

    private Iterator<Map> csvIterator(char separator) throws IOException {
        CsvSchema schema = CsvSchema.builder()
                .setColumnSeparator(separator)
                .setUseHeader(true)
                .addColumn("school")
                .addColumn("name")
                .addColumn("address")
                .addColumn("local-authority")
                .addColumn("minimum-age")
                .addColumn("maximum-age")
                .addColumn("headteacher")
                .addColumn("website")
                .addArrayColumn("denominations", ';')
                .addArrayColumn("dioceses", ';')
                .addColumn("school-type")
                .addColumn("school-phase")
                .addColumn("school-admissions-policy")
                .addColumn("school-gender")
                .addColumn("school-tags")
                .addColumn("school-federation")
                .addColumn("start-date")
                .addColumn("end-date")
                .build();

        return new CsvMapper().reader(Map.class)
                .with(schema)
                .readValues(reader());
    }

    private BufferedReader reader() {
        try {
            InputStreamReader inputStreamReader = new InputStreamReader(dataSourceURI.toURL().openStream(), StandardCharsets.UTF_8);
            return new BufferedReader(inputStreamReader);
        } catch (Exception e) {
            throw new RuntimeException("Error creating stream to read data to load", e);
        }
    }

    private URI createDataUri(String datafile) throws URISyntaxException {
        if (type.equals("yaml_dir")) {
            File dir = new File(datafile);
            if (dir.isDirectory()) {
                return dir.toURI();
            } else {
                throw new IllegalArgumentException("datasource: " + datafile + " must be a directory which contains yaml files.");
            }
        }

        if ((datafile.startsWith("http://") || datafile.startsWith("https://"))) {
            return new URI(datafile);
        } else {
            File inputFile = new File(datafile);
            if (inputFile.isDirectory()) {
                throw new RuntimeException("'" + inputFile + "' must be a file");
            }
            return inputFile.toURI();
        }
    }
}
