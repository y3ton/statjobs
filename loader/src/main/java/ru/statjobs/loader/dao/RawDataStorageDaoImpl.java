package ru.statjobs.loader.dao;

import ru.statjobs.loader.dto.DownloadableLink;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class RawDataStorageDaoImpl implements RawDataStorageDao {

    private final Connection connection;

    private final boolean postgresMode;

    public RawDataStorageDaoImpl(Connection connection) {
        this(connection, true);
    }

    public RawDataStorageDaoImpl(Connection connection, boolean postgresMode) {
        this.connection = connection;
        this.postgresMode = postgresMode;
    }

    @Override
    public void saveHhVacancy(DownloadableLink link, String json) {
        saveHhData(link, json, "T_HH_RAW_VACANCIES");
    }

    @Override
    public void saveHhResume(DownloadableLink link, String json) {
        saveHhData(link, json, "T_HH_RAW_RESUMES");
    }

    void saveHhData(DownloadableLink link, String json, String tableName) {
        String query = "INSERT INTO " + tableName + "(URL, DATA, DATE_CREATE, SEQUENCE_NUM) VALUES (?, ?::JSON, ?, ?)";
        if (!postgresMode) {
            query = query.replace("?::JSON", "?");
        }
        try (PreparedStatement preparedStatement = connection.prepareStatement (query)) {
            preparedStatement.setString(1, link.getUrl());
            preparedStatement.setObject(2, json);
            preparedStatement.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
            preparedStatement.setInt(4, link.getSequenceNum());
            preparedStatement.executeUpdate();
        }
        catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
