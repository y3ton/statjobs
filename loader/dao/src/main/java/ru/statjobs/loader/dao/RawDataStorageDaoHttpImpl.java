package ru.statjobs.loader.dao;


import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.statjobs.loader.common.dao.RawDataStorageDao;
import ru.statjobs.loader.common.dto.DownloadableLink;
import ru.statjobs.loader.common.dto.RawData;
import ru.statjobs.loader.utils.JsonUtils;

import java.util.function.Supplier;


public class RawDataStorageDaoHttpImpl implements RawDataStorageDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(RawDataStorageDaoHttpImpl.class);

    public final static int SEND_COUNT_ATTEMPT = 3;

    private final JsonUtils jsonUtils;
    private final String url;
    private final String authKey;
    private final HttpUtils httpUtils;

    private HttpClient httpClient = new HttpClient();

    public void start() {
        try {
            httpClient.start();
        } catch (Exception e) {
            throw new RuntimeException("fail start httpClient", e);
        }
    }

    public void stop() {
        try {
            httpClient.stop();
        } catch (Exception e) {
            throw new RuntimeException("fail stop httpClient", e);
        }
    }

    public RawDataStorageDaoHttpImpl(HttpUtils httpUtils, JsonUtils jsonUtils, String url, String authKey) {
        this.jsonUtils = jsonUtils;
        this.url = url;
        this.authKey = authKey;
        this.httpUtils = httpUtils;
    }

    @Override
    public void saveHhVacancy(DownloadableLink link, String json) {
        send(link, json);
    }

    @Override
    public void saveHhResume(DownloadableLink link, String json) {
        send(link, json);
    }

    private void send(DownloadableLink link, String json) {
        RawData rawData = new RawData(link, json);
        String message = jsonUtils.createString(rawData);
        Supplier<Request> request = () -> httpClient.POST(url)
                .header("Authorization", authKey)
                .content(new StringContentProvider(message), "application/json");
        try {
            httpUtils.send(request, SEND_COUNT_ATTEMPT);
        } catch (Exception e) {
            LOGGER.error("fail send json:{} to url: {}", json, url);
            throw new RuntimeException(e);
        }
    }


}
