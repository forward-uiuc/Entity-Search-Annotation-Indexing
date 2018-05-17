package edu.stanford.nlp.ling;

public class TestRegexNERAnnotation implements CoreAnnotation<String> {
    @Override
    public Class<String> getType() {
      return String.class;
    }
  }