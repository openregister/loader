# loader
Loads data from jsonl, tsv, csv or yaml sources in to a register.

[![Build Status](https://travis-ci.org/openregister/loader.svg?branch=master)](https://travis-ci.org/openregister/loader)

# Requirements

- Java 1.8+

# Build and Run project

- Use command `./gradlew cleanIdea idea` to generate the idea project files
- Build project using command `./gradlew clean build`

# Bulk load data from CLI

There are 2 options:

1. Run via gradle:
-     `gradle bulkLoad -PmintUrl=<mint-url> -Pdatafile=<data-file-path> [-Ptype=jsonl|tsv|csv|yaml]`
        e.g. gradle bulkLoad -PmintUrl=http://localhost:4567/load -Pdatafile=datafile.tsv -Ptype=tsv
2. Run using the built jar
-     `java -jar <path-to-jar> --mintUrl=<mint-url> --datafile=<data-file-path> --type=<jsonl|tsv|csv|yaml>`
        e.g. java -jar loader.jar --mintUrl=http://localhost:4567/load --datafile=datafile.tsv --type=tsv
