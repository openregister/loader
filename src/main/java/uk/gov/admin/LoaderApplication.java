package uk.gov.admin;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LoaderApplication {
    public static void main(String[] args) throws Exception {
        Map<String, String> argsMap = createArgumentsMap(args);

        String dataFile = argsMap.get("--datasource");
        String mintUrl = argsMap.get("--minturl");
        String type = argsMap.get("--type");

        DataFileReader dataFileReader = new DataFileReader(dataFile, type);

        new Loader(mintUrl).load(dataFileReader.getFileEntriesIterator());

    }

    private static Map<String, String> createArgumentsMap(String[] args) throws Exception {
        try {

            Map<String, String> argsMap = Stream.of(args)
                    .map(a -> a.split("="))
                    .collect(Collectors.toMap(argEntry -> argEntry[0], argEntry -> argEntry[1]));

            if (argsMap.containsKey("--datasource") && argsMap.containsKey("--minturl") && argsMap.containsKey("--type")) {
                return argsMap;
            }

            throw printUsageException();

        } catch (ArrayIndexOutOfBoundsException e) {

            throw printUsageException();

        }
    }

    private static Exception printUsageException() throws Exception {
        return new RuntimeException("Usage: java LoaderApplication --minturl=<mint-load-url> --datasource=<loadfile.json> --type=<jsonl|tsv|csv|yaml|yaml_dir>");
    }

}
