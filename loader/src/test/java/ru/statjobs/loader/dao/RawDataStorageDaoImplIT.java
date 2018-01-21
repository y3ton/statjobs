package ru.statjobs.loader.dao;

import org.junit.*;
import ru.statjobs.loader.dto.DownloadableLink;
import ru.statjobs.loader.testutils.H2Utils;
import ru.statjobs.loader.utils.JsonUtils;

import java.sql.*;
import java.time.Instant;

public class RawDataStorageDaoImplIT {

    private static Connection connection;
    private static RawDataStorageDaoImpl dao;

    JsonUtils jsonUtils = new JsonUtils();

    @BeforeClass
    public static void start() throws SQLException {
        connection = DriverManager.getConnection ("jdbc:h2:mem:test;MODE=PostgreSQL");
        H2Utils.runScript("sql/raw.sql", connection);
        dao = new RawDataStorageDaoImpl(connection, false);
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
        dao.saveHhVacancy(new DownloadableLink("url1", 1, "HANDLER", null), json);
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("select * from T_HH_RAW_VACANCIES");
        Assert.assertTrue(resultSet.next());
        Assert.assertEquals("url1", resultSet.getString("URL"));
        Assert.assertEquals(json, resultSet.getString("DATA"));
        Assert.assertEquals(1, resultSet.getInt("SEQUENCE_NUM"));
        Timestamp timestamp = resultSet.getTimestamp("DATE_CREATE");
        long time = Instant.now().getEpochSecond();
        Assert.assertTrue(time - 1000 < timestamp.getTime() && timestamp.getTime() <= time);
        resultSet.close();
        statement.close();
    }

}
