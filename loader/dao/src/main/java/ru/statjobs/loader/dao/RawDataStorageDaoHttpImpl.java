package ru.statjobs.loader.dao;


import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.statjobs.loader.common.dao.RawDataStorageDao;
import ru.statjobs.loader.common.dto.DownloadableLink;
import ru.statjobs.loader.common.dto.RawData;
import ru.statjobs.loader.utils.JsonUtils;


public class RawDataStorageDaoHttpImpl implements RawDataStorageDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(RawDataStorageDaoHttpImpl.class);

    private final JsonUtils jsonUtils;
    private final String url;

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

    public RawDataStorageDaoHttpImpl(JsonUtils jsonUtils, String url) {
        this.jsonUtils = jsonUtils;
        this.url = url;
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
        ContentResponse response;
        RawData rawData = new RawData(link, json);
        String message = jsonUtils.createString(rawData);
        try {
            response = httpClient
                    .POST(url)
                    .content(new StringContentProvider(message), "application/json")
                    .send();
        } catch (Exception e) {
            LOGGER.error("fail send json {} to url {}", json, url);
            throw new RuntimeException("fail message " +  url +  " " + message, e);
        }
        if (response.getStatus() != HttpStatus.OK_200) {
            LOGGER.error("response status {}, json {}, url {}", json, url, response.getStatus());
            throw new RuntimeException("fail message code:" + response.getStatus() + ", url:" +  url +  ", message:" + message);
        }


    }
}
