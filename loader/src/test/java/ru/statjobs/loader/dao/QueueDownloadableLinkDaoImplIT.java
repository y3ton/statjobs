package ru.statjobs.loader.dao;

import org.junit.*;
import ru.statjobs.loader.dto.DownloadableLink;
import ru.statjobs.loader.testutils.H2Utils;
import ru.statjobs.loader.url.UrlHandler;
import ru.statjobs.loader.utils.JsonUtils;

import java.io.FileNotFoundException;
import java.sql.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class QueueDownloadableLinkDaoImplIT {

    private static Connection connection;
    private static QueueDownloadableLinkDaoImpl dao;

    JsonUtils jsonUtils = new JsonUtils();

    @BeforeClass
    public static void start() throws SQLException {
        connection = DriverManager.getConnection ("jdbc:h2:mem:test;MODE=PostgreSQL");
        JsonUtils jsonUtils = new JsonUtils();
        H2Utils.runScript("sql/queue.sql", connection);
        dao = new QueueDownloadableLinkDaoImpl(connection, jsonUtils, false);
    }

    @AfterClass
    public static void stop() throws SQLException {
        connection.close();
    }

    @After
    public void clean() {
        try (Statement  statement = connection.createStatement();){
            statement.execute("delete from T_QUEUE_DOWNLOADABLE_LINK");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void saveAndLoadLinkTest() {
        Map<String, String> props = new HashMap();
        props.put("prop1", "1");
        props.put("prop2", "2");
        Assert.assertTrue(dao.createDownloadableLink(new DownloadableLink("url1", 1, "HANDLER", props)));
        DownloadableLink link1 = dao.getDownloadableLink();
        Assert.assertEquals("url1", link1.getUrl());
        Assert.assertEquals(Integer.valueOf(1), link1.getSequenceNum());
        Assert.assertEquals("HANDLER", link1.getHandlerName());
        Map<String, String> propsResult = link1.getProps();
        Assert.assertEquals(2, propsResult.size());
        Assert.assertEquals ("1", propsResult.get("prop1"));
        Assert.assertEquals ("2", propsResult.get("prop2"));
    }

    @Test
    public void getDownloadableLinkTest() throws SQLException, FileNotFoundException {
        Assert.assertNull(dao.getDownloadableLink());
        Assert.assertTrue(dao.createDownloadableLink(new DownloadableLink("url1", 1, UrlHandler.HH_VACANCY.name(), Collections.EMPTY_MAP)));
        Assert.assertTrue(dao.createDownloadableLink(new DownloadableLink("url2", 1, UrlHandler.HH_VACANCY.name(), null)));
        Assert.assertTrue(dao.createDownloadableLink(new DownloadableLink("url3", 1, UrlHandler.HH_VACANCY.name(), null)));
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select count(*) from T_QUEUE_DOWNLOADABLE_LINK where DATE_PROCESS is null");
        Assert.assertTrue(resultSet.next());
        Assert.assertEquals(3, resultSet.getInt(1));
        resultSet.close();
        statement.close();
        Assert.assertNotNull(dao.getDownloadableLink());
        Assert.assertNotNull(dao.getDownloadableLink());
        Assert.assertNotNull(dao.getDownloadableLink());
        Assert.assertNull(dao.getDownloadableLink());
        // check updateDownloadableLinkDateProcess
        statement = connection.createStatement();
        resultSet = statement.executeQuery("select count(*) from T_QUEUE_DOWNLOADABLE_LINK where DATE_PROCESS is not null");
        Assert.assertTrue(resultSet.next());
        Assert.assertEquals(3, resultSet.getInt(1));
        resultSet.close();
        statement.close();
    }

    @Test
    public void createDownloadableLinkTest() throws SQLException {

        Map<String, String> props = new HashMap();
        props.put("prop1", "1");
        props.put("prop2", "2");
        Assert.assertTrue(
                dao.createDownloadableLink(new DownloadableLink("url", 17, UrlHandler.HH_VACANCY.name(), props)));
        // duplicate check isContainsDownloadableLink
        Assert.assertFalse(
                dao.createDownloadableLink(new DownloadableLink("url", 17, UrlHandler.HH_VACANCY.name(), props)));
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select * from T_QUEUE_DOWNLOADABLE_LINK");
        Assert.assertTrue(resultSet.next());
        //HANDLER_NAME, URL, DATE_CREATE, SEQUENCE_NUM, IS_DELETE
        Assert.assertEquals(UrlHandler.HH_VACANCY.name(), resultSet.getString("HANDLER_NAME"));
        Assert.assertEquals("url", resultSet.getString("URL"));
        Assert.assertEquals(17, resultSet.getInt("SEQUENCE_NUM"));
        Assert.assertNotNull(resultSet.getTimestamp("DATE_CREATE"));
        Assert.assertNull(resultSet.getTimestamp("DATE_PROCESS"));
        Assert.assertFalse(resultSet.getBoolean("IS_DELETE"));
        Map<String, String> map = jsonUtils.readString(resultSet.getString("PROPS"));
        Assert.assertNotNull(map);
        Assert.assertEquals (2, map.size());
        Assert.assertEquals ("1", map.get("prop1"));
        Assert.assertEquals ("2", map.get("prop2"));

        Assert.assertFalse(resultSet.next());
        resultSet.close();
        statement.close();
    }

    @Test
    public void deleteDownloadableLinkTest() throws SQLException, FileNotFoundException {
        DownloadableLink link = new DownloadableLink("url", 1, UrlHandler.HH_VACANCY.name(), null);
        // get empty - null
        Assert.assertNull(dao.getDownloadableLink());
        Assert.assertFalse(dao.deleteDownloadableLink(link));
        // insert - true
        Assert.assertTrue(dao.createDownloadableLink(link));
        // delete not process link
        Assert.assertFalse(dao.deleteDownloadableLink(link));
        // process link
        Assert.assertNotNull(dao.getDownloadableLink());
        Assert.assertTrue(dao.deleteDownloadableLink(link));
        // repeat delete
        Assert.assertFalse(dao.deleteDownloadableLink(link));

        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select * from T_QUEUE_DOWNLOADABLE_LINK");
        Assert.assertTrue(resultSet.next());
        Assert.assertNotNull(resultSet.getTimestamp("DATE_CREATE"));
        Assert.assertNotNull(resultSet.getTimestamp("DATE_PROCESS"));
        Assert.assertTrue(resultSet.getBoolean("IS_DELETE"));
        Assert.assertFalse(resultSet.next());
        resultSet.close();
        statement.close();
    }
}
