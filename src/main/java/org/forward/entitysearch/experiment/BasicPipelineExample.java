package org.forward.entitysearch.experiment;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CustomizableCoreAnnotations;
import edu.stanford.nlp.pipeline.*;
import edu.stanford.nlp.util.CoreMap;

import java.util.*;

public class BasicPipelineExample {

    public static void main(String[] args) {

        // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution
        Properties props = new Properties();
//        props.setProperty("annotators", "tokenize, ssplit");
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, regexner");
//        props.setProperty("customAnnotatorClass.cus","edu.stanford.nlp.pipeline.RegexNERAnnotator");
//        props.setProperty("cus.mapping","edu/stanford/nlp/models/regexner/type_map_clean");
//        props.setProperty("regexner.mapping.header","edu.stanford.nlp.ling.CoreAnnotations$NamedEntityTagAnnotation");
        String MAPPING = "edu/stanford/nlp/models/regexner/type_map_clean";
//        String MAPPING = "edu/stanford/nlp/models/dcoref/statesandprovinces";
        //String MAPPING = "edu/stanford/nlp/models/kbp/regexner_caseless.tab";
//        CustomizableOutputColumnRegexNERAnnotator annotator = new CustomizableOutputColumnRegexNERAnnotator(MAPPING, false, null, CustomizableCoreAnnotations.TestRegexNERAnnotation.class);
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        TokensRegexNERAnnotator regexNerAnnotator = (TokensRegexNERAnnotator)(pipeline.annotators.get(5));
//        regexNerAnnotator.getAnnotationFields().remove(0);
        regexNerAnnotator.getAnnotationFields().set(0,CustomizableCoreAnnotations.TestRegexNERAnnotation.class);

//        TokensRegexNERAnnotator annotator2 = new TokensRegexNERAnnotator(MAPPING);
//        pipeline2.addAnnotator(pipeline.annotators.get(1));
//        pipeline.addAnnotator(annotator2);

        // read some text in the text variable
        String text = "Joe Smith was born in California. " +
                "His email is joe@illinois.edu and his phone number is 2174024647. " +
                "He was borned in Champaign, Illinois. \n" +
                "In 2017, he went to Paris, France in the summer. " +
                "His flight left at 3:00pm on July 10th, 2017. " +
                "After eating some escargot for the first time, Joe said, \"That was delicious!\" " +
                "He sent a postcard to his sister Jane Smith. " +
                "After hearing about Joe's trip, Jane decided she might go to France one day.";

        // create an empty Annotation just with the given text
        Annotation document = new Annotation(text);
        pipeline.annotate(document);
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

            System.out.format("%20s %10s %10s %10s\n", word, pos, ne, tmp);
        }

        for(CoreMap sentence: document.get(CoreAnnotations.SentencesAnnotation.class)) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                // this is the text of the token
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                // this is the POS tag of the token
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                // this is the NER label of the token
                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                // this is the custom label of the token from ner annotator
                String tmp = token.get(CustomizableCoreAnnotations.TestRegexNERAnnotation.class);
                // this is the custom label of the token from the custom annotator

                System.out.format("%20s %10s %10s %10s\n", word, pos, ne, tmp);
            }
            System.out.println("\n");
        }
        System.exit(0);

        AnnotationPipeline pipeline2 = new StanfordCoreNLP();
        System.out.println(pipeline.annotators.size());
        pipeline2.addAnnotator(pipeline.annotators.get(0));

        // run all Annotators on this text
        pipeline2.annotate(document);

        // these are all the sentences in this document
        // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
        List<CoreMap> sentences2 = document.get(CoreAnnotations.SentencesAnnotation.class);
        System.out.println(sentences2.size());

//        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, regexner");
//        pipeline2 = new StanfordCoreNLP(props);
        pipeline2 = new AnnotationPipeline();
        pipeline2.addAnnotator(pipeline.annotators.get(0));
        pipeline2.addAnnotator(pipeline.annotators.get(1));
        pipeline2.addAnnotator(pipeline.annotators.get(2));
        pipeline2.addAnnotator(pipeline.annotators.get(3));
        pipeline2.addAnnotator(pipeline.annotators.get(4));
        pipeline2.addAnnotator(pipeline.annotators.get(5));
        Annotation document2 = new Annotation(sentences2);
        document2 = document;
        pipeline2.annotate(document2);
        //pipeline2.annotate(new Annotation(text));
        List<CoreMap> sentences = document2.get(CoreAnnotations.SentencesAnnotation.class);
        System.out.println(sentences.size());

        System.out.format("%20s %10s %10s %10s\n", "Token", "POS", "NE", "RegexNE");
        System.out.format("%20s %10s %10s %10s\n", "--------------------", "----------", "----------", "----------");
        for(CoreMap sentence: sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                // this is the text of the token
                String word = token.get(CoreAnnotations.TextAnnotation.class);
                // this is the POS tag of the token
                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
                // this is the NER label of the token
                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                // this is the custom label of the token from ner annotator
                String tmp = token.get(CustomizableCoreAnnotations.TestRegexNERAnnotation.class);
                // this is the custom label of the token from the custom annotator

                System.out.format("%20s %10s %10s %10s\n", word, pos, ne, tmp);
            }
        }
    }

}