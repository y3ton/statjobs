package ru.statjobs.loader.handlers;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.statjobs.loader.ClientConsts;
import ru.statjobs.loader.JsScript;
import ru.statjobs.loader.SeleniumBrowser;
import ru.statjobs.loader.common.dao.DownloadableLinkDao;
import ru.statjobs.loader.common.dao.RawDataStorageDao;
import ru.statjobs.loader.common.dto.DownloadableLink;

public class HhResumeHandler implements LinkHandler{

    private static final Logger LOGGER = LoggerFactory.getLogger(HhResumeHandler.class);

    private final SeleniumBrowser seleniumBrowser;
    private final JsScript jsScript;
    private final RawDataStorageDao rawDataStorageDao;
    private final DownloadableLinkDao downloadableLinkDao;

    public HhResumeHandler(SeleniumBrowser seleniumBrowser, JsScript jsScript, RawDataStorageDao rawDataStorageDao, DownloadableLinkDao downloadableLinkDao) {
        this.seleniumBrowser = seleniumBrowser;
        this.jsScript = jsScript;
        this.rawDataStorageDao = rawDataStorageDao;
        this.downloadableLinkDao = downloadableLinkDao;
    }

    @Override
    public void process(DownloadableLink link) {
        LOGGER.debug("parse resume url {}", link.getUrl());
        if (!seleniumBrowser.isStart()) {
            seleniumBrowser.start();
        }
        String dateCreate = link.getProps().get(ClientConsts.DATE_CREATE_RESUME);
        String dateCreateJson = String.format("\"%s\":\"%s\"", ClientConsts.DATE_CREATE_RESUME, dateCreate);
        String cityCode = link.getProps().get(ClientConsts.AREA_CODE);
        String cityCodeJson = String.format("\"%s\":\"%s\"", ClientConsts.AREA_CODE, cityCode);

        long getUrlStartTime = System.currentTimeMillis();
        seleniumBrowser.get(link.getUrl());
        String json = (String) seleniumBrowser.execJs(jsScript.getResume());
        LOGGER.debug("resume parse successful. elapsed time: {}", System.currentTimeMillis() - getUrlStartTime);
        if (StringUtils.isBlank(json)) {
            throw new RuntimeException("fail load resume, result is empty " + link.getUrl());
        }
        json = "{" + dateCreateJson + "," +  cityCodeJson + "," + json.substring(1);
        rawDataStorageDao.saveHhResume(link, json);
        downloadableLinkDao.deleteDownloadableLink(link);

    }
}
