package org.forward.entitysearch.experiment;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CustomizableCoreAnnotations;
import org.forward.entitysearch.ingestion.ESAnnotatedHTMLDocument;

import java.io.FileInputStream;
import java.io.ObjectInputStream;

public class TestReadObject {
    public static void main (String[] args) {
        ESAnnotatedHTMLDocument doc = null;
        FileInputStream fin = null;
        ObjectInputStream ois = null;
        try {
            fin = new FileInputStream("serialized/0.ser");
            ois = new ObjectInputStream(fin);
            doc = (ESAnnotatedHTMLDocument) ois.readObject();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (doc != null) {
            System.out.println(doc.getTitle());
            System.out.println(doc.get(CoreAnnotations.TokensAnnotation.class).size());
            for (CoreLabel token : doc.get(CoreAnnotations.TokensAnnotation.class)) {
                System.out.println(token.word() + " " + token.ner() + " " +
                        token.get(CustomizableCoreAnnotations.LayoutHeightAnnotation.class) + " " +
                        token.get(CustomizableCoreAnnotations.LayoutWidthAnnotation.class));
            }
            System.out.println(doc.getHeight() + " " + doc.getWidth());
        }
    }
}
