package uk.gov.admin;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import com.jcabi.http.Request;
import com.jcabi.http.Response;
import com.jcabi.http.request.JdkRequest;
import com.jcabi.http.wire.BasicAuthWire;

import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;

class Loader {
    private static final int BATCH_SIZE = 1000;

    private final String mintUrl;
    private long entryCount = 0;

    public Loader(String mintUrl) {
        this.mintUrl = mintUrl;
    }

    public void load(Iterator<Map> fileEntries) throws IOException {
        Iterable<List<Map>> entryBatches = Iterables.partition(() -> fileEntries, BATCH_SIZE);

        for (List<Map> entryBatch : entryBatches) {
            send(entryBatch);
        }
    }

    private void send(List<Map> batch) throws IOException {
        Response response = makeRestCallToLoadEntryBatch(
                new EmptyFieldPruner().removeKeysWithEmptyValues(batch)
                        .stream()
                        .map(this::convertToString)
                        .collect(Collectors.toList()));

        if (!isSuccess(response.status())) {
            throw new RuntimeException("Exception while loading entries: statusCode -> " + response.status() + "\n" +
                    " entity -> " + response.body());
        }
        entryCount += batch.size();

        System.out.println("Loaded " + entryCount + " entries...");
    }


    private String convertToString(Map input) {
        try {
            return new ObjectMapper().writeValueAsString(input);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    protected Response makeRestCallToLoadEntryBatch(List<String> batch) throws IOException {
        return new JdkRequest(mintUrl)
                .through(BasicAuthWire.class)
                .method(Request.POST)
                .body()
                .set(String.join("\n", batch))
                .back()
                .fetch();
    }

    private boolean isSuccess(int statusCode) {
        return Status.fromStatusCode(statusCode).getFamily() == SUCCESSFUL;
    }
}
