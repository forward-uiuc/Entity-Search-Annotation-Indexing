package org.forward.entitysearch.experiment;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CustomizableCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import org.forward.entitysearch.ingestion.ESAnnotatedHTMLDocument;
import org.forward.entitysearch.ingestion.HTMLDocumentIngestionManager;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.List;

public class WalkThroughSerializedDocuments {
    public static void main (String[] args) throws IOException {
        // Kevin: 4.ser
        // Cheng: 00087.ser

        File folder = new File(args[0]);

        WebDriver driver = HTMLDocumentIngestionManager.createChromeDriver();

        File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles) {
            if (file.isFile()) {
//                ESAnnotatedHTMLDocument doc = FindCommonPatternsInDocuments.getEsAnnotatedHTMLDocumentFromFile(file);
//                if (doc != null) {
//                    String url = doc.getURL();
//                    System.out.println(url);
//                    if (url.equals("https://cs.illinois.edu/homes/czhai/")) {
//                        System.out.println(file);
//                    }
//                }

                Annotation doc = FindCommonPatternsInDocuments.getAnnotationFromFile(file);
                if (doc != null) {
                    for (CoreLabel token : doc.get(CoreAnnotations.TokensAnnotation.class)) {
                        System.out.println(token.word() + "\t" + token.ner() + "\t" + token.get(CustomizableCoreAnnotations.TopicTagAnnotation.class));
                    }
                }
                System.out.println();
            }
        }

        driver.close();
    }
}
