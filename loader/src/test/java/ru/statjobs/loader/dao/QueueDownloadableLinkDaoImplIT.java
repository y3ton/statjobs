package ru.statjobs.loader.dao;

import org.junit.*;
import ru.statjobs.loader.UrlHandler;
import ru.statjobs.loader.dto.DownloadableLink;
import ru.statjobs.loader.testutils.H2Utils;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class QueueDownloadableLinkDaoImplIT {

    private static Connection connection;
    private static QueueDownloadableLinkDaoImpl dao;

    @BeforeClass
    public static void start() throws SQLException {
        connection = DriverManager.getConnection ("jdbc:h2:mem:test;MODE=PostgreSQL");
        H2Utils.runScript("sql/queue.sql", connection);
        dao = new QueueDownloadableLinkDaoImpl(connection);
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
    public void simpleInsertTest() throws SQLException, FileNotFoundException {
        Assert.assertNull(dao.getDownloadableLink());
        boolean isInsert =
                dao.createDownloadableLink(new DownloadableLink("url", 1, UrlHandler.HH_VACANCY.name()));
        Assert.assertTrue(isInsert);
        Assert.assertNotNull(dao.getDownloadableLink());
    }
}
