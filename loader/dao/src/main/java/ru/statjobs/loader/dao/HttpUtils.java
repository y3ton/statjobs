package ru.statjobs.loader.dao;

import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public class HttpUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpUtils.class);

    public static final int TIMEOUT_SECOND = 3;

    public String send(Supplier<Request> f, int attempt) throws Exception {
        Exception exception = null;
        for (int i = 0; i < attempt; i++) {
            Request request = f.get().timeout(TIMEOUT_SECOND, TimeUnit.SECONDS);
            try {
                ContentResponse response = request.send();
                if (response.getStatus() == HttpStatus.OK_200) {
                    return response.getContentAsString();
                }
                LOGGER.error("response status {}, url {}", request.getURI(), response.getStatus());
                exception = new RuntimeException("fail message code:" + response.getStatus() + ", url:" +  request.getURI());
            } catch (InterruptedException e) {
                throw e;
            } catch (ExecutionException | TimeoutException e) {
                exception = new RuntimeException(e);
                LOGGER.error("fail send {}", request.getURI());
            }
            if (i < attempt - 1) {
                Thread.sleep((i + 1) * 1000);
            }
        }
        throw exception;
    }

}
