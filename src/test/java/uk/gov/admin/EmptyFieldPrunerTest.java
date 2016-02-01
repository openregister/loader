package uk.gov.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class EmptyFieldPrunerTest {

    @Test
    public void emptyValue_causesFieldToBeRemoved() {
        final String jsonData = "{\"key\" : \"value\", \"emptyKey\" : \"\"}";

        ObjectMapper om = new ObjectMapper();
        try {
            JsonNode node = om.readTree(jsonData);
            EmptyFieldPruner.prune(node);
            String output = om.writeValueAsString(node);
            assertThat(output, not(containsString("emptyKey")));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void emptyValueInContainer_causesFieldToBeRemoved() {
        final String jsonData = "{\"key1\" : \"value1\", \"container\" : { \"subkey1\"  : \"\", \"subkey2\" : \"subkey2value\"}}";

        ObjectMapper om = new ObjectMapper();
        try {
            JsonNode node = om.readTree(jsonData);
            EmptyFieldPruner.prune(node);
            String output = om.writeValueAsString(node);
            assertThat(output, not(containsString("subkey1")));
            assertThat(output, containsString("subkey2"));
            assertThat(output, containsString("subkey2value"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void emptyValueInArray_causesFieldToBeRemoved() {
        final String jsonData = "{\"key1\" : \"value1\", \"array\" : [{ \"subarray1\"  : \"\" }, {\"subarray2\" : \"subarray2value\"}]}";

        ObjectMapper om = new ObjectMapper();
        try {
            JsonNode node = om.readTree(jsonData);
            EmptyFieldPruner.prune(node);
            String output = om.writeValueAsString(node);
            assertThat(output, not(containsString("subarray1")));
            assertThat(output, containsString("subarray2"));
            assertThat(output, containsString("subarray2value"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
