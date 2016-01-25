# loader
Loads data from jsonl, tsv, csv or yaml sources in to a register.

[![Build Status](https://travis-ci.org/openregister/loader.svg?branch=master)](https://travis-ci.org/openregister/loader)

Source must contain the data in the format explained below:

- jsonl: The source path must be a file which contains a json entry per line
- yaml: The source path must be the file whose contents are parsed as one register entry
- yaml_dir: The source path must be the path of a directory which contains yaml files where every yaml file is parsed as one register entry
- csv: The file must contain first line as header which explains the register fields and then subsequent one line per entry
- tsv: The file must contain first line as header which explains the register fields and then subsequent one line per entry

# Requirements

- Java 1.8+

# Build and Run project

- Use command `./gradlew cleanIdea idea` to generate the idea project files
- Build project using command `./gradlew clean build`

# Bulk load data from CLI

There are 2 options:

1. Run via gradle:
-     `./gradlew bulkLoad -Pminturl=<mint-url> -Pdatasource=<data-file-path> [-Ptype=jsonl|tsv|csv|yaml|yaml_dir]`
        e.g. ./gradlew bulkLoad -Pminturl=http://localhost:4567/load -Pdatasource=datafile.tsv -Ptype=tsv
2. Run using the built jar
-     `java -jar <path-to-jar> --minturl=<mint-url> --datasource=<data-file-path> --type=<jsonl|tsv|csv|yaml|yaml_dir>`
        e.g. java -jar loader.jar --minturl=http://localhost:4567/load --datasource=datafile.tsv --type=tsv
