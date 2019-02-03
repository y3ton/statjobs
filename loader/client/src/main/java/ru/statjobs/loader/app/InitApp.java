package ru.statjobs.loader.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.statjobs.loader.ClientConsts;
import ru.statjobs.loader.Consts;
import ru.statjobs.loader.common.dao.DownloadableLinkDao;
import ru.statjobs.loader.common.dao.HhDictionaryDao;
import ru.statjobs.loader.common.dto.DownloadableLink;
import ru.statjobs.loader.common.dto.HhDictionary;
import ru.statjobs.loader.dao.DownloadableLinkDaoHttpImpl;
import ru.statjobs.loader.dao.HhDictionaryDaoImpl;
import ru.statjobs.loader.dao.HttpUtils;
import ru.statjobs.loader.url.InitUrlCreator;
import ru.statjobs.loader.url.UrlConstructor;
import ru.statjobs.loader.utils.FileUtils;
import ru.statjobs.loader.utils.JsonUtils;
import ru.statjobs.loader.utils.PropertiesUtils;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class InitApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitApp.class);

    private UrlConstructor urlConstructor;
    private JsonUtils jsonUtils;
    HttpUtils httpUtils;
    private DownloadableLinkDao qownloadableLinkDao;

    private HhDictionaryDao hhDictionaryDao;
    private List<HhDictionary> specialization;
    private List<HhDictionary> industries;
    private Map<String, String> cities;
    private Map<String, String> experience;
    private InitUrlCreator initUrlCreator;
    private FileUtils fileUtils;


    public static void main(String[] args) {
        InitApp initApp = new InitApp();
        Properties props = new PropertiesUtils().loadProperties(Consts.PROPERTIES_FILE);
        initApp.process(props);
    }

    private void init( Properties props) {
        urlConstructor = new UrlConstructor();
        jsonUtils = new JsonUtils();
        httpUtils = new HttpUtils();
        fileUtils = new FileUtils();
        qownloadableLinkDao = new DownloadableLinkDaoHttpImpl(
                httpUtils,
                jsonUtils,
                props.getProperty("linksrv"),
                props.getProperty("linksrvkey")
        );
        ((DownloadableLinkDaoHttpImpl)qownloadableLinkDao).start();
        hhDictionaryDao = new HhDictionaryDaoImpl(jsonUtils, fileUtils);
        specialization = hhDictionaryDao.getSpecialization();
        cities = hhDictionaryDao.getCity();
        industries = hhDictionaryDao.getIndustries();
        experience = hhDictionaryDao.getExperience();
        initUrlCreator = new InitUrlCreator();
    }

    private void process(Properties props) {
        init(props);
        int sequenceNum = (int) Instant.now().getEpochSecond();
        LOGGER.info("sequenceNum: {}", sequenceNum);
        List<DownloadableLink> links = new ArrayList<>();
        links.addAll(initUrlCreator.initHhItVacancyLink(urlConstructor, sequenceNum, ClientConsts.HH_PER_PAGE, cities, specialization, experience, industries));
        links.addAll(initUrlCreator.initHhItResumeLink(urlConstructor, sequenceNum, ClientConsts.HH_PER_PAGE, cities, specialization, ClientConsts.HH_SEARCH_PERIOD));
        Collections.shuffle(links);
        qownloadableLinkDao.createDownloadableLinks(links);
        LOGGER.info("create {} link", links.size());
        ((DownloadableLinkDaoHttpImpl)qownloadableLinkDao).stop();
    }


}
