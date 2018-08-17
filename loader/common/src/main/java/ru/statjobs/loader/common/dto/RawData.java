package ru.statjobs.loader.common.dto;

import java.io.Serializable;

public class RawData implements Serializable{

    private final DownloadableLink link;
    private final String json;

    public DownloadableLink getLink() {
        return link;
    }

    public String getJson() {
        return json;
    }

    public RawData(DownloadableLink link, String json) {
        this.link = link;
        this.json = json;
    }


}
