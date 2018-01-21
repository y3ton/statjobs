package ru.statjobs.loader.utils;

import org.junit.Assert;
import org.junit.Test;

public class FileUtilsIT {

    FileUtils fileUtils = new FileUtils();
    String RESOURCE_FILE_NAME = "resourceFile.txt";

    @Test
    public void getResourceFileTet() {
        Assert.assertTrue(fileUtils.getResourceFile(RESOURCE_FILE_NAME).endsWith(RESOURCE_FILE_NAME));
    }

    @Test
    public void readFileTest() {
        Assert.assertEquals("123", fileUtils.readFile(fileUtils.getResourceFile(RESOURCE_FILE_NAME)));
    }

    @Test
    public void readResourceFileTet() {
        Assert.assertEquals("123", fileUtils.readResourceFile(RESOURCE_FILE_NAME));
    }

}
