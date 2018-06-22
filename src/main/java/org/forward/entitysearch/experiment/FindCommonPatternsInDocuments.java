package org.forward.entitysearch.experiment;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CustomizableCoreAnnotations;
import edu.stanford.nlp.ling.tokensregex.*;
import edu.stanford.nlp.patterns.Pattern;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;
import org.forward.entitysearch.ingestion.ESAnnotatedHTMLDocument;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;

public class FindCommonPatternsInDocuments {
    private static final String ALL_NER = "[{ner:PERSON}|{ner:LOCATION}|{ner:ORGANIZATION}|{ner:MISC}|{ner:MONEY}|{ner:NUMBER}|{ner:ORDINAL}|{ner:PERCENT}|{ner:DATE}|{ner:TIME}|{ner:DURATION}|{ner:SET}]+";
    private static final boolean DEBUG = false;

    public static void main(String[] args) {
        // Kevin: 4.ser
        // Cheng: 00087.ser

        String[] queryTokens = args[0].split(" ");

        ArrayList<File> listOfFiles = new ArrayList<>();
        for (int i = 1; i < args.length; i++) {
            listOfFiles.add(new File(args[i]));
        }
        ArrayList<ESAnnotatedHTMLDocument> docs = new ArrayList<>();
        for (File file : listOfFiles) {
            if (file.isFile()) {
                ESAnnotatedHTMLDocument doc = getEsAnnotatedHTMLDocumentFromFile(file);
                if (doc != null) {
                    docs.add(doc);
                }
            }
        }

        Env env = TokenSequencePattern.getNewEnv();
        env.setDefaultStringMatchFlags(NodePattern.CASE_INSENSITIVE);



        ArrayList<ArrayList<String>> listOfTemplates = new ArrayList<>();
        for (ESAnnotatedHTMLDocument doc : docs) {
            ArrayList<String> templates = new ArrayList<>();
            listOfTemplates.add(templates);
            if (DEBUG) {
                System.out.println("Processing " + doc.getURL());
            }
            for (String qTok : queryTokens) {
//            SequencePattern.PatternExpr sequencePatternExpr = getSequencePatternExpr("Kevin", "C.C.", "Chang");
//            TokenSequencePattern p2 = TokenSequencePattern.compile("[{ner:PERSON}|{ner:LOCATION}]+ []{0,3} {ner}+");
                for (String patternString : getPatterns(qTok)) {
                    if (DEBUG) {
                        System.out.println(patternString);
                    }
                    TokenSequencePattern p = TokenSequencePattern.compile(patternString);
                    TokenSequenceMatcher m = p.getMatcher(doc.get(CoreAnnotations.TokensAnnotation.class));
                    String template;
                    while (m.find()) {
                        if (DEBUG) {
                            System.out.println(m.group());
                        }
                        List<CoreMap> map = m.groupNodes();
                        String first = convertFromLabelToHashTag((CoreLabel) map.get(0));
                        String second = convertFromLabelToHashTag((CoreLabel) map.get(map.size() - 1));
                        if (!first.equals(second)) {
                            template = first + " " + second;
                            templates.add(template);
                        }
//                for (CoreMap map : m.groupNodes()) {
//                    System.out.println(" * " + map.get(CoreAnnotations.NamedEntityTagAnnotation.class));
//                }
                    }
                }
            }
        }
        HashSet<String> firstTemplates = new HashSet<>(listOfTemplates.get(0));
        ArrayList<String> finalTemplates = new ArrayList<>();
        for (String template : firstTemplates) {
            //System.out.println(template);
            boolean ok = true;
            for (int i = 1; i < listOfTemplates.size(); i++) {
                if (!listOfTemplates.get(i).contains(template)) {
                    ok = false;
                    break;
                }
            }
            if (ok) {
                finalTemplates.add(template);
            }
        }
        String strOut = "[";
        for (int i = 0; i < finalTemplates.size()-1; i++) {
            String template = finalTemplates.get(i);
            strOut += "\"@near ( " + template + " )\",";
        }
        strOut += "\"@near ( " + finalTemplates.get(finalTemplates.size()-1) + " )\"" + "]";
        System.out.print(strOut);
    }

    private static ArrayList<String> getPatterns(String token) {
        String tmp = token;
        if (isEntity(token)) {
            tmp = "[{ner:" + convertFromHashTagToLabel(token) + "}]";
        }
        ArrayList<String> rets = new ArrayList<>();
        rets.add(tmp + " []{0,5} " + ALL_NER);
        rets.add(ALL_NER + " []{0,5} " + tmp);
        return rets;
    }

    private static boolean isEntity(String token) {
        return token.charAt(0) == '#';
    }

    private static String convertFromHashTagToLabel(String hashtag) {
        return hashtag.replaceAll("#", "").toUpperCase();
    }

    private static String convertFromLabelToHashTag(CoreLabel label) {
        if (!label.ner().equals("O")) {
            return "#" + label.ner().toLowerCase();
        } else {
            return label.word();
        }
    }

    private static SequencePattern.PatternExpr getSequencePatternExpr(String... textRegex) {
        List<SequencePattern.PatternExpr> patterns = new ArrayList<SequencePattern.PatternExpr>(textRegex.length);
        for (String s : textRegex) {
            patterns.add(new SequencePattern.NodePatternExpr(CoreMapNodePattern.valueOf(s)));
        }
        return new SequencePattern.SequencePatternExpr(patterns);
    }

    public static ESAnnotatedHTMLDocument getEsAnnotatedHTMLDocumentFromFile(File file) {
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
        return doc;
    }
}
