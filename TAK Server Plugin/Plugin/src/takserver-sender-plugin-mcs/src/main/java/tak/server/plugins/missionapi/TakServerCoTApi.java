package tak.server.plugins.missionapi;

import java.util.concurrent.Future;

import org.apache.hc.client5.http.async.methods.SimpleHttpRequest;
import org.apache.hc.client5.http.async.methods.SimpleHttpRequests;
import org.apache.hc.client5.http.async.methods.SimpleHttpResponse;
import org.apache.hc.client5.http.impl.async.CloseableHttpAsyncClient;
import org.apache.hc.client5.http.impl.async.HttpAsyncClients;
import org.apache.hc.core5.concurrent.FutureCallback;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TakServerCoTApi {
    private static final Logger logger = LoggerFactory.getLogger(TakServerCoTApi.class);

    public static void queryForCotEvent(String targetAddress, int targetPort, String uuid, TakServerCoTCallback callback) {
        try {
            final IOReactorConfig ioReactorConfig = IOReactorConfig.custom()
                .setSoTimeout(Timeout.ofSeconds(5))
                .build();

            final CloseableHttpAsyncClient client = HttpAsyncClients.custom()
                .setIOReactorConfig(ioReactorConfig)
                .build();

            client.start();

            String requestUri = "/Marti/api/cot/xml/" + uuid;
            final HttpHost target = new HttpHost(targetAddress, targetPort);
            final SimpleHttpRequest httpget = SimpleHttpRequests.get(target, requestUri);
            logger.info("Executing request " + httpget.getMethod() + " " + httpget.getUri());
            final Future<SimpleHttpResponse> future = client.execute(
                httpget,
                new FutureCallback<SimpleHttpResponse>() {
                    @Override
                        public void completed(final SimpleHttpResponse response) {
                            callback.cotResult(response.getCode() == 200, response.getBody().getBodyText());
                        }

                    @Override
					    public void failed(final Exception ex) {
                            logger.error(target.toURI() + requestUri + "->" + ex);
                            callback.cotResult(false, "");
						}

					@Override
					    public void cancelled() {
						    logger.error(target.toURI() + requestUri + " cancelled");
                            callback.cotResult(false, "");
						}
                });
        } 
        catch (Exception e) {
            logger.info("exception making HTTP request", e);
        }
    }
}
