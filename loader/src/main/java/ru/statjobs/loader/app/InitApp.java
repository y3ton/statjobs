package ru.statjobs.loader.app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.statjobs.loader.Const;
import ru.statjobs.loader.dao.DownloadableLinkDao;
import ru.statjobs.loader.dao.DownloadableLinkDaoPostgresImpl;
import ru.statjobs.loader.dao.HhDictionaryDao;
import ru.statjobs.loader.dao.HhDictionaryDaoImpl;
import ru.statjobs.loader.dto.DownloadableLink;
import ru.statjobs.loader.dto.HhDictionary;
import ru.statjobs.loader.url.InitUrlCreator;
import ru.statjobs.loader.url.UrlConstructor;
import ru.statjobs.loader.utils.FileUtils;
import ru.statjobs.loader.utils.JsonUtils;
import ru.statjobs.loader.utils.PropertiesUtils;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class InitApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(InitApp.class);

    private UrlConstructor urlConstructor;
    private JsonUtils jsonUtils;
    private DownloadableLinkDao qownloadableLinkDao;

    private HhDictionaryDao hhDictionaryDao;
    private List<HhDictionary> specialization;
    private List<HhDictionary> industries;
    private Map<String, String> cities;
    private Map<String, String> experience;
    private InitUrlCreator initUrlCreator;
    private FileUtils fileUtils;


    public static void main(String[] args) throws IOException, SQLException {
        InitApp initApp = new InitApp();
        Properties props = new PropertiesUtils().loadProperties(Const.PROPERTIES_FILE);
        initApp.process(props);
    }

    private void init(Connection connection) {
        urlConstructor = new UrlConstructor();
        jsonUtils = new JsonUtils();
        fileUtils = new FileUtils();
        qownloadableLinkDao = new DownloadableLinkDaoPostgresImpl(connection, jsonUtils);
        hhDictionaryDao = new HhDictionaryDaoImpl(jsonUtils, fileUtils);
        specialization = hhDictionaryDao.getSpecialization();
        cities = hhDictionaryDao.getCity();
        industries = hhDictionaryDao.getIndustries();
        experience = hhDictionaryDao.getExperience();
        initUrlCreator = new InitUrlCreator();
    }

    private void process(Properties properties) {
        LOGGER.info("connect to DB. url: {}", properties.getProperty("url"));
        try (Connection connection = DriverManager.getConnection(
                properties.getProperty("url"),
                properties.getProperty("user"),
                properties.getProperty("password")))
        {
            init(connection);
            int sequenceNum = (int) Instant.now().getEpochSecond();
            LOGGER.info("sequenceNum: {}", sequenceNum);
            List<DownloadableLink> firstLink = new ArrayList<>();
            firstLink.addAll(initUrlCreator.initHhItVacancyLink(urlConstructor, sequenceNum, Const.HH_PER_PAGE, cities, specialization, experience, industries));
            firstLink.addAll(initUrlCreator.initHhItResumeLink(urlConstructor, sequenceNum, Const.HH_PER_PAGE, cities, specialization, Const.HH_SEARCH_PERIOD));
            firstLink.forEach(qownloadableLinkDao::createDownloadableLink);
            LOGGER.info("create {} link", firstLink.size());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }


}
