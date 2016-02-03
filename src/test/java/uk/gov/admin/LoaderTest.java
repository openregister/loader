package uk.gov.admin;

import com.jcabi.http.Response;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;


@RunWith(MockitoJUnitRunner.class)
public class LoaderTest {
    @Mock
    Response mockResponse;

    @Test
    public void load_throwsRuntimeExceptionWhenMintApiReturnsNon200Response() throws IOException {
        Loader loader = new Loader("someUrl") {
            @Override
            protected Response makeRestCallToLoadEntryBatch(List<String> batch) throws IOException {
                return mockResponse;
            }
        };

        when(mockResponse.status()).thenReturn(400);
        when(mockResponse.body()).thenReturn("body");

        List<Map> entries = Collections.singletonList(Collections.singletonMap("entry1", "value1"));
        try {
            loader.load(entries.iterator());
            fail("must not reach here");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().startsWith("Exception while loading entries:"));
        }
    }

    @Test
    public void load_doesNotThrowRuntimeExceptionWhenMintApiReturnsSuccessResponse() {
        Arrays.asList(200, 201, 204).forEach(statusCode -> {
            Loader loader = new Loader("someUrl") {
                @Override
                protected Response makeRestCallToLoadEntryBatch(List<String> batch) throws IOException {
                    return mockResponse;
                }
            };

            when(mockResponse.status()).thenReturn(statusCode);
            when(mockResponse.body()).thenReturn("body");

            List<Map> entries = Collections.singletonList(Collections.singletonMap("entry1", "value1"));
            try {
                loader.load(entries.iterator());
            } catch (IOException e) {
                fail("should not throw this exception in this case");
            }
        });
    }

}