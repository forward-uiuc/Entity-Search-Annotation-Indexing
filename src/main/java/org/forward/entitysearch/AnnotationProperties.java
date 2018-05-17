package org.forward.entitysearch;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.Set;

public class AnnotationProperties
{
    private final Properties configProp = new Properties();

    private AnnotationProperties()
    {
        //Private constructor to restrict new instances
        //InputStream in = this.getClass().getClassLoader().getResourceAsStream("annotation_config.properties");

        try {
            InputStream in = new FileInputStream(new File("annotation_config.properties")) ;
            configProp.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Solution for singleton pattern
    private static class LazyHolder
    {
        private static final AnnotationProperties INSTANCE = new AnnotationProperties();
    }

    public static AnnotationProperties getInstance()
    {
        return LazyHolder.INSTANCE;
    }

    public String getProperty(String key){
        return configProp.getProperty(key);
    }

    public Set<String> getAllPropertyNames(){
        return configProp.stringPropertyNames();
    }

    public boolean containsKey(String key){
        return configProp.containsKey(key);
    }
}