package org.forward.entitysearch.ingestion;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CustomizableCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import org.forward.entitysearch.AnnotationProperties;
import org.forward.entitysearch.experiment.AnnotatorFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebElement;

import java.util.ArrayList;
import java.util.List;

public class HTMLDocumentIngestionManager {

    public static ArrayList<String> LIST_OF_TAGS_CREATING_NEW_LINES = new ArrayList<>();

    static {
        LIST_OF_TAGS_CREATING_NEW_LINES.add("div");
        LIST_OF_TAGS_CREATING_NEW_LINES.add("p");
        LIST_OF_TAGS_CREATING_NEW_LINES.add("br");
        LIST_OF_TAGS_CREATING_NEW_LINES.add("table");
        LIST_OF_TAGS_CREATING_NEW_LINES.add("th");
        LIST_OF_TAGS_CREATING_NEW_LINES.add("tr");
        LIST_OF_TAGS_CREATING_NEW_LINES.add("td");
        LIST_OF_TAGS_CREATING_NEW_LINES.add("li");
        LIST_OF_TAGS_CREATING_NEW_LINES.add("ul");
        LIST_OF_TAGS_CREATING_NEW_LINES.add("h1");
        LIST_OF_TAGS_CREATING_NEW_LINES.add("h2");
        LIST_OF_TAGS_CREATING_NEW_LINES.add("h3");
        LIST_OF_TAGS_CREATING_NEW_LINES.add("h4");
        LIST_OF_TAGS_CREATING_NEW_LINES.add("h5");
        LIST_OF_TAGS_CREATING_NEW_LINES.add("h6");

        System.setProperty("webdriver.chrome.driver",AnnotationProperties.getInstance().getProperty("selenium.chromedriver"));
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // add this if wanting Chrome to be headless.
        driver = new ChromeDriver(options);
    }

    private static int travelDOMTreeWithSelenium(RemoteWebElement e, StringBuilder sb) {
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
                    count += travelDOMTreeWithSelenium((RemoteWebElement) children.get(i), sb);
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

    private static void travelDOMTreeWithSelenium(RemoteWebElement e, Rectangle r, List<CoreLabel> allTokens) {
        if (e.isDisplayed()) {
            Rectangle rec = null;
            try {
                rec = new Rectangle(e.getLocation().getX(), e.getLocation().getY(), e.getSize().getHeight(), e.getSize().getWidth());
            } catch (Exception err) {
                System.err.println("Thee is no layout info in this element: " + e.getTagName());
            }
            if (rec == null) {
                rec = r;
            }
            List<Object> children = (List<Object>) ((ChromeDriver) driver).executeScript("return arguments[0].childNodes;", e);
            for (int i = 0; i < children.size(); i++) {
                if (children.get(i) instanceof RemoteWebElement) {
                    travelDOMTreeWithSelenium((RemoteWebElement) children.get(i), rec, allTokens);
                } else {
                    String nodeName = (String) ((ChromeDriver) driver).executeScript("return arguments[0].childNodes[arguments[1]].nodeName;", e, i);
                    if (nodeName.equals("#text")) {
                        String txt = (String) ((ChromeDriver) driver).executeScript("return arguments[0].childNodes[arguments[1]].nodeValue;", e, i);
                        if (txt.length() > 0) {
                            List<CoreLabel> tokens = tokenizeText(txt);
                            if (rec!=null) {
                                for (CoreLabel t : tokens) {
                                    t.set(CustomizableCoreAnnotations.LayoutXAnnotation.class,rec.x);
                                    t.set(CustomizableCoreAnnotations.LayoutYAnnotation.class,rec.y);
                                    t.set(CustomizableCoreAnnotations.LayoutHeightAnnotation.class,rec.height);
                                    t.set(CustomizableCoreAnnotations.LayoutWidthAnnotation.class,rec.width);
                                }
                            }
                            allTokens.addAll(tokens);
                        }
                    }
                }
            }
        }
        // put it out side of the if above because
        // although <br/> is not displayed, we still add new line when seeing it
        if (LIST_OF_TAGS_CREATING_NEW_LINES.contains(e.getTagName())
                && allTokens.get(allTokens.size()-1).get(CoreAnnotations.TextAnnotation.class).matches("\\p{Punct}")){
            List<CoreLabel> tokens = tokenizeText(".");
            allTokens.addAll(tokens);
        }
    }

    private static List<CoreLabel> tokenizeText(String txt) {
        Annotation annotation = new Annotation(txt);
        AnnotatorFactory.getInstance().getTokenizer().annotate(annotation);
        return annotation.get(CoreAnnotations.TokensAnnotation.class);
    }

    public static void main(String[] args) {

        String baseUrl = "http://www.forwarddatalab.org/kevinchang";

        // launch browser and direct it to the Base URL
        driver.get(baseUrl);

        // get the title and print it
        String pageTitle = driver.getTitle();
        System.out.println(pageTitle);

        // List<WebElement> el = driver.findElements(By.cssSelector("*"));
        // It is not working because it will miss text nodes
        // System.out.println(((RemoteWebElement) tmp).getAttribute("innerHTML"));
        // String manipulation for the inner HTML or inner Text will be very fragile
        // System.out.println(((RemoteWebElement) tmp).findElement(By.xpath(".//*/text()")));
        // It is not working because it will throw an error for returned Object is not a Web Element

        // Object tmp = ((ChromeDriver) driver).executeScript("return document.querySelector('.sites-layout-tile h2 font');");
        // The script above is useful to directly get DOM node from selenium

//        String out = getAllTextWithLayout();

//        System.out.println(out);

        List<CoreLabel> allTokens = new ArrayList<>();
        travelDOMTreeWithSelenium((RemoteWebElement)driver.findElement(By.xpath("/html/body")),null,allTokens);
        Annotation document = new Annotation("");
        document.set(CoreAnnotations.TokensAnnotation.class,allTokens);
        AnnotatorFactory.getInstance().getAnnotationPipeline().annotate(document);

        System.out.format("%20s %10s %10s %10s\n", "Token", "POS", "NE", "RegexNE");
        System.out.format("%20s %10s %10s %10s\n", "--------------------", "----------", "----------", "----------");

        for (CoreLabel token: document.get(CoreAnnotations.TokensAnnotation.class)) {
            // this is the text of the token
            String word = token.get(CoreAnnotations.TextAnnotation.class);
            // this is the POS tag of the token
            String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
            // this is the NER label of the token
            String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
            // this is the custom label of the token from ner annotator
            String tmp = token.get(CustomizableCoreAnnotations.TestRegexNERAnnotation.class);
            // this is the custom label of the token from the custom annotator
            int x = token.get(CustomizableCoreAnnotations.LayoutXAnnotation.class);

            System.out.format("%20s %10s %10s %10s %d\n", word, pos, ne, tmp, x);
        }

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

    private static String getAllTextWithLayout() {
        StringBuilder sb2 = new StringBuilder();
        travelDOMTreeWithSelenium((RemoteWebElement)driver.findElement(By.xpath("/html/body")), sb2);
        return sb2.toString().replaceAll("\n\n+", "\n");
    }

    private static WebDriver driver;
}
