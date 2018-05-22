package edu.stanford.nlp.pipeline;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CustomizableCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.TypesafeMap;

public class PipelineHelper {

    public static void printAnnotatedDocument(Annotation document, List<Class<? extends TypesafeMap.Key<String>>> fields) {
        Hashtable<Class<? extends TypesafeMap.Key<String>>,String> formatStr = new Hashtable<>();
        formatStr.put(CoreAnnotations.TextAnnotation.class, "%25s");
        String defaultFormatStr = "%15s";
        for (Class<? extends TypesafeMap.Key<String>> field: fields) {
            System.out.format(formatStr.containsKey(field)?formatStr.get(field):defaultFormatStr,
                    field.getName()
                            .replaceAll(".*Annotations\\$","")
                            .replaceAll("Annotation","")
                            .replaceAll("Tag",""));
        }
        System.out.println();
        for (Class<? extends TypesafeMap.Key<String>> field: fields) {
            System.out.format(formatStr.containsKey(field)?formatStr.get(field):defaultFormatStr, "---------------");
        }
        System.out.println();
        for(CoreMap sentence: document.get(CoreAnnotations.SentencesAnnotation.class)) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                for (Class<? extends TypesafeMap.Key<String>> field: fields) {
                    String tmp = token.get(field);
                    System.out.format(formatStr.containsKey(field)?formatStr.get(field):defaultFormatStr, tmp);
                }
                System.out.println();
            }
            System.out.println();
        }
    }

    public static void createRuleFileFromDictionary(String inFile, String outFile, String annotationLabel) {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        try {
            List<String> lines = Files.readAllLines(Paths.get(inFile), Charset.defaultCharset());
            List<String> outLines = new ArrayList<>();
            for (String line : lines) {
                Annotation doc = new Annotation(line);
                pipeline.annotate(doc);
                List<CoreLabel> tokens = doc.get(CoreAnnotations.TokensAnnotation.class);
                StringBuilder strBuilder = new StringBuilder();
                if (tokens.size() ==0) System.out.println(line);
                for (CoreLabel token : tokens) {
                    strBuilder.append(token.word().replaceAll("\\+","\\\\+") + " ");
                }
                strBuilder.deleteCharAt(strBuilder.length()-1);
                strBuilder.append("\t" + annotationLabel + "\t");
                strBuilder.append("PERSON,LOCATION,ORGANIZATION,MISC,MONEY,NUMBER,ORDINAL,PERCENT,DATE,TIME,DURATION,SET,EMAIL,URL,CITY,STATE_OR_PROVINCE,COUNTRY,NATIONALITY,RELIGION,TITLE,IDEOLOGY,CRIMINAL_CHARGE,CAUSE_OF_DEATH");
                strBuilder.append("\t1");
                outLines.add(strBuilder.toString());
            }
            outLines.sort((a,b) -> b.length()-a.length());
            Files.write(Paths.get(outFile), outLines, Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Class<? extends TypesafeMap.Key<String>>> addPopularRegexRuleAnnotators(AnnotationPipeline pipeline) {
        String CONFERENCE_MAPPING = "mapping_files/conference_list.rules";
        String CONFERENCE_ACRONYM_MAPPING = "mapping_files/conference_acronym_list.rules";
        String COURSE_MAPPING = "mapping_files/course_list.rules";
        String JOURNAL_MAPPING = "mapping_files/journal_list.rules";
        String PROFESSOR_MAPPING = "mapping_files/professor_list.rules";
        String SPONSOR_MAPPING = "mapping_files/sponsor_agency_list.rules";
        String TOPIC_MAPPING = "mapping_files/topic_list.rules";
        String REGEX_MAPPING = "mapping_files/regex_list.rules";

        pipeline.addAnnotator(new CustomizableFieldRegexNERAnnotator(CONFERENCE_MAPPING, false,CustomizableCoreAnnotations.ConferenceTagAnnotation.class));
        pipeline.addAnnotator(new CustomizableFieldRegexNERAnnotator(CONFERENCE_ACRONYM_MAPPING, true, CustomizableCoreAnnotations.ConferenceAcronymTagAnnotation.class));
        pipeline.addAnnotator(new CustomizableFieldRegexNERAnnotator(COURSE_MAPPING, false,CustomizableCoreAnnotations.CourseTagAnnotation.class));
        pipeline.addAnnotator(new CustomizableFieldRegexNERAnnotator(JOURNAL_MAPPING, false,CustomizableCoreAnnotations.JournalTagAnnotation.class));
        pipeline.addAnnotator(new CustomizableFieldRegexNERAnnotator(PROFESSOR_MAPPING, false, CustomizableCoreAnnotations.ProfessorTagAnnotation.class));
        pipeline.addAnnotator(new CustomizableFieldRegexNERAnnotator(SPONSOR_MAPPING, true, CustomizableCoreAnnotations.SponsorAgencyTagAnnotation.class));
        pipeline.addAnnotator(new CustomizableFieldRegexNERAnnotator(TOPIC_MAPPING, false, CustomizableCoreAnnotations.TopicTagAnnotation.class));
        pipeline.addAnnotator(new CustomizableFieldRegexNERAnnotator(REGEX_MAPPING, true, CustomizableCoreAnnotations.RegexTagAnnotation.class));

        List<Class<? extends TypesafeMap.Key<String>>> fields = new ArrayList();
        fields.add(CoreAnnotations.TextAnnotation.class);
        fields.add(CoreAnnotations.NamedEntityTagAnnotation.class);
        fields.add(CustomizableCoreAnnotations.RegexNERAnnotation.class);
        fields.add(CustomizableCoreAnnotations.ConferenceTagAnnotation.class);
        fields.add(CustomizableCoreAnnotations.ConferenceAcronymTagAnnotation.class);
        fields.add(CustomizableCoreAnnotations.RegexTagAnnotation.class);
        fields.add(CustomizableCoreAnnotations.CourseTagAnnotation.class);
        fields.add(CustomizableCoreAnnotations.JournalTagAnnotation.class);
        fields.add(CustomizableCoreAnnotations.TopicTagAnnotation.class);
        fields.add(CustomizableCoreAnnotations.SponsorAgencyTagAnnotation.class);
        fields.add(CustomizableCoreAnnotations.ProfessorTagAnnotation.class);

        return fields;
    }

    public static void main(String[] args) {
        createRuleFileFromDictionary("mapping_files/backup/course_list.txt","mapping_files/course_list.rules", "COURSE");
    }
}
