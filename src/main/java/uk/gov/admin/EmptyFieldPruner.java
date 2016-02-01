package uk.gov.admin;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Iterator;
import java.util.Map;

public class EmptyFieldPruner {
    public static void prune(JsonNode node) {
        Iterator<Map.Entry<String, JsonNode>> it = node.fields();
        while (it.hasNext()) {
            Map.Entry<String, JsonNode> keyValue = it.next();
            if (keyValue.getValue().isValueNode()) {
                if (keyValue.getValue().textValue().isEmpty()) {
                    it.remove();
                }
            } else if (keyValue.getValue().isContainerNode()) {
                prune(keyValue.getValue());
            }
        }

        // Parse any arrays (also reparses containers due to Jackson limitations)
        if (node.isContainerNode()) {
            Iterator<JsonNode> it2 = node.elements();
            while (it2.hasNext()) {
                JsonNode subnode = it2.next();
                prune(subnode);

                // Remove any empty containers / array elements
                if (subnode.isContainerNode() && !subnode.elements().hasNext()) {
                    it2.remove();
                }
            }
        }
    }
}
