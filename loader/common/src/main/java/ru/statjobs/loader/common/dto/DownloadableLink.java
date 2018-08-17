package ru.statjobs.loader.common.dto;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;
import java.util.Map;

public class DownloadableLink implements Serializable {

    private final String url;
    private final Integer sequenceNum;
    private final String handlerName;
    private final Map<String, String> props;

    public DownloadableLink(String url, Integer sequenceNum, String handlerName, Map<String, String> props) {
        this.url = url;
        this.sequenceNum = sequenceNum;
        this.handlerName = handlerName;
        this.props = props;
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
