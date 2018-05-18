package org.forward.entitysearch.experiment;

public class TestRegex {

    public static void main (String args[]) {
        String str = "Peter is a man?";
        //Ending with a delimiter
        System.out.println (str.matches(".*\\p{Punct}\\s*"));
    }
}
