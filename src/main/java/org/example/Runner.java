package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.List;

public class Runner {
    static final String link = "https://crm.ukc.loc/crm";
    static ChromeDriver driver;
    static Source[] sources = new Source[3];

    public static void main(String[] args) throws AWTException {
        sources[0] = new Source("phone", "Урядова «гаряча лінія»");
        sources[1] = new Source("site", "Веб-сайт УКЦ");
        sources[2] = new Source("kmu", "Урядовий портал");

        System.out.println("PLEASE, WAIT UNTIL CHROME WILL NOT CLOSE!\n\n");

        if (new File("d:/Users/admin.UKC/Desktop/Reports/!dontDelete_chromedriver/chromedriver.exe").exists())
            System.setProperty("webdriver.chrome.driver", "d:/Users/admin.UKC/Desktop/Reports/!dontDelete_chromedriver/chromedriver.exe");
        else
            System.setProperty("webdriver.chrome.driver", "chromedriver/chromedriver.exe");
        driver = new ChromeDriver();

        setSettings();
        login();
        gotoFilter();

        findSource(sources[0].getName());
        setDate();
        useFilter();
        sleep(1000);
        sources[0].setCount(getCount());

        findSource(sources[0].getName());
        findSource(sources[1].getName());
        useFilter();
        sleep(1000);
        sources[1].setCount(getCount());

        findSource(sources[1].getName());
        findSource(sources[2].getName());
        useFilter();
        sleep(1000);
        sources[2].setCount(getCount());


        writeToFile();

        driver.quit();
    }

    private static void writeToFile() {
        StringWriter writer = new StringWriter();
        for (Source source : sources) {
            writer.write(getXml(source));
        }

        try {
            FileWriter fw = new FileWriter("!!!result.xml");
            fw.write(writer.toString());
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private static String getXml(Source source) {
        String xml = "<" + source.getCode() + ">" + source.getCount() + "</" + source.getCode() + ">";

        return xml + "\n";
    }

    private static String getCount() {
        String data = driver.findElementByXPath("//div[@class='paginateInfo']").getText();
        return data.substring(data.indexOf("(з ")+3, data.lastIndexOf(")"));
    }

    private static void useFilter() {
        driver.findElementByClassName("btn-primary").click();
    }

    private static void setDate() {
        if (driver.findElementByXPath("//input[@name='INTERACTION_START_DATE@TO']").getText().length() == 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy 00:00");
            Calendar cal = Calendar.getInstance();

            driver.findElementByXPath("//input[@name='INTERACTION_START_DATE@TO']").click();
            driver.findElementByXPath("//input[@name='INTERACTION_START_DATE@TO']").sendKeys(sdf.format(cal.getTime()));

            sleep(500);
            cal.add(Calendar.DATE, -1);
            driver.findElementByXPath("//input[@name='INTERACTION_START_DATE@FROM']").click();
            driver.findElementByXPath("//input[@name='INTERACTION_START_DATE@FROM']").sendKeys(sdf.format(cal.getTime()));
        }
    }

    private static void findSource(String source) {
        driver.findElementsByXPath("//button[@class='input-field']").get(0).click();
        for (WebElement element : driver.findElementsByXPath("//span[@class='checkbox-text']")) {
            if (element.getText().contains(source)) element.click();
        }
        driver.findElementsByXPath("//button[@class='input-field']").get(0).click();
    }

    private static void login() {
        for (WebElement element : driver.findElementsByXPath("//input[@class='login-page__form-input']")) {
            switch (element.getAttribute("placeholder")) {
                case "Домен...": element.sendKeys("UKC");
                    break;
                case "Ім’я користувача...": element.sendKeys("admin");
                    break;
                case "Пароль...": element.sendKeys("<thtptym14");
                    break;
            }
        }
    }

    private static void gotoFilter() {
        driver.findElementByXPath("//input[@class='login-page__form-submit']").click();
        driver.findElementByXPath("//button[@class='icon-menu-cross add-tab-button']").click();
        driver.findElementByXPath("//span[@class='new-tab-item-text']").click();
        sleep(1000);
    }

    private static void setSettings() {
        driver.manage().window().maximize();
        driver.get(link);
        System.out.println(driver.getWindowHandles().size());
        if (driver.getWindowHandles().size() > 1) {
            ArrayList<String> tabs = new ArrayList<>(driver.getWindowHandles());
            driver.switchTo().window(tabs.get(1));
        }
    }

    private static void sleep(int ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
