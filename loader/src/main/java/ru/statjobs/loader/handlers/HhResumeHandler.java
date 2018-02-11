package ru.statjobs.loader.handlers;

import org.apache.commons.lang3.StringUtils;
import ru.statjobs.loader.Const;
import ru.statjobs.loader.JsScript;
import ru.statjobs.loader.SeleniumBrowser;
import ru.statjobs.loader.dao.QueueDownloadableLinkDao;
import ru.statjobs.loader.dao.RawDataStorageDao;
import ru.statjobs.loader.dto.DownloadableLink;

public class HhResumeHandler implements LinkHandler{

    private final SeleniumBrowser seleniumBrowser;
    private final JsScript jsScript;
    private final RawDataStorageDao rawDataStorageDao;
    private final QueueDownloadableLinkDao queueDownloadableLinkDao;

    public HhResumeHandler(SeleniumBrowser seleniumBrowser, JsScript jsScript, RawDataStorageDao rawDataStorageDao, QueueDownloadableLinkDao queueDownloadableLinkDao) {
        this.seleniumBrowser = seleniumBrowser;
        this.jsScript = jsScript;
        this.rawDataStorageDao = rawDataStorageDao;
        this.queueDownloadableLinkDao = queueDownloadableLinkDao;
    }

    @Override
    public void process(DownloadableLink link) {
        if (!seleniumBrowser.isStart()) {
            seleniumBrowser.start();
        }
        seleniumBrowser.get(link.getUrl());
        String dateCreate = link.getProps().get(Const.DATE_CREATE_RESUME);
        String dateCreateJson = String.format("\"%s\":\"%s\"", Const.DATE_CREATE_RESUME, dateCreate);
        String cityCode = link.getProps().get(Const.AREA_CODE);
        String cityCodeJson = String.format("\"%s\":\"%s\"", Const.AREA_CODE, cityCode);
        String json = (String) seleniumBrowser.execJs(jsScript.getResume());
        json = "{" + dateCreateJson + "," +  cityCodeJson + "," + json.substring(1);
        if (StringUtils.isBlank(json)) {
            throw new RuntimeException("fail load resume, result is empty " + link.getUrl());
        }
        rawDataStorageDao.saveHhResume(link, json);
        queueDownloadableLinkDao.deleteDownloadableLink(link);

    }
}
