package org.forward.entitysearch.experiment;

import org.apache.commons.codec.digest.DigestUtils;
import org.forward.entitysearch.ingestion.ESAnnotatedHTMLDocument;
import org.json.simple.JSONObject;

import java.io.*;
import java.util.ArrayList;

public class CreateImportFileForElasticSearch {
    public static void main (String[] args) throws IOException {

        /*
         * Run this command after exporting:
         * curl -XPOST localhost:9200/test_annotation/_bulk -H ‘Content-Type: application/json’ --data-binary @test.json
         */

        ArrayList<String> fields = new ArrayList<>();

        fields.add("_entity_NamedEntityTag");
        fields.add("_entity_RegexNER");
        fields.add("_entity_Type");
        fields.add("_entity_ConferenceTag");
        fields.add("_entity_ConferenceAcronymTag");
        fields.add("_entity_RegexTag");
        fields.add("_entity_CourseTag");
        fields.add("_entity_JournalTag");
        fields.add("_entity_TopicTag");
        fields.add("_entity_ProfessorTag");
        fields.add("_entity_SponsorAgencyTag");

        fields.add("_layout_X_NamedEntityTag");
        fields.add("_layout_X_RegexNER");
        fields.add("_layout_X_Type");
        fields.add("_layout_X_ConferenceTag");
        fields.add("_layout_X_ConferenceAcronymTag");
        fields.add("_layout_X_RegexTag");
        fields.add("_layout_X_CourseTag");
        fields.add("_layout_X_JournalTag");
        fields.add("_layout_X_TopicTag");
        fields.add("_layout_X_ProfessorTag");
        fields.add("_layout_X_SponsorAgencyTag");

        fields.add("_layout_Y_NamedEntityTag");
        fields.add("_layout_Y_RegexNER");
        fields.add("_layout_Y_Type");
        fields.add("_layout_Y_ConferenceTag");
        fields.add("_layout_Y_ConferenceAcronymTag");
        fields.add("_layout_Y_RegexTag");
        fields.add("_layout_Y_CourseTag");
        fields.add("_layout_Y_JournalTag");
        fields.add("_layout_Y_TopicTag");
        fields.add("_layout_Y_ProfessorTag");
        fields.add("_layout_Y_SponsorAgencyTag");

        ArrayList<String> tFields = new ArrayList<>();

        tFields.add("_title_NamedEntityTag");
        tFields.add("_title_RegexNER");
        tFields.add("_title_Type");
        tFields.add("_title_ConferenceTag");
        tFields.add("_title_ConferenceAcronymTag");
        tFields.add("_title_RegexTag");
        tFields.add("_title_CourseTag");
        tFields.add("_title_JournalTag");
        tFields.add("_title_TopicTag");
        tFields.add("_title_ProfessorTag");
        tFields.add("_title_SponsorAgencyTag");

        BufferedWriter outputJson = new BufferedWriter(new FileWriter(args[1]));

        File folder = new File(args[0]);
        File[] listOfFiles = folder.listFiles();
        for (File file : listOfFiles) {
            if (file.isFile()) {
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
                if (doc != null) {
                    JSONObject obj = new JSONObject();
                    obj.put("title", doc.getTitle());
                    obj.put("url", doc.getURL());
                    for (String field : fields) {
                        String fileName = field+"_DO_LOAD_SERIALIZED_FILE_" + file.getAbsolutePath();
                        obj.put(field,fileName);
                    }
                    String titleFileName = file.getParentFile().getParent() + "/serializedTitles/" + DigestUtils.md5Hex(doc.getURL()) + ".ser";
                    File titleFile = new File(titleFileName);
                    if (titleFile.exists()) {
                        // System.out.println(titleFile.getAbsolutePath());
                        for (String field : tFields) {
                            String fileName = field+"_DO_LOAD_SERIALIZED_FILE_" +  titleFile.getAbsolutePath();
                            obj.put(field,fileName);
                        }
                    }
                    obj.put("text", "_DO_LOAD_SERIALIZED_FILE_" + file.getAbsolutePath());
                    outputJson.write("{\"index\": {\"_type\": \"document\"}}\n");
                    outputJson.write(obj.toJSONString()+"\n");
                }
            }
        }
        outputJson.close();
    }
}
