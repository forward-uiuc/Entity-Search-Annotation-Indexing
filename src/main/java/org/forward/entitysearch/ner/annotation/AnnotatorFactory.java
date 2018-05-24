package org.forward.entitysearch.ner.annotation;

import edu.stanford.nlp.ling.CustomizableCoreAnnotations;
import edu.stanford.nlp.pipeline.*;

import java.util.List;
import java.util.Properties;

public class AnnotatorFactory {

    private AnnotatorFactory() {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, regexner");
        StanfordCoreNLP providedPipeline = new StanfordCoreNLP(props);
        List<Annotator> providedAnnotators = providedPipeline.annotators;
        // separate label between ner and regexner
        TokensRegexNERAnnotator regexNerAnnotator = (TokensRegexNERAnnotator)(providedAnnotators.get(5));
        regexNerAnnotator.getAnnotationFields().set(0,CustomizableCoreAnnotations.RegexNERAnnotation.class);
        // separate tokenizer from other annotators
        tokenizer = (TokenizerAnnotator) providedAnnotators.get(0);
        pipeline = new AnnotationPipeline();
        //Can't use StanfordNLPAnnotator because it automatically adds pre-requisite annotators
        pipeline.addAnnotator(providedAnnotators.get(1));
        pipeline.addAnnotator(providedAnnotators.get(2));
        pipeline.addAnnotator(providedAnnotators.get(3));
        pipeline.addAnnotator(providedAnnotators.get(4));
        pipeline.addAnnotator(providedAnnotators.get(5));
    }

    public static AnnotatorFactory getInstance() {
        if (instance == null) {
            instance = new AnnotatorFactory();
        }
        return instance;
    }

    public TokenizerAnnotator getTokenizer() {
        return tokenizer;
    }

    public AnnotationPipeline getAnnotationPipeline() {
        return pipeline;
    }

    private static AnnotatorFactory instance = null;
    private TokenizerAnnotator tokenizer;
    private AnnotationPipeline pipeline;
}
