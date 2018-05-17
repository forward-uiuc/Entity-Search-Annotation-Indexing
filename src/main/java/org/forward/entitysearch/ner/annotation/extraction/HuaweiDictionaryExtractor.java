package org.forward.entitysearch.ner.annotation.extraction;

import org.forward.entitysearch.ner.annotation.extraction.huawei_tagging.ChineseQuestionTagging;
import org.forward.entitysearch.ner.annotation.EntityAnnotation;
import org.forward.entitysearch.ner.annotation.EntityCatalog;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;

public class HuaweiDictionaryExtractor extends EntityExtractor
{
	private HashMap<Integer, String> annotations;
	private String dictFolder;
	public HuaweiDictionaryExtractor(EntityCatalog cat, String dictFolder)
	{
		super(cat);
		this.dictFolder = dictFolder;
		annotations = new HashMap<Integer, String>();
	}
	
	public void generateAnnotation(String inputPath)
	{
		String[] dictionaryPaths=new String[5];
		dictionaryPaths[0]= dictFolder + "/generalwords.v2.csv";
		dictionaryPaths[1]= dictFolder + "professionalwords.v2.csv";
		dictionaryPaths[2]= dictFolder + "product.v3.txt_update_update";
		dictionaryPaths[3]= dictFolder + "symptom.v3.txt_update_update";
		dictionaryPaths[4]= dictFolder + "A.v3.txt_update_update";
		String outputPath= dictFolder + "entitySearchLabelResults";
		
		ChineseQuestionTagging tagger = new ChineseQuestionTagging();
		tagger.runAnnotation(dictionaryPaths, outputPath, inputPath);
		
		try
		{
			BufferedReader huaweiFile = new BufferedReader(new FileReader(outputPath));
			String annotatedDoc = "";
			int lineNum = 0;
			while((annotatedDoc = huaweiFile.readLine()) != null)
			{
				annotations.put(lineNum, annotatedDoc);
				lineNum++;
			}
			huaweiFile.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void extractEntities(String text)
	{
		String[] components = text.split(",");
		int lineNum = Integer.parseInt(components[0]);
		String inputPath = components[1];
		if(lineNum ==0)
			generateAnnotation(inputPath);
		if(!annotations.containsKey(lineNum))
			return;
		String annotation = annotations.get(lineNum);
		String[] annotationComponents = annotation.split("\t");
		for(int i = 1; i < annotationComponents.length; i++)
		{
			String token = annotationComponents[i];
			token = token.substring(1, token.length()-1);
			try
			{
				String[] split1 = token.split("_");
				String[] split2 = split1[1].split("&");
				String content = split1[0];
				String category = split2[0];
				String offset = split2[1];
				EntityAnnotation newAnnotation = new EntityAnnotation();
				newAnnotation.setContent(content);
				newAnnotation.addType(catalog.getEntityType(category));
				newAnnotation.setPosition(Integer.parseInt(offset));
				newAnnotation.setSource("HUAWEIDICT");
				catalog.addAnnotation(newAnnotation);
			}
			catch(Exception e)
			{
				System.out.println("Unexpected tagging output: " + token);
			}
		}
	}

}
