package ru.statjobs.loader.dao;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.ContentResponse;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.statjobs.loader.common.dao.DownloadableLinkDao;
import ru.statjobs.loader.common.dto.DownloadableLink;
import ru.statjobs.loader.utils.JsonUtils;

public class DownloadableLinkDaoHttpImpl implements DownloadableLinkDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(RawDataStorageDaoHttpImpl.class);

    private final JsonUtils jsonUtils;
    private final String url;
    private final String authKey;

    private HttpClient httpClient = new HttpClient();

    public DownloadableLinkDaoHttpImpl(JsonUtils jsonUtils, String url, String authKey) {
        this.jsonUtils = jsonUtils;
        this.url = url;
        this.authKey = authKey;
    }

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

    @Override
    public boolean createDownloadableLink(DownloadableLink link) {
        String json = jsonUtils.createString(link);
        Request request = httpClient.POST(url + "/create")
                .content(new StringContentProvider(json), "application/json");
        send(request);
        return true;
    }

    @Override
    public boolean deleteDownloadableLink(DownloadableLink link) {
        String json = jsonUtils.createString(link);
        Request request = httpClient.POST(url + "/delete")
                .content(new StringContentProvider(json), "application/json");
        send(request);
        return true;
    }

    @Override
    public DownloadableLink getDownloadableLink() {
        Request request = httpClient.newRequest(url + "/get")
                .method(HttpMethod.GET);
        String json = send(request);
        return jsonUtils.readString(json, DownloadableLink.class );
    }


    private String send(Request request) {
        ContentResponse response;
        try {
            response = request.header(HttpHeader.AUTHORIZATION, authKey).send();
        } catch (Exception e) {
            LOGGER.error("fail send to url {}", request.getURI());
            throw new RuntimeException("fail message " +  request.getURI(), e);
        }
        if (response.getStatus() != HttpStatus.OK_200) {
            LOGGER.error("response status {}, url {}", response.getStatus(), request.getURI());
            throw new RuntimeException("fail message code:" + response.getStatus() + ", url:" +  request.getURI());
        }
        return response.getContentAsString();
    }


}
