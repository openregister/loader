package uk.gov.admin;

import com.google.common.base.Predicate;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EmptyFieldPruner {
    Predicate<Map.Entry> keyHasNonEmptyValue = new Predicate<Map.Entry>() {
        public boolean apply(Map.Entry i) {
            if (i == null) {
                return false;
            } else if(i.getValue() instanceof String && ((String) i.getValue()).isEmpty()) {
                return false;
            }
            return true;
        }
    };

    public Map removeKeysWithEmptyValues(Map input) {
        return Maps.filterEntries(input, keyHasNonEmptyValue);
    }

    public List<Map> removeKeysWithEmptyValues(List<Map> input) {
        return input.stream().map(this::removeKeysWithEmptyValues).collect(Collectors.toList());
    }
}
