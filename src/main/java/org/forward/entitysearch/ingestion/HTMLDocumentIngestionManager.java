package org.forward.entitysearch.ingestion;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CustomizableCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.PipelineHelper;
import edu.stanford.nlp.util.Pair;
import edu.stanford.nlp.util.TypesafeMap;
import org.forward.entitysearch.AnnotationProperties;
import org.forward.entitysearch.ner.annotation.AnnotatorFactory;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebElement;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.cli.*;

public class HTMLDocumentIngestionManager {

    public static final String WEB_DOCUMENT_CANNOT_BE_OPENED_ERROR = "oOo This web document cannot be opened by browser oOo";
    public static final int MAX_LENGTH_OF_PAGE_SOURCE = 400000;
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

    public static WebDriver createChromeDriver() {
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

    private static void travelDOMTreeWithSelenium(RemoteWebElement e, Rectangle r, List<CoreLabel> allTokens, WebDriver driver) throws StaleElementReferenceException {
        try {
            e.isDisplayed();
        } catch (Exception ex) {
            System.out.println("Some dangling nodes are found in this URL");
            System.err.println("Some dangling nodes are found in this URL");
            return;
            // Doing it is more beneficial than throwing errors because we might get partial documents.
            // Actually it is both good and bad. Should think of a good strategy here!
        }
        if (e.isDisplayed()) {
            Rectangle rec = null;
            try {
                rec = new Rectangle(e.getLocation().getX(), e.getLocation().getY(), e.getSize().getHeight(), e.getSize().getWidth());
            } catch (Exception err) {
                System.err.println("There is no layout info in this element: " + e.getTagName());
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

        Options options = new Options();

        Option input = new Option("i", "input", true, "input url");
        input.setRequired(true);
        options.addOption(input);

        Option output = new Option("o", "output", true, "output folder for serialized files");
        output.setRequired(true);
        options.addOption(output);

        Option verbose = new Option("v", "verbose", true, "print additional message for debugging");
        verbose.setRequired(false);
        options.addOption(verbose);

        Option startingName = new Option("s", "start", true, "skip until seeing the url with this name");
        startingName.setRequired(false);
        options.addOption(startingName);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);

            System.exit(1);
            return;
        }

        boolean VERBOSE = false;
        if (cmd.hasOption("verbose"))
            VERBOSE = Boolean.parseBoolean(cmd.getOptionValue("verbose"));

        String STARTING_FILE_NAME = null;
        if (cmd.hasOption("start"))
            STARTING_FILE_NAME = cmd.getOptionValue("start");

        String inputFile = cmd.getOptionValue("input");
        String outputFolder = cmd.getOptionValue("output");

        List<Pair<String,String>> urls = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] tmp = line.split("\t");
                urls.add(new Pair<>(tmp[0],tmp[1]));
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        long time = System.currentTimeMillis();
        long start = time;

        WebDriver driver = createChromeDriver();
//        System.out.println(getAllTextWithLayout(driver,baseUrl));
        time = System.currentTimeMillis();
        System.out.println("Finish loading web driver " + (time-start)/1000 + " seconds");
        start = time;

        AnnotatorFactory.getInstance().getAnnotationPipeline();
        time = System.currentTimeMillis();
        System.out.println("Finish loading the default annotation pipeline " + (time-start)/1000 + " seconds");
        start = time;

        List<Class<? extends TypesafeMap.Key<String>>> fields = PipelineHelper.addPopularRegexRuleAnnotators(AnnotatorFactory.getInstance().getAnnotationPipeline());
        time = System.currentTimeMillis();
        System.out.println("Finish loading extra annotators " + (time-start)/1000 + " seconds");
        start = time;

        System.out.println("Ready to download and annotate HTML documents");
        System.out.println("----------------------------------------------");

        HashSet<String> seenUrls = new HashSet<>();

        for (int i = 0; i < urls.size(); i++) {
            String filename = urls.get(i).first;
            String baseUrl = urls.get(i).second;

            if (STARTING_FILE_NAME != null) {
                if (filename.equals(STARTING_FILE_NAME)) {
                    STARTING_FILE_NAME = null;
                } else {
                    continue;
                    // skip until seeing the file name
                }
            }

            System.out.println(filename + "\t" +  baseUrl);

            Integer responseCode = getHttpResponseCode(baseUrl);
            if (responseCode == null || responseCode >= 400) {
                System.out.println("Bad Request with code: " + responseCode);
                System.err.println("Bad Request " + "with code " + responseCode + ": " + baseUrl);
                continue;
            }

            try {
                driver.get(baseUrl);
            } catch (Exception e) {
                System.out.println("Web driver cannot open the page!");
                System.err.println("Web driver cannot open the page: " + baseUrl);
                driver.close();
                driver = createChromeDriver();
                continue;
            }
            String currentUrl; // final url after redirects
            try {
                currentUrl = driver.getCurrentUrl();
            } catch (Exception ex) {
                System.out.println("This web document cannot be opened by browser!");
                System.err.println("This web document cannot be opened by browser: " + baseUrl);
                driver.close();
                driver = createChromeDriver();
                continue;
            }
            if (seenUrls.contains(currentUrl)) {
                // for deduplication
                // and also for avoiding the case when the new URL is a file which does not navigate the driver to a new page
                System.out.println("This URL has been rendered " + currentUrl);
                System.err.println("This URL has been rendered " + currentUrl);
                if (!baseUrl.equalsIgnoreCase(currentUrl)) {
                    System.out.println("It has been redirected from the base URL: " + baseUrl);
                    System.err.println("It has been redirected from the base URL: " + baseUrl);
                }
                continue;
            }
            if (driver.getPageSource().length() > MAX_LENGTH_OF_PAGE_SOURCE) {
                System.out.println("This document is too long!");
                System.err.println("This document is too long " + baseUrl);
                continue;
            }

            ESAnnotatedHTMLDocument document = null;
            try {
                document = getHTMLDocumentForAnnotation(driver);
            } catch(Exception ex) {
                System.out.println("There is some exception when parsing the document ");
                System.err.println("There is some exception when parsing the document in this URL: " + baseUrl);
                System.err.println(ex.getClass());
                continue;
            }

            if (document == null) {
                System.out.println("This URL cannot be rendered by Selenium!");
                System.err.println("This URL cannot be rendered by Selenium " + baseUrl);
                continue;
            }

            if (document.get(CoreAnnotations.TokensAnnotation.class).size() <= 1) {
                System.out.println("This URL is probably not a web page!");
                System.err.println("This URL is probably not a web page " + baseUrl);
                continue;
            }

            time = System.currentTimeMillis();
            System.out.println("Finish creating document for annotation " + (time-start)/1000 + " seconds");
            start = time;

            seenUrls.add(currentUrl);

            try {
                AnnotatorFactory.getInstance().getAnnotationPipeline().annotate(document);
            } catch(Exception ex) {
                System.out.println("There is an exception when annotating this document");
                System.err.println("There is an exception when annotating the document in this URL: " + baseUrl);
                continue;
            }

            time = System.currentTimeMillis();
            System.out.println("Finish annotation " + (time-start)/1000 + " seconds");
            start = time;

            try {
                String path = outputFolder + filename + ".ser";
                FileOutputStream fileOut =
                        new FileOutputStream(path);
                ObjectOutputStream out = new ObjectOutputStream(fileOut);
                out.writeObject(document);
                out.close();
                fileOut.close();
                if (VERBOSE)
                    System.out.println("Serialized data is saved to " + path);
            } catch (IOException e) {
                e.printStackTrace();
            }
            time = System.currentTimeMillis();
            if (VERBOSE) {
                System.out.println("Finish serialization " + (time-start)/1000 + " seconds");
                System.out.println("Done with " + document.getTitle() + " with size " + document.getHeight() + " " + document.getWidth());
            }

            if (VERBOSE) {
                printAnnotatedDocument(document);
                PipelineHelper.printAnnotatedDocument(document, fields);
            }
        }
        driver.close();


//        time = System.currentTimeMillis();
//        start = time;
//        document = getHTMLDocumentForAnnotation("https://cs.illinois.edu/directory/profile/kcchang", driver);
//        System.out.println("After creating document for annotation pipeline " + (System.currentTimeMillis()-time));
//        time = System.currentTimeMillis();
//        AnnotatorFactory.getInstance().getAnnotationPipeline().annotate(document);
//        System.out.println("After annotation " + (System.currentTimeMillis()-time));
//        System.out.println("Total time: " + (System.currentTimeMillis() - start));
//        time = System.currentTimeMillis();
//        PipelineHelper.printAnnotatedDocument(document, fields);
//        System.out.println("After printing results " + (System.currentTimeMillis()-time));

//        printAnnotatedDocument(document);

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

    public static Integer getHttpResponseCode(String baseUrl) {
        try {
            URL url = new URL(baseUrl);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            return connection.getResponseCode();
        } catch (Exception e) {
            System.err.println("This url cannot be opened: " + baseUrl);
        }
        return null;
    }

    private static ESAnnotatedHTMLDocument getHTMLDocumentForAnnotation(WebDriver driver) throws StaleElementReferenceException{

        List<CoreLabel> allTokens = new ArrayList<>();
        RemoteWebElement e;
        try{
            e = (RemoteWebElement) driver.findElement(By.xpath("/html/body"));
        } catch (Exception ex) {
            //ex.printStackTrace();
            return null;
        }
        travelDOMTreeWithSelenium(e,null,allTokens, driver);
        ESAnnotatedHTMLDocument document = new ESAnnotatedHTMLDocument(allTokens);
//        List<List<CoreLabel>> allTokens = new ArrayList<>();
//        travelDOMTreeWithSelenium2((RemoteWebElement)driver.findElement(By.xpath("/html/body")),null,allTokens, driver);
//        ESAnnotatedHTMLDocument document = new ESAnnotatedHTMLDocument();
//        document.loadFromTokens(allTokens);
        document.setURL(driver.getCurrentUrl());
        document.setTitle(driver.getTitle());
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

    private static String CUR_URL = "";
}
