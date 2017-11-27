package ru.statjobs.loader.dao;

import java.sql.*;

public class RawDataStorageDaoImpl implements RawDataStorageDao {

    private final Connection connection;

    public RawDataStorageDaoImpl(Connection connection) {
        this.connection = connection;
    }

    @Override
    public void saveHhVacancy(String url, String json) {
        String sql ="INSERT INTO \"T_HH_RAW_VACANCIES\"(\"URL\", \"DATA\", \"DATE_CREATE\") VALUES (?, ?::JSON, to_timestamp(?))";
        try (PreparedStatement preparedStatement = connection.prepareStatement (sql)) {
            preparedStatement.setString(1, url);
            preparedStatement.setObject(2, json);
            preparedStatement.setLong(3, System.currentTimeMillis() / 1000);
            preparedStatement.executeUpdate();
        }
         catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
