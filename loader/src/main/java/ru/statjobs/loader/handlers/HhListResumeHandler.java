package ru.statjobs.loader.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.statjobs.loader.Const;
import ru.statjobs.loader.JsScript;
import ru.statjobs.loader.SeleniumBrowser;
import ru.statjobs.loader.dao.QueueDownloadableLinkDao;
import ru.statjobs.loader.dto.DownloadableLink;
import ru.statjobs.loader.url.UrlConstructor;
import ru.statjobs.loader.url.UrlHandler;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


public class HhListResumeHandler implements  LinkHandler  {

    private static final Logger LOGGER = LoggerFactory.getLogger(HhListResumeHandler.class);

    public final DateFormat jsFormat = new SimpleDateFormat("Обновлено dd MMMM, HH:mm", new Locale("ru"));
    public final DateFormat outputFormat = new SimpleDateFormat(Const.DATE_FORMAT, new Locale("ru"));

    private final SeleniumBrowser seleniumBrowser;
    private final JsScript jsScript;
    private final QueueDownloadableLinkDao queueDownloadableLinkDao;
    private final UrlConstructor urlConstructor;


    public HhListResumeHandler(SeleniumBrowser seleniumBrowser, JsScript jsScript, QueueDownloadableLinkDao queueDownloadableLinkDao, UrlConstructor urlConstructor) {
        this.seleniumBrowser = seleniumBrowser;
        this.jsScript = jsScript;
        this.queueDownloadableLinkDao = queueDownloadableLinkDao;
        this.urlConstructor = urlConstructor;
    }

    @Override
    public void process(DownloadableLink link) {
        LOGGER.debug("parse resume list url {}", link.getUrl());
        if (!seleniumBrowser.isStart()) {
            seleniumBrowser.start();
        }
        long getUrlStartTime = System.currentTimeMillis();
        seleniumBrowser.get(link.getUrl());
        List<List<String>> list = (List<List<String>>) seleniumBrowser.execJs(jsScript.getResumeList());
        LOGGER.debug("url parse successful. Find {} resumes; elapsed time: {}", list.size(), System.currentTimeMillis() - getUrlStartTime);
        list.stream()
                .map(list2 -> {
                    Map<String, String> props = new HashMap();
                    props.put(Const.DATE_CREATE_RESUME, formatDate(list2.get(1)));
                    props.put(Const.AREA_CODE, urlConstructor.getParameter(Const.AREA_CODE, link.getUrl()));
                    return new DownloadableLink(
                            list2.get(0),
                            link.getSequenceNum(),
                            UrlHandler.HH_RESUME.name(),
                            props);
                })
                .forEach(queueDownloadableLinkDao::createDownloadableLink);

        long i = (long) seleniumBrowser.execJs("return document.querySelectorAll('[data-qa=\"pager-next\"]').length");
        if (i > 0) {
            queueDownloadableLinkDao.createDownloadableLink(
                        new DownloadableLink(
                            urlConstructor.hhUrlNextPage(link.getUrl()),
                            link.getSequenceNum(),
                            UrlHandler.HH_LIST_RESUME.name(),
                            null
                        )
            );
        }
        queueDownloadableLinkDao.deleteDownloadableLink(link);

    }

    String formatDate(String date) {
        Date parseDate;
        try {
            parseDate = jsFormat.parse(date);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        calendar.setTime(parseDate);
        calendar.set(Calendar.YEAR, year);
        if (calendar.getTime().getTime() > System.currentTimeMillis()) {
            calendar.set(Calendar.YEAR, year - 1);
        }
        return outputFormat.format(calendar.getTime());
    }

}
