package ru.statjobs.loader.utils;

import org.apache.commons.io.IOUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

public class Downloader {

    public static class DownloaderResult {

        private final Integer responseCode;
        private final String text;

        public DownloaderResult(Integer responseCode, String text) {
            this.responseCode = responseCode;
            this.text = text;
        }

        public Integer getResponseCode() {
            return responseCode;
        }

        public String getText() {
            return text;
        }
    }

    public DownloaderResult download(String url, Charset codePage, int timeOut) {
        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setConnectTimeout(timeOut);
            connection.setReadTimeout(timeOut);
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                return new DownloaderResult(responseCode, "");
            }
            try (InputStream is = connection.getInputStream()) {
                return new DownloaderResult(responseCode, IOUtils.toString(new BufferedInputStream(is), codePage));
            }
        } catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }

}
