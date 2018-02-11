package ru.statjobs.loader.handlers;

import ru.statjobs.loader.*;
import ru.statjobs.loader.dao.QueueDownloadableLinkDao;
import ru.statjobs.loader.dto.DownloadableLink;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;


public class HhListResumeHandler implements  LinkHandler  {

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
        if (!seleniumBrowser.isStart()) {
            seleniumBrowser.start();
        }
        seleniumBrowser.get(link.getUrl());
        List<List<String>> list = (List<List<String>>) seleniumBrowser.execJs(jsScript.getResumeList());

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
