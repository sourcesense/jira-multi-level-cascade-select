package com.sourcesense.jira.customfield;

import java.util.Comparator;

import junit.framework.TestCase;

/**
 * User: fabio
 * Date: Jun 13, 2007
 * Time: 12:32:48 PM
 */
public class TestMultiLevelCascadingSelectComparator extends TestCase {
    public void testComparator() {
        Comparator comparator=new MultiLevelCascadingSelectComparator();
        assertEquals(1, comparator.compare(null, "1"));
        assertEquals(-1, comparator.compare("1", null));
        assertTrue(comparator.compare(new Long(3), "1")>0);
        assertEquals(0, comparator.compare(new Long(3), "3"));
        assertEquals(0, comparator.compare(null, null));
    }
}
