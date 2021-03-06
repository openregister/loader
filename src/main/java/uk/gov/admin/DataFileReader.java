package uk.gov.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.Iterators;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.lang.String.valueOf;
import static java.util.stream.Collectors.toMap;

public class DataFileReader {
    private final URI dataSourceURI;
    private final String type;
    private final ObjectMapper yamlObjectMapper = new ObjectMapper(new YAMLFactory());
    private final ObjectMapper jsonMapper = new ObjectMapper();
    private final Optional<Map<String, String>> cardinalities;

    public DataFileReader(String dataSource, String type, Optional<String> fieldsSource) throws URISyntaxException {
        this.type = type;
        this.dataSourceURI = createDataUri(dataSource);
        this.cardinalities = fieldsSource.map(this::loadFieldCardinalities);
    }

    public DataFileReader(String dataSource, String type) throws URISyntaxException {
        this(dataSource, type, Optional.empty());
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

    private Map<String, String> loadFieldCardinalities(String fieldsUrlStr) {
        try {
            URL fieldsUrl = new URL(fieldsUrlStr);
            Map<String, Map<String, String>> fieldsToProperties = jsonMapper.readValue(fieldsUrl, Map.class);
            return fieldsToProperties.entrySet().stream().collect(
                    toMap(e -> e.getKey(), e -> e.getValue().get("cardinality")));

        } catch (IOException e) {
            throw new UncheckedIOException(e);
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

        CsvSchema.Builder builder = CsvSchema.builder();
        BufferedReader reader = reader();

        if (cardinalities.isPresent()) {
            String header = reader.readLine();

            Arrays.stream(header.split(valueOf(separator))).map(String::trim).forEach(field -> {
                String columnHeader = cardinalities.get().get(field);
                if (columnHeader == null){
                    throw new RuntimeException("Column header did not match any field name. Are the headers missing?");
                }
                switch (columnHeader) {
                    case "n":
                        builder.addArrayColumn(field);
                        break;
                    case "1":
                        builder.addColumn(field);
                        break;
                    default:
                        throw new RuntimeException("Heading did not match valid cardinality.");
                }
            });

        } else {
            builder.setUseHeader(true);
        }

        builder.setColumnSeparator(separator).setAllowComments(true);

        if (separator == '\t') {
            builder.disableQuoteChar();
        }

        CsvSchema schema = builder.build();

        return new CsvMapper().reader(Map.class)
                .with(schema)
                .readValues(reader);
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
