package org.forward.entitysearch.experiment;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.forward.entitysearch.ingestion.ESAnnotatedHTMLDocument;
import org.forward.entitysearch.ingestion.HTMLDocumentIngestionManager;
import org.json.simple.JSONObject;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class CreateScreenshots {
    public static void main (String[] args) throws IOException {

        File folder = new File(args[0]);
        String outFolder = args[1] + "/screenshots";
        boolean success = (new File(outFolder)).mkdirs();
        if (!success) {
            System.err.println("Cannot create the folder " + outFolder);
        }

        WebDriver driver = HTMLDocumentIngestionManager.createChromeDriver();

        File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles) {
            if (file.isFile()) {
                ESAnnotatedHTMLDocument doc = null;
                FileInputStream fin;
                ObjectInputStream ois;
                try {
                    fin = new FileInputStream(file);
                    ois = new ObjectInputStream(fin);
                    doc = (ESAnnotatedHTMLDocument) ois.readObject();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (doc != null) {
                    String imageFileName = DigestUtils.md5Hex(doc.getURL());
                    try {
                        driver.get(doc.getURL());
                    } catch (Exception e) {
                        System.err.println("Web driver cannot open the page: " + doc.getURL());
                        driver.close();
                        driver = HTMLDocumentIngestionManager.createChromeDriver();
                        continue;
                    }
                    File scrFile = ((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
                    FileUtils.copyFile(scrFile, new File(outFolder + "/" + imageFileName + ".png"));
                    try {
                        TimeUnit.SECONDS.sleep(3);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        driver.close();
    }
}
