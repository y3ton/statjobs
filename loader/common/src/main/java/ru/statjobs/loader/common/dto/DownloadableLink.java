package ru.statjobs.loader.common.dto;

import org.apache.commons.lang3.builder.ToStringBuilder;
import ru.statjobs.loader.common.url.UrlTypes;

import java.io.Serializable;
import java.util.Map;

public class DownloadableLink implements Serializable {

    private String url;
    private Integer sequenceNum;
    private UrlTypes handlerName;
    private Map<String, String> props;

    public DownloadableLink() {
    }

    public DownloadableLink(String url, Integer sequenceNum, UrlTypes handlerName, Map<String, String> props) {
        this.url = url;
        this.sequenceNum = sequenceNum;
        this.handlerName = handlerName;
        this.props = props;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getSequenceNum() {
        return sequenceNum;
    }

    public void setSequenceNum(Integer sequenceNum) {
        this.sequenceNum = sequenceNum;
    }

    public UrlTypes getHandlerName() {
        return handlerName;
    }

    public void setHandlerName(UrlTypes handlerName) {
        this.handlerName = handlerName;
    }

    public Map<String, String> getProps() {
        return props;
    }

    public void setProps(Map<String, String> props) {
        this.props = props;
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
