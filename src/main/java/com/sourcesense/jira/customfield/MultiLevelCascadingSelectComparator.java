package com.sourcesense.jira.customfield;

import java.util.Comparator;

/**
 * @author Alessandro Benedetti
 *
 */
public class MultiLevelCascadingSelectComparator implements Comparator<MultiLevelCascadingSelectValue> {

    @Override
    public int compare(MultiLevelCascadingSelectValue o1, MultiLevelCascadingSelectValue o2) {
      if (o1 == null && o2 == null)
      {
          return 0;
      }
      else if (o1 == null||o1.getSearchValue()==null)
      {
          return 1;
      }
      else if (o2 == null||o2.getSearchValue()==null)
      {
          return -1;
      }
      return o1.toString().compareTo(o2.toString());
    }
}
