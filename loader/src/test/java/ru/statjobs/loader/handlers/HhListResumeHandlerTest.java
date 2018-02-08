package ru.statjobs.loader.handlers;

import org.junit.Assert;
import org.junit.Test;

import java.util.Calendar;

public class HhListResumeHandlerTest {

    HhListResumeHandler hhListResumeHandler = new HhListResumeHandler(null, null, null, null);

    // yyyy-MM-dd HH:mm:ss
    @Test
    public void formatDateTest() {
        int crntYear = Calendar.getInstance().get(Calendar.YEAR);
        Assert.assertEquals(crntYear + "-01-01 00:00:00", hhListResumeHandler.formatDate("Обновлено 1 января, 00:00"));
        Assert.assertEquals(crntYear - 1 + "-12-31 23:59:00", hhListResumeHandler.formatDate("Обновлено 31 декабря, 23:59"));
    }

}
