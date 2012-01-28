package com.jthalbert;

import java.util.*;

/**
 * Hello world!
 *
 */
public class Utilities
{
    public static Map<String,Long> createHistogram(Collection<String> collection) {
        Map<String,Long> retVal = new HashMap<String, Long>();
        for (String item : collection) {
            if (retVal.containsKey(item)) {
                Long count = retVal.get(item);
                retVal.put(item,count + 1l);
            } else {
                retVal.put(item,1l);
            }
        }
        return retVal;
    }
    public static void main( String[] args )
    {
        String[] test = {"A","A","B","C"};
        List<String> testList = new ArrayList<String>(Arrays.asList(test));
        System.out.println(createHistogram(testList));
    }
}
