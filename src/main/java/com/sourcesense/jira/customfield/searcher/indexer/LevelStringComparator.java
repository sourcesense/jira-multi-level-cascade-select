package com.sourcesense.jira.customfield.searcher.indexer;

import java.util.Comparator;

public class LevelStringComparator implements Comparator<String> {

  @Override
  public int compare(String o1, String o2) {
    if(o1==null||o1.equals("null"))
    o1="0";
    if(o2==null||o2.equals("null"))
      o2="0";
    return o1.compareTo(o2);
  }

}
