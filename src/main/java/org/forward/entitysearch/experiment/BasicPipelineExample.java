package org.forward.entitysearch.experiment;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CustomizableCoreAnnotations;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.util.CoreMap;

import java.util.*;

public class BasicPipelineExample {

    public static void main(String[] args) {

        String text = "Joe Smith was born in California. " +
                "His email is joe@illinois.edu and his phone number is (217) 402 4647. " +
                "He was borned in Champaign, IL 61802. \n" +
                "He is a member of CIKM. He usually attends at Information Retrieval Facility Conference, which is very good! " +
                "He took course CS412 at UIUC" +
                "In 2017, he went to Paris, France in the summer. " +
                "His flight left at 3:00pm on July 10th, 2017. " +
                "After eating some escargot for the first time, Joe said, \"That was delicious!\" " +
                "He sent a postcard to his sister Jane Smith. " +
                "After hearing about Joe's trip, Jane decided she might go to France one day.";

        // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner");
//        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, regexner");
//        props.setProperty("customAnnotatorClass.cus","edu.stanford.nlp.pipeline.RegexNERAnnotator");
//        props.setProperty("cus.mapping","edu/stanford/nlp/models/regexner/type_map_clean");
//        props.setProperty("regexner.mapping.header","edu.stanford.nlp.ling.CoreAnnotations$NamedEntityTagAnnotation");
//        String MAPPING = "edu/stanford/nlp/models/regexner/type_map_clean";
//        String MAPPING = "edu/stanford/nlp/models/dcoref/statesandprovinces";
        String MAPPING = "edu/stanford/nlp/models/kbp/regexner_cased.tab";
//        CustomizableFieldRegexNERAnnotator annotator = new CustomizableFieldRegexNERAnnotator(MAPPING, false, null, CustomizableCoreAnnotations.RegexNERAnnotation.class);
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
//        TokensRegexNERAnnotator regexNerAnnotator = (TokensRegexNERAnnotator)(pipeline.annotators.get(5));
//        regexNerAnnotator.getAnnotationFields().remove(0);
//        regexNerAnnotator.getAnnotationFields().set(0,CustomizableCoreAnnotations.RegexNERAnnotation.class);

        String CONFERENCE_MAPPING = "mapping_files/conference_list.txt";
        String CONFERENCE_ACRONYM_MAPPING = "mapping_files/conference_acronym_list.txt";
        String test_regex = "mapping_files/test_regex.txt";
//        pipeline.addAnnotator(new CustomizableFieldRegexNERAnnotator(CONFERENCE_MAPPING, true, CustomizableCoreAnnotations.ConferenceTagAnnotation.class));
        //pipeline.addAnnotator(new CustomizableFieldRegexNERAnnotator(CONFERENCE_ACRONYM_MAPPING, false, CustomizableCoreAnnotations.ConferenceAcronymTagAnnotation.class));
        pipeline.addAnnotator(new CustomizableFieldRegexNERAnnotator(test_regex, false,true,null, false, CustomizableCoreAnnotations.ConferenceAcronymTagAnnotation.class));
        /*
         tokensMap = {
          '-LRB-': '(',
          '-RRB-': ')',
          '-LSB-': '[',
          '-RSB-': ']',
          '-LCB-': '{',
          '-RCB-': '}',
          '``': '"',
          '\'\'': '"',
        };
         */

        // Take note to use non-breaking white space instead of white space for regex matching.
        // create an empty Annotation just with the given text
        Annotation document = new Annotation(text);
        pipeline.annotate(document);
//        for (CoreLabel token: document.get(CoreAnnotations.TokensAnnotation.class)) {
//            System.out.print("|" + token.word() + "|");
//            for (int i = 0; i < token.word().length(); i++) {
//                System.out.print(" " + (int) token.word().charAt(i));
//            }
//            System.out.println();
//        }
        System.out.format("%20s %10s %10s %10s\n", "Token", "NE", "POS", "Conference");
        System.out.format("%20s %10s %10s %10s\n", "--------------------", "----------", "----------", "----------");

//        for (CoreLabel token: document.get(CoreAnnotations.TokensAnnotation.class)) {
//            // this is the text of the token
//            String word = token.get(CoreAnnotations.TextAnnotation.class);
//            // this is the POS tag of the token
//            String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
//            // this is the NER label of the token
//            String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
//            // this is the custom label of the token from ner annotator
//            String tmp = token.get(CustomizableCoreAnnotations.RegexNERAnnotation.class);
//            // this is the custom label of the token from the custom annotator
//            System.out.format("%20s %10s %10s %10s\n", word, pos, ne, tmp);
//        }

        for(CoreMap sentence: document.get(CoreAnnotations.SentencesAnnotation.class)) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                // this is the text of the token
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                // this is the NER label of the token
                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                // this is the custom label of the token from ner annotator
                String pos = token.get(CustomizableCoreAnnotations.ConferenceAcronymTagAnnotation.class);
                // this is the custom label of the token from the custom annotator
                String tmp = token.get(CustomizableCoreAnnotations.ConferenceTagAnnotation.class);
                // this is the custom label of the token from the custom annotator

                System.out.format("%20s %10s %10s %10s\n", word, ne, pos, tmp);
            }
            System.out.println("\n");
        }
    }

}