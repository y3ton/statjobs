package ru.statjobs.loader.linksrv;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.statjobs.loader.common.dao.DownloadableLinkDao;
import ru.statjobs.loader.common.dto.DownloadableLink;
import ru.statjobs.loader.linksrv.dao.RedisMap;
import ru.statjobs.loader.linksrv.dao.RedisQueue;
import ru.statjobs.loader.utils.JsonUtils;

@Service
public class LinkSrvController implements DownloadableLinkDao {

    private final RedisMap redisMap;

    private final RedisQueue redisQueue;

    private final JsonUtils jsonUtils;

    public static final String QUEUE_NAME = "outLinks";

    public LinkSrvController(
            @Autowired RedisMap redisMap,
            @Autowired RedisQueue redisQueue,
            @Autowired JsonUtils jsonUtils
    ) {
        this.redisMap = redisMap;
        this.redisQueue = redisQueue;
        this.jsonUtils = jsonUtils;
    }


    @Override
    public boolean createDownloadableLink(DownloadableLink link) {
        String json = jsonUtils.createString(link);
        if (redisMap.setIfNotExists(createHash(link), "")) {
            redisMap.set(createProceesLinkHash(link), json);
            redisQueue.push(QUEUE_NAME, json);
        }
        return true;
    }

    @Override
    public boolean deleteDownloadableLink(DownloadableLink link) {
        return redisMap.del(createProceesLinkHash(link)) != 0;
    }

    @Override
    public DownloadableLink getDownloadableLink() {
        String json = redisQueue.pop(QUEUE_NAME);
        return  json == null ? null : jsonUtils.readString(json, DownloadableLink.class);
    }

    public static String createHash(DownloadableLink link) {
        return "C:" + link.getSequenceNum() + ":" + link.getUrl();
    }

    public static String createProceesLinkHash(DownloadableLink link) {
        return "P:" + link.getSequenceNum() + ":" + link.getUrl();
    }

}
