package uk.gov.admin;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LoaderApplication {
    public static void main(String[] args) {
        try {
            Map<String, String> argsMap = createArgumentsMap(args);
            String dataFile = argsMap.get("--datasource");
            String mintUrl = argsMap.get("--minturl");
            String type = argsMap.get("--type");
            Optional<String> fieldsUrl = Optional.ofNullable(argsMap.get("--fieldsurl"));

            System.out.println(String.format("Loading to %s from %s", mintUrl, dataFile));

            DataFileReader dataFileReader = new DataFileReader(dataFile, type, fieldsUrl);

            new Loader(mintUrl).load(dataFileReader.getFileEntriesIterator());

            System.out.println("Loading complete");
        } catch (Exception e) {
            e.printStackTrace();
        }

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
        return new RuntimeException("Usage: java LoaderApplication --minturl=<mint-load-url> --datasource=<loadfile.json> --type=<jsonl|tsv|csv|yaml|yaml_dir> --fieldsurl=<fields-url>(optional)");
    }

}
