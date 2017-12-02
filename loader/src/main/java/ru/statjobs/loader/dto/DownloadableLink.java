package ru.statjobs.loader.dto;

public class DownloadableLink {

    private final String url;
    private final Integer sequenceNum;
    private final String handlerName;

    public DownloadableLink(String url, Integer sequenceNum, String handlerName) {
        this.url = url;
        this.sequenceNum = sequenceNum;
        this.handlerName = handlerName;
    }

    public String getUrl() {
        return url;
    }

    public String getHandlerName() {
        return handlerName;
    }

    public Integer getSequenceNum() {
        return sequenceNum;
    }
}
