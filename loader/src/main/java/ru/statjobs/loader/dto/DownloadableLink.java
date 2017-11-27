package ru.statjobs.loader.dto;

public class DownloadableLink {

    private final String url;
    private final String handlerName;

    public DownloadableLink(String url, String handlerName) {
        this.url = url;
        this.handlerName = handlerName;
    }

    public String getUrl() {
        return url;
    }

    public String getHandlerName() {
        return handlerName;
    }
}
