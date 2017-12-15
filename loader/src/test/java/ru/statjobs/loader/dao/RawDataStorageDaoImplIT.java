package ru.statjobs.loader.dao;

import org.junit.Test;
import org.mockito.Mockito;
import ru.statjobs.loader.dto.DownloadableLink;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.mockito.Mockito.*;

public class RawDataStorageDaoImplIT {

    @Test
    public void saveHhVacancyTest() throws SQLException {

        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        RawDataStorageDao dao = new RawDataStorageDaoImpl(connection);
        DownloadableLink link = new DownloadableLink("url", 177, "handler");
        dao.saveHhVacancy(link, "json_1" );

        verify(preparedStatement, Mockito.times(1)).setString( 1, "url");
        verify(preparedStatement, Mockito.times(1)).setObject(2, "json_1");
        verify(preparedStatement, Mockito.times(1)).setLong(anyInt(), anyLong());
        verify(preparedStatement, Mockito.times(1)).setInt(4, 177);
        verify(preparedStatement, Mockito.times(1)).executeUpdate();
    }


}
