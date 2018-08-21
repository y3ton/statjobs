package ru.statjobs.loader.common.dto;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

public class RawData implements Serializable{

    private DownloadableLink link;
    private String json;

    public RawData(DownloadableLink link, String json) {
        this.link = link;
        this.json = json;
    }

    public RawData() {
    }

    public DownloadableLink getLink() {
        return link;
    }

    public String getJson() {
        return json;
    }

    public void setLink(DownloadableLink link) {
        this.link = link;
    }

    public void setJson(String json) {
        this.json = json;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("link", link)
                .append("json", json)
                .toString();
    }
}
