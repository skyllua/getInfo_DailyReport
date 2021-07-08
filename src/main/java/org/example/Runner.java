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
    static final String linkGrafanaLocal = "http://192.168.15.182:3000/d/51PtXZ1Gk/mobileoperators?orgId=1&from=now-1d%2Fd&to=now-1d%2Fd";
    static final String linkGrafanaGlobal = "https://grafana.ukc.gov.ua/d/51PtXZ1Gk/mobileoperators?orgId=1&from=now-1d%2Fd&to=now-1d%2Fd";
    static ChromeDriver driver;
    static ArrayList<Source> requests = new ArrayList<>();
    static ArrayList<Source> calls = new ArrayList<>();
    static boolean isAdminPC = false;

    public static void main(String[] args) throws AWTException {
        requests.add(new Source("phone", "Урядова «гаряча лінія»"));
        requests.add(new Source("site", "Веб-сайт УКЦ"));
        requests.add(new Source("kmu", "Урядовий портал"));

        calls.add(new Source("Kyivstar", "Киевстар"));
        calls.add(new Source("Vodafone", "Водафон"));
        calls.add(new Source("Lifecell", "Лайф"));
        calls.add(new Source("Ukrtelecom", "Укртелеком"));

        System.out.println("PLEASE, WAIT UNTIL CHROME WILL NOT CLOSE!\n\n");

        if (new File("d:/Users/admin.UKC/Desktop/Reports/!dontDelete_chromedriver/chromedriver.exe").exists()) {
            isAdminPC = true;
            System.setProperty("webdriver.chrome.driver", "d:/Users/admin.UKC/Desktop/Reports/!dontDelete_chromedriver/chromedriver.exe");
        } else
            System.setProperty("webdriver.chrome.driver", "chromedriver/chromedriver.exe");
        driver = new ChromeDriver();

        getCountRequests();
        getCountCalls();

        writeToFile();

        driver.quit();
    }

    private static void getCountCalls() {
        if (isAdminPC) driver.get(linkGrafanaLocal);
        else driver.get(linkGrafanaGlobal);

        loginIntoGrafana();
        getCallsData();
    }

    private static void getCallsData() {
        for (WebElement element : driver.findElementsByXPath("//div[@class='panel-container']")) {
            String[] data = element.getText().split("\n");

            for (Source call : calls) {
                if (call.getCode().equals(data[0])) {
                    System.out.println(data[0] + ": " + data[1]);
                    call.setCount(data[1]);
                }
            }
        }
    }

    private static void loginIntoGrafana() {
        driver.findElementByClassName("login-form-input").sendKeys("adminUKC");
        driver.findElementByXPath("//input[@type='password']").sendKeys("P@ssw0rd");
        driver.findElementByXPath("//button[@aria-label='Login button']").click();

        boolean isLoaded = false;
        sleep(500);

        while (!isLoaded) {
            for (WebElement element : driver.findElementsByXPath("//div[@class='panel-container']")) {
                if (element.getText().split("\n")[0].equals("Kyivstar")) isLoaded = true;
            }
        }

        sleep(1000);
    }

    private static void getCountRequests() {
        setSettings();
        login();
        gotoFilter();

        findSource(requests.get(0).getName());
        setDate();
        useFilter();
        sleep(1000);
        requests.get(0).setCount(getCount());

        findSource(requests.get(0).getName());
        findSource(requests.get(1).getName());
        useFilter();
        sleep(1000);
        requests.get(1).setCount(getCount());

        findSource(requests.get(1).getName());
        findSource(requests.get(2).getName());
        useFilter();
        sleep(1000);
        requests.get(2).setCount(getCount());
    }

    private static void writeToFile() {
        StringWriter writer = new StringWriter();
        for (Source source : requests) {
            writer.write(getXml(source));
        }

        for (Source source : calls) {
            writer.write(getXml(source));
        }

        try {
            FileWriter fw = new FileWriter("!!!result.xml");
            if (isAdminPC) fw = new FileWriter("d:/Users/admin.UKC/Desktop/Reports/!!!result.xml");
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
