package org.forward.entitysearch.ner.annotation.extraction;

import org.forward.entitysearch.ner.annotation.EntityCatalog;

/**
 * Abstract class for all entity extraction techniques
 * @author aaulabaugh@gmail.com
 */
public abstract class EntityExtractor
{
	
	protected EntityCatalog catalog;
	
	public EntityExtractor(EntityCatalog cat)
	{
		catalog = cat;
	}
	
	/**
	 * Used for any final print statements after annotation finishes.
	 */
	public void close()
	{
		//do nothing by default
	}
	
	/**
	 * Given input text, generate an array of EntityAnnotations.
	 * @param text
	 * @return
	 */
	public abstract void extractEntities(String text);
}
