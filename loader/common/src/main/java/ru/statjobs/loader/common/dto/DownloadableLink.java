package ru.statjobs.loader.common.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.apache.commons.lang3.builder.ToStringBuilder;
import ru.statjobs.loader.common.url.UrlTypes;

import java.io.Serializable;
import java.util.Map;

public class DownloadableLink implements Serializable {

    private final String url;
    private final Integer sequenceNum;
    private final UrlTypes handlerName;
    private final Map<String, String> props;


    @JsonCreator
    public DownloadableLink(
            @JsonProperty("url") String url,
            @JsonProperty("sequenceNum") Integer sequenceNum,
            @JsonProperty("handlerName") UrlTypes handlerName,
            @JsonProperty("props") Map<String, String> props) {
        this.url = url;
        this.sequenceNum = sequenceNum;
        this.handlerName = handlerName;
        this.props = props;
    }

    public String getUrl() {
        return url;
    }

    public Integer getSequenceNum() {
        return sequenceNum;
    }

    public UrlTypes getHandlerName() {
        return handlerName;
    }

    public Map<String, String> getProps() {
        return props;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("url", url)
                .append("sequenceNum", sequenceNum)
                .append("handlerName", handlerName)
                .append("props", props)
                .toString();
    }
}
