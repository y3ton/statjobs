package ru.statjobs.loader.dao;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Request;
import org.eclipse.jetty.client.util.StringContentProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.statjobs.loader.common.dao.DownloadableLinkDao;
import ru.statjobs.loader.common.dto.DownloadableLink;
import ru.statjobs.loader.utils.JsonUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class DownloadableLinkDaoHttpImpl implements DownloadableLinkDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(RawDataStorageDaoHttpImpl.class);

    public final static int SEND_COUNT_ATTEMPT = 3;
    public final static int MAX_BATCH_SIZE = 100;

    private final JsonUtils jsonUtils;
    private final String url;
    private final String authKey;
    private final HttpUtils httpUtils;

    private HttpClient httpClient = new HttpClient();

    public DownloadableLinkDaoHttpImpl(HttpUtils httpUtils, JsonUtils jsonUtils, String url, String authKey) {
        this.jsonUtils = jsonUtils;
        this.url = url;
        this.authKey = authKey;
        this.httpUtils = httpUtils;
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
        Supplier<Request> request = () -> httpClient.POST(url + "/create")
                .content(new StringContentProvider(json), "application/json")
                .header(HttpHeader.AUTHORIZATION, authKey);
        try {
            httpUtils.send(request, SEND_COUNT_ATTEMPT);
        } catch (Exception e) {
            LOGGER.error("fail create link {}, json {}", url + "/create", json);
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public boolean createDownloadableLinks(List<DownloadableLink> links) {
        boolean b = true;
        List<DownloadableLink> list = new ArrayList<>();
        for (DownloadableLink link: links) {
            list.add(link);
            if (list.size() >= MAX_BATCH_SIZE) {
                b = sendList(list) && b;
                list = new ArrayList<>();
            }
        }
        return sendList(list) && b;

    }

    private boolean sendList(List<DownloadableLink> links) {
        if (links.isEmpty()) {
            return true;
        }
        String json = jsonUtils.createString(links);
        Supplier<Request> request = () -> httpClient.POST(url + "/createlist")
                .content(new StringContentProvider(json), "application/json")
                .header(HttpHeader.AUTHORIZATION, authKey);
        try {
            httpUtils.send(request, SEND_COUNT_ATTEMPT);
        } catch (Exception e) {
            LOGGER.error("fail create links {}, json {}", url + "/createlist", json);
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public boolean deleteDownloadableLink(DownloadableLink link) {
        String json = jsonUtils.createString(link);
        Supplier<Request> request = () -> httpClient.POST(url + "/delete")
                .content(new StringContentProvider(json), "application/json")
                .header(HttpHeader.AUTHORIZATION, authKey);
        try {
            httpUtils.send(request, SEND_COUNT_ATTEMPT);
        } catch (Exception e) {
            LOGGER.error("fail delete link {}, json {}", url + "/delete", json);
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public DownloadableLink getDownloadableLink() {
        Supplier<Request> request = () -> httpClient.newRequest(url + "/get")
                .method(HttpMethod.GET)
                .header(HttpHeader.AUTHORIZATION, authKey);
        String json = null;
        try {
            json = httpUtils.send(request, SEND_COUNT_ATTEMPT);
        } catch (Exception e) {
            LOGGER.error("fail get link {}", url + "/get");
            throw new RuntimeException(e);
        }
        return jsonUtils.readString(json, DownloadableLink.class );
    }


}
