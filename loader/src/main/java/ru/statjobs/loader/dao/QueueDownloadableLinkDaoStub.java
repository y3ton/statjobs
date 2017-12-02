package ru.statjobs.loader.dao;

import ru.statjobs.loader.dto.DownloadableLink;

import java.util.HashMap;
import java.util.Map;

public class QueueDownloadableLinkDaoStub implements QueueDownloadableLinkDao  {

    private Map<String, DownloadableLink> map = new HashMap<>();

    @Override
    public boolean createDownloadableLink(DownloadableLink link) {
        if (map.containsKey(link.getUrl())){
            return false;
        }
        map.put(link.getUrl(), link);
        return true;
    }

    @Override
    public boolean deleteDownloadableLink(DownloadableLink link) {
        return map.remove(link.getUrl()) != null;
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
