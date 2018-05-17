package edu.stanford.nlp.ling;

import edu.stanford.nlp.pipeline.CustomizableOutputColumnRegexNERAnnotator;
import edu.stanford.nlp.util.*;

/**
 * <p>
 * Extends {@link CoreAnnotations} to allow customizable classes
 * </p>
 *
 * <p>
 * Set of common annotations for {@link CoreMap}s. The classes
 * defined here are typesafe keys for getting and setting annotation
 * values. These classes need not be instantiated outside of this
 * class. e.g {@link RegexNERAnnotation}.class serves as the key and a
 * {@code String} serves as the value containing the
 * corresponding word.
 * </p>
 *
 * <p>
 * New types of {@link CoreAnnotation} can be defined anywhere that is
 * convenient in the source tree - they are just classes. This file exists to
 * hold widely used "core" annotations and others inherited from the
 * Label family. In general, most keys should be placed in this file as
 * they may often be reused throughout the code. This architecture allows for
 * flexibility, but in many ways it should be considered as equivalent to an
 * enum in which everything should be defined
 * </p>
 *
 * <p>
 * The getType method required by CoreAnnotation must return the same class type
 * as its value type parameter. It feels like one should be able to get away
 * without that method, but because Java erases the generic type signature, that
 * info disappears at runtime. See {@link RegexNERAnnotation} for an example.
 * </p>
 *
 * @author longpt214
 */
public class CustomizableCoreAnnotations {

  private CustomizableCoreAnnotations() { } // only static members

  /**
   * The CoreMap key for customizable {@link CustomizableOutputColumnRegexNERAnnotator}
   */
  public static class RegexNERAnnotation implements CoreAnnotation<String> {
    @Override
    public Class<String> getType() {
      return String.class;
    }
  }

  /**
   * One instance of {@link CustomizableOutputColumnRegexNERAnnotator}
   */
  public static class TestRegexNERAnnotation implements CoreAnnotation<String> {
    @Override
    public Class<String> getType() {
      return String.class;
    }
  }

}
