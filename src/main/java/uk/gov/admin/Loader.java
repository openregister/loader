package uk.gov.admin;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Iterables;
import com.jcabi.http.Request;
import com.jcabi.http.Response;
import com.jcabi.http.request.JdkRequest;

import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;

class Loader {
    private static final int BATCH_SIZE = 1000;

    private final String mintUrl;
    private long entryCount = 0;

    public Loader(String mintUrl) {
        this.mintUrl = mintUrl;
    }

    public void load(Iterator<String> fileEntries) throws IOException {
        Iterable<List<String>> entryBatches = Iterables.partition(() -> fileEntries, BATCH_SIZE);

        ObjectMapper om = new ObjectMapper();
        entryBatches.forEach(singleBatch -> {
            List<String> prunedSingleBatch =
                    singleBatch.stream().map(singleBatchEntry -> {
                        try {
                            JsonNode root = om.readTree(singleBatchEntry);
                            EmptyFieldPruner.prune(root);
                            return om.writeValueAsString(root);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }).collect(Collectors.toList());

            try {
                send(prunedSingleBatch);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void send(List<String> batch) throws IOException {
        Response response = new JdkRequest(mintUrl)
                .method(Request.POST)
                .body()
                .set(String.join("\n", batch))
                .back()
                .fetch();
        if (!isSuccess(response.status())) {
            throw new RuntimeException("Exception while loading entries: statusCode -> " + response.status() + "\n" +
                    " entity -> " + response.body());
        }
        entryCount += batch.size();

        System.out.println("Loaded " + entryCount + " entries...");
    }

    private boolean isSuccess(int statusCode) {
        return Status.fromStatusCode(statusCode).getFamily() == SUCCESSFUL;
    }
}
