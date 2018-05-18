package org.forward.entitysearch.experiment;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.CustomizableOutputColumnRegexNERAnnotator;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.util.ArrayCoreMap;
import edu.stanford.nlp.util.CoreMap;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class StanfordNLPAnnotator {

    // private static final String MAPPING = "/u/nlp/data/TAC-KBP2010/sentence_extraction/itest_map";
    private static final String MAPPING = "edu/stanford/nlp/models/regexner/type_map_clean";
    private static CustomizableOutputColumnRegexNERAnnotator annotator;

    public static void main(String[] args) {
        annotator = new CustomizableOutputColumnRegexNERAnnotator(MAPPING, false, null);
        String str = "President Barack Obama lives in Chicago , Illinois , "
                + "and is a practicing Christian .";
        TokenizerFactory<CoreLabel> tokenizerFactory =
                PTBTokenizer.factory(new CoreLabelTokenFactory(), "");
        Tokenizer<CoreLabel> tok =
                tokenizerFactory.getTokenizer(new StringReader(str));
        List<CoreLabel> tokens = tok.tokenize();
        CoreMap sentence = new ArrayCoreMap();
        sentence.set(CoreAnnotations.TokensAnnotation.class, tokens);
        List<CoreMap> sentences = new ArrayList<>();
        sentences.add(sentence);
        Annotation corpus = new Annotation(str);
        corpus.set(CoreAnnotations.SentencesAnnotation.class, sentences);
        annotator.annotate(corpus);
        for (int i = 0; i < tokens.size(); i++) {
            System.out.println(i + ": " + tokens.get(i).get(CoreAnnotations.NamedEntityTagAnnotation.class));
        }
    }
}
