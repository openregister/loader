package uk.gov.admin;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

public class EmptyFieldPrunerTest {
    @Test
    public void nonStringTypeValue_remainsIntact() {
        Map testData = Collections.singletonMap("fields", Arrays.asList("a", "b"));
        Map result = new EmptyFieldPruner().removeKeysWithEmptyValues(testData);
        assertThat(result.size(), is(1));
        assertThat(result.get("fields").toString(), equalTo("[a, b]"));
    }

    @Test
    public void emptyValue_causesKeyRemoval() {
        Map<String, String> testData = Collections.singletonMap("key", "");

        Map result = new EmptyFieldPruner().removeKeysWithEmptyValues(testData);
        assertThat(result.size(), is(0));
        assertThat(result.get("key"), is(nullValue()));
    }

    @Test
    public void multipleEmptyValues_removesKeys() {
        Map<String, String> testData = new HashMap<>();
        testData.put("k1", "v1");
        testData.put("k2", "");
        testData.put("k3", "v3");
        testData.put("k4", "");
        testData.put("k5", " ");
        testData.put("k6", "             ");


        Map result = new EmptyFieldPruner().removeKeysWithEmptyValues(testData);
        assertThat(result.size(), is(2));
        assertThat(result.get("k1"), is("v1"));
        assertThat(result.get("k2"), is(nullValue()));
        assertThat(result.get("k3"), is("v3"));
        assertThat(result.get("k4"), is(nullValue()));
        assertThat(result.get("k5"), is(nullValue()));
        assertThat(result.get("k6"), is(nullValue()));
    }

    @Test
    public void nonEmptyValue_doesntRemoveKey() {
        Map<String, String> testData = Collections.singletonMap("key", "value");

        Map result = new EmptyFieldPruner().removeKeysWithEmptyValues(testData);
        assertThat(result.size(), is(1));
        assertThat(result.get("key"), is("value"));
    }

}
