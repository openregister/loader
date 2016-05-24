package uk.gov.admin;

import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class EmptyFieldPruner {
    public Map removeKeysWithEmptyValues(Map input) {
        return Maps.filterEntries(input, e -> !isValueNullOrBlank(e));
    }

    public List<Map> removeKeysWithEmptyValues(List<Map> input) {
        return input.stream().map(this::removeKeysWithEmptyValues).collect(Collectors.toList());
    }

    private boolean isValueNullOrBlank(Map.Entry e) {
        return e == null || e.getValue().toString().trim().isEmpty();
    }
}
