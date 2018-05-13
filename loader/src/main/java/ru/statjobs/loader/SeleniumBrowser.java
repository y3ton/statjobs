package ru.statjobs.loader;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

public class SeleniumBrowser {

    private WebDriver driver;

    private boolean isStart = false;
    private final String pathDriver;

    public SeleniumBrowser(String pathDriver) {
        this.pathDriver = pathDriver;
    }

    public void start() {
        if (!isStart) {
            System.setProperty("webdriver.chrome.driver", pathDriver);
            driver = new ChromeDriver();
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
