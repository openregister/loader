package uk.gov.admin;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Iterator;

class CsvEntriesIterator implements Iterator<String> {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    private final MappingIterator<String> mappingIterator;

    public CsvEntriesIterator(MappingIterator<String> mappingIterator) {
        this.mappingIterator = mappingIterator;
    }

    @Override
    public boolean hasNext() {
        return mappingIterator.hasNext();
    }

    @Override
    public String next() {
        try {
            return objectMapper.writeValueAsString(mappingIterator.next());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
