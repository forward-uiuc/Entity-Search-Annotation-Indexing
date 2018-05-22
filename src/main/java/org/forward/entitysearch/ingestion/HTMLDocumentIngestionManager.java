package org.forward.entitysearch.ingestion;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CustomizableCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.PipelineHelper;
import edu.stanford.nlp.util.TypesafeMap;
import org.forward.entitysearch.AnnotationProperties;
import org.forward.entitysearch.experiment.AnnotatorFactory;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebElement;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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
    }

    private static WebDriver createChromeDriver() {
        System.setProperty("webdriver.chrome.driver",AnnotationProperties.getInstance().getProperty("selenium.chromedriver"));
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // add this if wanting Chrome to be headless.
        WebDriver driver = new ChromeDriver(options);
        driver.manage().window().setSize(new Dimension(1024,768));
        return driver;
    }

    private static int travelDOMTreeWithSelenium(RemoteWebElement e, StringBuilder sb, WebDriver driver) {
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
                    count += travelDOMTreeWithSelenium((RemoteWebElement) children.get(i), sb, driver);
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

    private static void travelDOMTreeWithSelenium(RemoteWebElement e, Rectangle r, List<CoreLabel> allTokens, WebDriver driver) {
        if (e.isDisplayed()) {
            Rectangle rec = null;
            try {
                rec = new Rectangle(e.getLocation().getX(), e.getLocation().getY(), e.getSize().getHeight(), e.getSize().getWidth());
            } catch (Exception err) {
                System.err.println("Thee is no layout info in this element: " + e.getTagName());
            }
            if (rec != null && e.getTagName().equalsIgnoreCase("img")) {
                CoreLabel t = new CoreLabel();
                t.set(CustomizableCoreAnnotations.LayoutXAnnotation.class,rec.x);
                t.set(CustomizableCoreAnnotations.LayoutYAnnotation.class,rec.y);
                t.set(CustomizableCoreAnnotations.LayoutHeightAnnotation.class,rec.height);
                t.set(CustomizableCoreAnnotations.LayoutWidthAnnotation.class,rec.width);
                t.set(CustomizableCoreAnnotations.TypeAnnotation.class, "img");
                t.set(CoreAnnotations.TextAnnotation.class,"");
                allTokens.add(t);
            }
            if (rec == null) {
                rec = r;
            }
            List<Object> children = (List<Object>) ((ChromeDriver) driver).executeScript("return arguments[0].childNodes;", e);
            for (int i = 0; i < children.size(); i++) {
                if (children.get(i) instanceof RemoteWebElement) {
                    travelDOMTreeWithSelenium((RemoteWebElement) children.get(i), rec, allTokens, driver);
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
                && allTokens.size() > 0 && !allTokens.get(allTokens.size()-1).get(CoreAnnotations.TextAnnotation.class).matches("\\p{Punct}")){
            List<CoreLabel> tokens = tokenizeText(".");
            allTokens.addAll(tokens);
        }
    }

    private static void travelDOMTreeWithSelenium2(RemoteWebElement e, Rectangle r, List<List<CoreLabel>> allTokens, WebDriver driver) {
        List<CoreLabel> tokens = null;
        if (e.isDisplayed()) {
            Rectangle rec = null;
            try {
                rec = new Rectangle(e.getLocation().getX(), e.getLocation().getY(), e.getSize().getHeight(), e.getSize().getWidth());
            } catch (Exception err) {
                System.err.println("Thee is no layout info in this element: " + e.getTagName());
            }
            if (rec != null && e.getTagName().equalsIgnoreCase("img")) {
                CoreLabel t = new CoreLabel();
                t.set(CustomizableCoreAnnotations.LayoutXAnnotation.class,rec.x);
                t.set(CustomizableCoreAnnotations.LayoutYAnnotation.class,rec.y);
                t.set(CustomizableCoreAnnotations.LayoutHeightAnnotation.class,rec.height);
                t.set(CustomizableCoreAnnotations.LayoutWidthAnnotation.class,rec.width);
                t.set(CustomizableCoreAnnotations.TypeAnnotation.class, "img");
                t.set(CoreAnnotations.TextAnnotation.class,"");
                tokens = new ArrayList<>();
                tokens.add(t);
                allTokens.add(tokens);
            }
            if (rec == null) {
                rec = r;
            }
            List<Object> children = (List<Object>) ((ChromeDriver) driver).executeScript("return arguments[0].childNodes;", e);
            for (int i = 0; i < children.size(); i++) {
                if (children.get(i) instanceof RemoteWebElement) {
                    travelDOMTreeWithSelenium2((RemoteWebElement) children.get(i), rec, allTokens, driver);
                } else {
                    String nodeName = (String) ((ChromeDriver) driver).executeScript("return arguments[0].childNodes[arguments[1]].nodeName;", e, i);
                    if (nodeName.equals("#text")) {
                        String txt = (String) ((ChromeDriver) driver).executeScript("return arguments[0].childNodes[arguments[1]].nodeValue;", e, i);
                        if (txt.length() > 0) {
                            tokens = tokenizeText(txt);
                            if (rec!=null) {
                                for (CoreLabel t : tokens) {
                                    t.set(CustomizableCoreAnnotations.LayoutXAnnotation.class,rec.x);
                                    t.set(CustomizableCoreAnnotations.LayoutYAnnotation.class,rec.y);
                                    t.set(CustomizableCoreAnnotations.LayoutHeightAnnotation.class,rec.height);
                                    t.set(CustomizableCoreAnnotations.LayoutWidthAnnotation.class,rec.width);
                                }
                            }
                            allTokens.add(tokens);
                        }
                    }
                }
            }
        }
        // put it out side of the if above because
        // although <br/> is not displayed, we still add new line when seeing it
        // tokens is the last arraylist
        if (LIST_OF_TAGS_CREATING_NEW_LINES.contains(e.getTagName())
                && tokens != null && tokens.size() > 0 && !tokens.get(tokens.size()-1).get(CoreAnnotations.TextAnnotation.class).matches("\\p{Punct}")){
            tokens = tokenizeText(".");
            allTokens.add(tokens);
        }
    }


    private static List<CoreLabel> tokenizeText(String txt) {
        Annotation annotation = new Annotation(txt);
        AnnotatorFactory.getInstance().getTokenizer().annotate(annotation);
        return annotation.get(CoreAnnotations.TokensAnnotation.class);
    }

    public static void main(String[] args) {

        long time = System.currentTimeMillis();
        long start = time;
        String baseUrl = "http://www.forwarddatalab.org/kevinchang";
        WebDriver driver = createChromeDriver();
//        System.out.println(getAllTextWithLayout(driver,baseUrl));
        System.out.println("After creating driver " + (System.currentTimeMillis()-time));
        time = System.currentTimeMillis();
        AnnotatorFactory.getInstance().getAnnotationPipeline();
        System.out.println("After loading the default annotation pipeline " + (System.currentTimeMillis()-time));
        time = System.currentTimeMillis();
        ESAnnotatedHTMLDocument document = getHTMLDocumentForAnnotation(baseUrl, driver);
        System.out.println("After creating document for annotation " + (System.currentTimeMillis()-time));
        time = System.currentTimeMillis();
        List<Class<? extends TypesafeMap.Key<String>>> fields = PipelineHelper.addPopularRegexRuleAnnotators(AnnotatorFactory.getInstance().getAnnotationPipeline());
        System.out.println("After loading extra components for annotation pipeline " + (System.currentTimeMillis()-time));
        time = System.currentTimeMillis();
        AnnotatorFactory.getInstance().getAnnotationPipeline().annotate(document);
        System.out.println("After annotation " + (System.currentTimeMillis()-time));
        System.out.println("Total time: " + (System.currentTimeMillis() - start));
        System.out.println(document.getTitle());
        System.out.println(document.getURL());
        System.out.println(document.getHeight() + " " + document.getWidth());
        try {
            FileOutputStream fileOut =
                    new FileOutputStream("serialized/kevin.ser");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(document);
            out.close();
            fileOut.close();
            System.out.printf("Serialized data is saved in serialized/kevin.ser");
        } catch (IOException i) {
            i.printStackTrace();
        }
        time = System.currentTimeMillis();
        printAnnotatedDocument(document);
        PipelineHelper.printAnnotatedDocument(document, fields);
        System.out.println("After printing results " + (System.currentTimeMillis()-time));
        time = System.currentTimeMillis();
        start = time;
        document = getHTMLDocumentForAnnotation("https://cs.illinois.edu/directory/profile/kcchang", driver);
        System.out.println("After creating document for annotation pipeline " + (System.currentTimeMillis()-time));
        time = System.currentTimeMillis();
        AnnotatorFactory.getInstance().getAnnotationPipeline().annotate(document);
        System.out.println("After annotation " + (System.currentTimeMillis()-time));
        System.out.println("Total time: " + (System.currentTimeMillis() - start));
        time = System.currentTimeMillis();
        PipelineHelper.printAnnotatedDocument(document, fields);
        System.out.println("After printing results " + (System.currentTimeMillis()-time));
//        printAnnotatedDocument(document);
        driver.close();

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

    }

    private static ESAnnotatedHTMLDocument getHTMLDocumentForAnnotation(String url, WebDriver driver) {
        driver.get(url);
        String pageTitle = driver.getTitle();
        System.out.println(url + " " + pageTitle);
        List<CoreLabel> allTokens = new ArrayList<>();
        RemoteWebElement e = (RemoteWebElement) driver.findElement(By.xpath("/html/body"));
        travelDOMTreeWithSelenium(e,null,allTokens, driver);
        ESAnnotatedHTMLDocument document = new ESAnnotatedHTMLDocument(allTokens);
//        List<List<CoreLabel>> allTokens = new ArrayList<>();
//        travelDOMTreeWithSelenium2((RemoteWebElement)driver.findElement(By.xpath("/html/body")),null,allTokens, driver);
//        ESAnnotatedHTMLDocument document = new ESAnnotatedHTMLDocument();
//        document.loadFromTokens(allTokens);
        document.setURL(url);
        document.setTitle(pageTitle);
        document.setHeight(e.getSize().height);
        document.setWidth(e.getSize().width);
        return document;
    }

    private static void printAnnotatedDocument(ESAnnotatedHTMLDocument document) {
        System.out.format("%30s %15s %15s %15s %15s %15s %15s %15s \n", "Token", "POS", "NE", "RegexNE", "x", "y", "height", "width");
        String sep = "---------------";
        System.out.format("%30s %15s %15s %15s %15s %15s %15s %15s\n", sep + sep, sep, sep, sep, sep, sep, sep, sep);

        for (CoreLabel token: document.get(CoreAnnotations.TokensAnnotation.class)) {
            // this is the text of the token
            String word = token.get(CoreAnnotations.TextAnnotation.class);
            if (token.containsKey(CustomizableCoreAnnotations.TypeAnnotation.class)) {
                word = token.get(CustomizableCoreAnnotations.TypeAnnotation.class);
            }
            // this is the POS tag of the token
            String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
            // this is the NER label of the token
            String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
            // this is the custom label of the token from ner annotator
            String tmp = token.get(CustomizableCoreAnnotations.RegexNERAnnotation.class);
            // these are the custom labels of the token for layout info
            Integer x = token.get(CustomizableCoreAnnotations.LayoutXAnnotation.class);
            Integer y = token.get(CustomizableCoreAnnotations.LayoutYAnnotation.class);
            Integer height = token.get(CustomizableCoreAnnotations.LayoutHeightAnnotation.class);
            Integer width = token.get(CustomizableCoreAnnotations.LayoutWidthAnnotation.class);

            System.out.format("%30s %15s %15s %15s %15d %15d %15d %15d\n", word, pos, ne, tmp, x, y, height, width);
        }
    }

    public static String getAllTextWithLayout(WebDriver driver, String url) {
        driver.get(url);
        String pageTitle = driver.getTitle();
        System.out.println(pageTitle);
        StringBuilder sb2 = new StringBuilder();
        travelDOMTreeWithSelenium((RemoteWebElement)driver.findElement(By.xpath("/html/body")), sb2, driver);
        return sb2.toString().replaceAll("\n\n+", "\n");
    }
}
