package ru.statjobs.loader.dao;

import org.junit.*;
import ru.statjobs.loader.dto.DownloadableLink;
import ru.statjobs.loader.testutils.H2Utils;

import java.sql.*;

public class RawDataStorageDaoPostgresImplIT {

    private static Connection connection;
    private static RawDataStorageDaoPostgresImpl dao;

    @BeforeClass
    public static void start() throws SQLException {
        connection = DriverManager.getConnection ("jdbc:h2:mem:test;MODE=PostgreSQL");
        H2Utils.runScript("sql/raw.sql", connection);
        dao = new RawDataStorageDaoPostgresImpl(connection, false);
   }

    @AfterClass
    public static void stop() throws SQLException {
        connection.close();
    }

    @After
    public void clean() {
        try (Statement statement = connection.createStatement();){
            statement.execute("delete from T_HH_RAW_VACANCIES");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void getDownloadableLinkTest() throws SQLException {
        String json = "{\"a\":\"b\"}";
        long startTime = System.currentTimeMillis();
        dao.saveHhVacancy(new DownloadableLink("url1", 1, "HANDLER", null), json);
        long endTime = System.currentTimeMillis();
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select * from T_HH_RAW_VACANCIES");
        Assert.assertTrue(resultSet.next());
        Assert.assertEquals("url1", resultSet.getString("URL"));
        Assert.assertEquals(json, resultSet.getString("DATA"));
        Assert.assertEquals(1, resultSet.getInt("SEQUENCE_NUM"));
        Timestamp timestamp = resultSet.getTimestamp("DATE_CREATE");
        Assert.assertTrue(startTime <= timestamp.getTime() && timestamp.getTime() <= endTime);
        resultSet.close();
        statement.close();
    }

}
