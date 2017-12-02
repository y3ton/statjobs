package ru.statjobs.loader.dao;


import org.javatuples.Pair;
import ru.statjobs.loader.dto.DownloadableLink;

import java.sql.*;

public class QueueDownloadableLinkDaoImpl implements QueueDownloadableLinkDao {

    public final static int MAX_ATTEMPT_GET_LINK = 10;

    private final Connection connection;

    public QueueDownloadableLinkDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public boolean createDownloadableLink(DownloadableLink link) {
        String query =
                "insert into \"T_QUEUE_DOWNLOADABLE_LINK\"(\"HANDLER_NAME\", \"URL\", \"DATE_CREATE\", \"SEQUENCE_NUM\") " +
                "values (?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement (query)) {
            preparedStatement.setString(1, link.getHandlerName());
            preparedStatement.setString(2, link.getUrl());
            preparedStatement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            preparedStatement.setInt(4, link.getSequenceNum());
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean deleteDownloadableLink(DownloadableLink link) {
        String query = "delete from \"T_QUEUE_DOWNLOADABLE_LINK\" WHERE \"URL\" = ? and \"DATE_PROCESS\" is not null";
        try (PreparedStatement preparedStatement = connection.prepareStatement (query)) {
            preparedStatement.setString(1, link.getUrl());
            return preparedStatement.executeUpdate() > 0;
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DownloadableLink getDownloadableLink() {
        for (int i = 0; i < MAX_ATTEMPT_GET_LINK; i++) {
            Pair<Integer, DownloadableLink> pair = selectRandomDownloadableLink();
            if (pair == null) {
                return null;
            }
            if (updateDownloadableLinkDateProcess(pair.getValue0(), new Timestamp(System.currentTimeMillis()))) {
                return pair.getValue1();
            }
        }
        throw new RuntimeException("Fail load downloadable link");
    }

    private boolean updateDownloadableLinkDateProcess(Integer id, Timestamp dateProcess) {
        String query = "UPDATE \"T_QUEUE_DOWNLOADABLE_LINK\" SET \"DATE_PROCESS\"=? WHERE \"ID\"=? and \"DATE_PROCESS\" is null";
        try (PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setTimestamp(1, dateProcess);
            preparedStatement.setInt(2, id);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Pair<Integer, DownloadableLink> selectRandomDownloadableLink() {
        String query = "select \"ID\", \"HANDLER_NAME\", \"URL\", \"SEQUENCE_NUM\" " +
                "from \"T_QUEUE_DOWNLOADABLE_LINK\" " +
                "where \"DATE_PROCESS\" is null " +
                "order by random() limit 1";
        try (
                PreparedStatement preparedStatement = connection.prepareStatement(query);
                ResultSet resultSet = preparedStatement.executeQuery())
        {
            if (!resultSet.next()) {
                return null;
            }
            Integer id = resultSet.getInt("ID");
            DownloadableLink downloadableLink = new DownloadableLink(
                    resultSet.getString("URL"),
                    resultSet.getInt("SEQUENCE_NUM"),
                    resultSet.getString("HANDLER_NAME")
            );
            return new Pair<>(id, downloadableLink);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
