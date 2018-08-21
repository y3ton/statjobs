package ru.statjobs.loader.dao;

import org.junit.Test;
import org.mockito.Mockito;
import ru.statjobs.loader.common.dao.RawDataStorageDao;
import ru.statjobs.loader.common.dto.DownloadableLink;
import ru.statjobs.loader.common.url.UrlTypes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class RawDataStorageDaoPostgresImplTest {

    @Test
    public void saveHhRawTest() throws SQLException {

        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);

        RawDataStorageDao dao = new RawDataStorageDaoPostgresImpl(connection);
        DownloadableLink link = new DownloadableLink("url", 177, UrlTypes.HH_RESUME, null);

        dao.saveHhVacancy(link, "json_1" );
        verify(preparedStatement, Mockito.times(1)).setString( 1, "url");
        verify(preparedStatement, Mockito.times(1)).setObject(2, "json_1");
        verify(preparedStatement, Mockito.times(1)).setTimestamp(anyInt(), any());
        verify(preparedStatement, Mockito.times(1)).setInt(4, 177);
        verify(preparedStatement, Mockito.times(1)).executeUpdate();

        dao.saveHhResume(link, "json_1" );
        verify(preparedStatement, Mockito.times(2)).setString( 1, "url");
        verify(preparedStatement, Mockito.times(2)).setObject(2, "json_1");
        verify(preparedStatement, Mockito.times(2)).setTimestamp(anyInt(), any());
        verify(preparedStatement, Mockito.times(2)).setInt(4, 177);
        verify(preparedStatement, Mockito.times(2)).executeUpdate();
    }

}
