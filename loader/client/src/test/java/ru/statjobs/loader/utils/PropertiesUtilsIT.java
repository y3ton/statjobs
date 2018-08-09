package ru.statjobs.loader.utils;

import org.junit.Assert;
import org.junit.Test;
import ru.statjobs.loader.Const;

import java.util.Properties;

public class PropertiesUtilsIT {

    @Test
    public void loadPropertiesTes() {
        PropertiesUtils propertiesUtils = new PropertiesUtils();
        Properties props = propertiesUtils.loadProperties(Const.PROPERTIES_FILE);
        Assert.assertEquals("aaa", props.getProperty("a"));
        Assert.assertEquals("bbb", props.getProperty("b"));
        Assert.assertEquals("password", props.getProperty("password"));
        Assert.assertEquals("user", props.getProperty("user"));
    }
}
