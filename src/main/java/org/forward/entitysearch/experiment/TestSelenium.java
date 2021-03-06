package org.forward.entitysearch.experiment;

import org.forward.entitysearch.AnnotationProperties;
import org.forward.entitysearch.ingestion.HTMLDocumentIngestionManager;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class TestSelenium {

    public static ArrayList<String> LIST_OF_TAGS_CREATING_NEW_LINES = HTMLDocumentIngestionManager.LIST_OF_TAGS_CREATING_NEW_LINES;

    private static int travelDOMTreeWithSelenium(RemoteWebElement e, WebDriver driver, StringBuilder sb) {
        // String tagName = e.getTagName();
        // System.out.println(tagName);
        int count = 1; // count itself
        if (e.isDisplayed()) {
            try {
                // getrect doesn't work. Don't know why
                //sb.append("[" + e.getTagName() + " " + e.getRect().getX() + " " + e.getRect().getY() + " " + e.getRect().getHeight() + " " + e.getRect().getWidth() + "]");
                sb.append("[" + e.getTagName() + " " + e.getLocation().getX() + " " + e.getLocation().getY() + "]");
                //sb.append("[" + e.getTagName() + " " + e.getSize().getHeight() + " " + e.getSize().getWidth() + "]");
            } catch (Exception err) {
                System.err.println(e.getTagName());
            }
            List<Object> children = (List<Object>) ((ChromeDriver) driver).executeScript("return arguments[0].childNodes;", e);
            for (int i = 0; i < children.size(); i++) {
                if (children.get(i) instanceof RemoteWebElement) {
                    count += travelDOMTreeWithSelenium((RemoteWebElement) children.get(i), driver, sb);
                } else {
                    String nodeName = (String) ((ChromeDriver) driver).executeScript("return arguments[0].childNodes[arguments[1]].nodeName;", e, i);
                    if (nodeName.equals("#text")) {
                        String txt = (String) ((ChromeDriver) driver).executeScript("return arguments[0].childNodes[arguments[1]].nodeValue;", e, i);
                        if (txt.length() > 0) {
                            sb.append(txt);
                        }
                    }
                }
            }
        }
        // put it out side of the if above because
        // although <br/> is not displayed, we still add new line when seeing it
        if (LIST_OF_TAGS_CREATING_NEW_LINES.contains(e.getTagName())){
            sb.append("\n");
        }
        return count;
    }

    public static void main(String[] args) {

        System.setProperty("webdriver.chrome.driver",AnnotationProperties.getInstance().getProperty("selenium.chromedriver"));
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // add this if wanting Chrome to be headless.
        WebDriver driver = new ChromeDriver(options);

        String baseUrl = "http://www.forwarddatalab.org/kevinchang";
        baseUrl = "https://ece.illinois.edu/directory/profile/carney%3D";
        baseUrl = "http://ece.illinois.edu";
        baseUrl = "http://www.ece.illinois.edu/academics/courses/profile/ECE487&secM";

//        baseUrl = "https://charm.cs.illinois.edu/";
//        baseUrl = "http://codingspectator.cs.illinois.edu/updates/helios/";
//        baseUrl = "http://nlp.cs.illinois.edu/HockenmaierGroup/8k-pictures.html";
//        baseUrl = "http://hockenmaier.cs.illinois.edu/DenotationGraph/graph/index10.html";
//        baseUrl = "http://hockenmaier.cs.illinois.edu/DenotationGraph/graph/index100.html";
//        WebDriverWait wait = new WebDriverWait(driver, 10);
//        wait.until(new ExpectedCondition<Boolean>() {
//            @Override
//            public Boolean apply(WebDriver webDriver) {
//                return driver.findElement(By.xpath("/html/body")).getText().length() != 0;
//            }
//        });
        // launch browser and direct it to the Base URL
        driver.get(baseUrl);
        System.out.println(driver.getCurrentUrl());
        System.out.println(driver.getPageSource());
        System.exit(0);

        // get the title and print it
        String pageTitle = null;
        try{
            pageTitle = driver.getTitle();
        } catch (Exception e) {
            System.out.println("Get errors in " + baseUrl);
            System.out.println(e.getCause());
        }

        System.out.println(pageTitle);

        // List<WebElement> el = driver.findElements(By.cssSelector("*"));
        // It is not working because it will miss text nodes
        // System.out.println(((RemoteWebElement) tmp).getAttribute("innerHTML"));
        // String manipulation for the inner HTML or inner Text will be very fragile
        // System.out.println(((RemoteWebElement) tmp).findElement(By.xpath(".//*/text()")));
        // It is not working because it will throw an error for returned Object is not a Web Element

        // Object tmp = ((ChromeDriver) driver).executeScript("return document.querySelector('.sites-layout-tile h2 font');");
        // The script above is useful to directly get DOM node from selenium

        StringBuilder sb2 = new StringBuilder();
        try {
            System.out.println(travelDOMTreeWithSelenium((RemoteWebElement)driver.findElement(By.xpath("/html/body")), driver, sb2));
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("Hello");
        System.out.println(sb2.toString().replaceAll("\n\n+","\n"));

//        The method below won't work because it will miss the text nodes
//        StringBuilder sb = new StringBuilder();
//        List<WebElement> blacklist = new ArrayList<>();
//        List<WebElement> el = driver.findElements(By.cssSelector("*"));
//        for ( int i = el.size()-1; i >=0; i-- ) {
//            WebElement e = el.get(i);
//            if (e.isDisplayed() &&
//                    (blacklist.contains(e) || e.getText().trim().length() > 0) &&
//                    !e.getTagName().equals("html")) {
//                System.out.println(e.getTagName());
//                blacklist.add(e.findElement(By.xpath("..")));
//            }
//        }
//
//        boolean printNewLine = false;
//        for ( WebElement e : el ) {
//            if (e.isDisplayed()) {
//                String txt = e.getText().trim();
//                if (!blacklist.contains(e) && txt.length() > 0) {
//                    sb.append(txt);
//                    printNewLine = true;
//                }
//                if (printNewLine && LIST_OF_TAGS_CREATING_NEW_LINES.contains(e.getTagName())){
//                    sb.append("\n");
//                    printNewLine = false;
//                }
//            }
//        }
//
//        System.out.println("Done");
//        System.out.println(sb.toString());

        //close browser
        driver.close();
    }
}
