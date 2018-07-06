package org.forward.entitysearch.experiment;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotationPipeline;
import edu.stanford.nlp.pipeline.PipelineHelper;
import edu.stanford.nlp.util.TypesafeMap;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.forward.entitysearch.ingestion.ESAnnotatedHTMLDocument;
import org.forward.entitysearch.ingestion.HTMLDocumentIngestionManager;
import org.forward.entitysearch.ner.annotation.AnnotatorFactory;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class CreateAnnotatedTitles {
    public static void main (String[] args) {

        File folder = new File(args[0]);
        String outFolder = args[1];
        boolean success = (new File(outFolder)).mkdirs();
        if (!success) {
            System.err.println("Cannot create the folder " + outFolder);
        }

        AnnotationPipeline annotator = AnnotatorFactory.getInstance().getFullAnnotationPipeline();
        PipelineHelper.addPopularRegexRuleAnnotators(annotator);

        System.out.println("Ready to download and annotate HTML documents");
        System.out.println("----------------------------------------------");

        File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles) {
            if (file.isFile()) {
                ESAnnotatedHTMLDocument doc = FindCommonPatternsInDocuments.getEsAnnotatedHTMLDocumentFromFile(file);
                if (doc != null) {
                    String titleFileName = DigestUtils.md5Hex(doc.getURL());
                    System.out.println(doc.getTitle() + " " + titleFileName);
                    try {
                        String path = outFolder + titleFileName + ".ser";
                        Annotation titleDoc = new Annotation(doc.getTitle());
                        annotator.annotate(titleDoc);
                        FileOutputStream fileOut =
                                new FileOutputStream(path);
                        ObjectOutputStream out = new ObjectOutputStream(fileOut);
                        out.writeObject(titleDoc);
                        out.close();
                        fileOut.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
