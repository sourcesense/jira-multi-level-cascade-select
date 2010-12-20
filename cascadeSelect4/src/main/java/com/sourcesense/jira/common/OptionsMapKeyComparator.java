package com.sourcesense.jira.common;

import java.util.Comparator;

/**
 * This class (from the jira 3.0 plug-in) is a comparator for the optionsMap class.
 * @author Alessandro Benedetti
 *
 */
public class OptionsMapKeyComparator implements Comparator<String> {
    public int compare(String o1, String o2) {
      if (o1==null || o2==null) {
        if (o1==o2) {
            return 0;
        }
        if (o1==null) {
            return -1;
        }
        return 1;
    }

    return o1.toString().compareTo(o2.toString());
    }
}
