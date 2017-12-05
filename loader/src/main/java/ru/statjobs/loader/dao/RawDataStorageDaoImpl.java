package ru.statjobs.loader.dao;

import ru.statjobs.loader.dto.DownloadableLink;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Instant;

public class RawDataStorageDaoImpl implements RawDataStorageDao {

    private final Connection connection;

    public RawDataStorageDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void saveHhVacancy(DownloadableLink link, String json) {
        String query = "INSERT INTO T_HH_RAW_VACANCIES(URL, DATA, DATE_CREATE, SEQUENCE_NUM) VALUES (?, ?::JSON, to_timestamp(?), ?)";
        try (PreparedStatement preparedStatement = connection.prepareStatement (query)) {
            preparedStatement.setString(1, link.getUrl());
            preparedStatement.setObject(2, json);
            preparedStatement.setLong(3, Instant.now().getEpochSecond());
            preparedStatement.setInt(4, link.getSequenceNum());
            preparedStatement.executeUpdate();
        }
         catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
