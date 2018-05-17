package org.forward.entitysearch.ner.annotation.extraction;

import org.forward.entitysearch.ner.annotation.EntityAnnotation;
import org.forward.entitysearch.ner.annotation.EntityCatalog;

import java.io.IOException;
import java.io.StringReader;

/**
 * Wraps the jflex generated scanner to get all the entities at once
 * @author aaulabaugh@gmail.com
 */

public class PhoneEmailExtractor extends EntityExtractor
{
	private PhoneEmailExtractorImpl scanner;
		
	/**
	 * Initializes the jflex scanner
	 */
	public PhoneEmailExtractor(EntityCatalog cat)
	{
		super(cat);
		scanner = new PhoneEmailExtractorImpl(new StringReader(""));
		scanner.setCatalog(cat);
	}
	
	@Override
	public void extractEntities(String text)
	{
		scanner.yyreset(new StringReader(text));
		EntityAnnotation entity;
		try
		{
			while ((entity = scanner.getNextToken())!=null)
				catalog.addAnnotation(entity);
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
	}

}
