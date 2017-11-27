package ru.statjobs.loader.dao;

import ru.statjobs.loader.dto.DownloadableLink;

import java.util.HashMap;
import java.util.Map;

public class QueueDownloadableLinkDaoStub implements QueueDownloadableLinkDao  {

    private Map<String, DownloadableLink> map = new HashMap<>();

    @Override
    public boolean createDownloadableLink(DownloadableLink downloadableLink) {
        if (map.containsKey(downloadableLink.getUrl())){
            return false;
        }
        map.put(downloadableLink.getUrl(), downloadableLink);
        return true;
    }

    @Override
    public boolean deleteDownloadableLink(String url) {
        return map.remove(url) != null;
    }

    @Override
    public DownloadableLink getDownloadableLink() {
        if (map.values().size() > 0) {
            return map.values().iterator().next();
        } else {
            return null;
        }
    }
}
