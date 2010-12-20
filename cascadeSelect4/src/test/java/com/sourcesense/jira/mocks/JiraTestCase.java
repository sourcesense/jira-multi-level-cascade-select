package com.sourcesense.jira.mocks;

import junit.framework.TestCase;

import org.hsqldb.jdbcDriver;

import com.atlassian.jira.ManagerFactory;

/**
 * User: fabio
 * Date: Jun 13, 2007
 * Time: 2:56:49 PM
 */
public class JiraTestCase extends TestCase {
    static {
        new jdbcDriver();
    }

    public JiraTestCase() {
        ManagerFactory.initialise();
        ManagerFactory.quickRefresh();
    }
}
