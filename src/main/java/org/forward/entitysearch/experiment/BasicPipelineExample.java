package org.forward.entitysearch.experiment;

import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.util.TypesafeMap;

import java.util.*;

public class BasicPipelineExample {

    public static void main(String[] args) {

        String text = "Kevin Chang was born in California. " +
                "His email is joe@illinois.edu and his phone number is (217) 402 4647. " +
                "He was borned in Champaign, IL 61802. \n" +
                "He is a member of CIKM. He usually attends at Information Retrieval Facility Conference, which is very good! " +
                "He took course Adv Memory & Storage Systems and CS 412 and ECE411 and CS-410 at UIUC. " +
                "He loves C++ very much. " +
                "In 2017, he went to Paris, France in the summer for Access: critical perspectives on communication, cultural and policy studies, " +
                "as well as Online: exploring technology and resources for information professionals and finally " +
                "Connection Science : journal of neural computing , artificial intelligence , and cognitive research. " +
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

//        String test_regex = "mapping_files/test_regex.txt";
        List<Class<? extends TypesafeMap.Key<String>>> fields = PipelineHelper.addPopularRegexRuleAnnotators(pipeline);
//        pipeline.addAnnotator(new CustomizableFieldRegexNERAnnotator(test_regex, false,true,null, false, CustomizableCoreAnnotations.ConferenceAcronymTagAnnotation.class));
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
        // Take note to use tokenizer to create dictionary-based annotation
        Annotation document = new Annotation(text);
        pipeline.annotate(document);
//        for (CoreLabel token: document.get(CoreAnnotations.TokensAnnotation.class)) {
//            System.out.print("|" + token.word() + "|");
//            for (int i = 0; i < token.word().length(); i++) {
//                System.out.print(" " + (int) token.word().charAt(i));
//            }
//            System.out.println();
//        }

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

        PipelineHelper.printAnnotatedDocument(document, fields);
    }

}