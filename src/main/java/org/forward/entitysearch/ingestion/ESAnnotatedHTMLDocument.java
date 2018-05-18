package org.forward.entitysearch.ingestion;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;

import java.util.ArrayList;
import java.util.List;

public class ESAnnotatedHTMLDocument extends Annotation {

    /*
     * Potentially buggy. Do not consider text section here.
     */
    public ESAnnotatedHTMLDocument(List<CoreLabel> allTokens) {
        super("");
        List<CoreLabel> tokens = new ArrayList<>();
        StringBuilder text = new StringBuilder();
        int cur = 0;
        for (CoreLabel token : allTokens) {
            String txt = token.get(CoreAnnotations.TextAnnotation.class);
            text.append(txt + " ");
            token.set(CoreAnnotations.CharacterOffsetBeginAnnotation.class, cur);
            token.set(CoreAnnotations.CharacterOffsetEndAnnotation.class, cur + txt.length());
            cur += txt.length() + 1;
            tokens.add(token);
        }
        this.set(CoreAnnotations.TokensAnnotation.class, tokens);
        this.set(CoreAnnotations.TextAnnotation.class, text.toString());
    }
}
