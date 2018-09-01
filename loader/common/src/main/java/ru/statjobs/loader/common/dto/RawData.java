package ru.statjobs.loader.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

public class RawData implements Serializable {

    private final DownloadableLink link;
    private final String json;

    @JsonCreator
    public RawData(
            @JsonProperty("link") DownloadableLink link,
            @JsonProperty("json") String json) {
        this.link = link;
        this.json = json;
    }

    public DownloadableLink getLink() {
        return link;
    }

    public String getJson() {
        return json;
    }


    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("link", link)
                .append("json", json)
                .toString();
    }
}
