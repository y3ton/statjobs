package ru.statjobs.loader;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.HashMap;

public class SeleniumBrowser {

    private WebDriver driver;

    private boolean isStart = false;

    private final String pathDriver;
    private final boolean headless;
    private final boolean disableImg;

    public SeleniumBrowser(String pathDriver, boolean headless, boolean disableImg) {
        this.pathDriver = pathDriver;
        this.headless = headless;
        this.disableImg = disableImg;
    }

    public void start() {
        if (!isStart) {
            System.setProperty("webdriver.chrome.driver", pathDriver);
            ChromeOptions chromeOptions = new ChromeOptions();
            if (disableImg) {
                HashMap<String, Object> prefs = new HashMap<String, Object>();
                prefs.put("profile.managed_default_content_settings.images", 2);
                chromeOptions.setExperimentalOption("prefs", prefs);
            }
            if (headless) {
                chromeOptions.setHeadless(true);
            }
            driver = new ChromeDriver(chromeOptions);
            isStart = true;
        }
    }

    public boolean isStart() {
        return isStart;
    }

    public void get(String url) {
        // todo try catch and timeout
        driver.get(url);
    }

    public Object execJs(String jsScript)  {
        JavascriptExecutor javascriptExecutor;
        if (driver instanceof JavascriptExecutor) {
            javascriptExecutor = (JavascriptExecutor)driver;
        } else {
            throw new IllegalStateException("This driver does not support JavaScript");
        }
        return javascriptExecutor.executeScript(jsScript);
    }

    public void close() {
        if (isStart) {
            driver.close();
            isStart = false;
        }
    }




}
