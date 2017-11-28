package com.cv4j.piccrawler.selenium;

import com.safframework.tony.common.utils.IOUtils;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.File;
import java.io.IOException;

/**
 * Created by tony on 2017/11/28.
 */
public class Utils {

    static {

        System.setProperty("webdriver.chrome.driver", "crawler-selenium/chromedriver");
    }

    public static void getScreenshot(String url) {

        //启动chrome实例
        WebDriver driver = new ChromeDriver();
        driver.get(url);
        //指定了OutputType.FILE做为参数传递给getScreenshotAs()方法，其含义是将截取的屏幕以文件形式返回。
        File srcFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
        //利用IOUtils工具类的copyFile()方法保存getScreenshotAs()返回的文件对象。

        try {
            IOUtils.copyFile(srcFile, new File("screenshot.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        //关闭浏览器
        driver.quit();
    }

    /**
     * 浏览器向下滚动
     * @param driver
     */
    public static void scrollDown(WebDriver driver) {

        JavascriptExecutor js = (JavascriptExecutor)driver;
        js.executeScript("scrollTo(0,10000)");
    }
}
