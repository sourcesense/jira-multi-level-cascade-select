package com.sourcesense.jira.customfield;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;

/**
 * @author Alessandro Benedetti
 *
 */
public class MultiLevelCascadingSelectValue {
    private String value;
    private OptionsManager optionsManager;
    
    private static final Logger log = Logger.getLogger(MultiLevelCascadingSelectValue.class);

    public MultiLevelCascadingSelectValue(OptionsManager optionsManager, String value) {
        this.optionsManager = optionsManager;
        this.value = value;
    }

    public String getSearchValue() {
        return value;// try to comment 2.4.x
    }

  

    @Override
    public String toString() {
        return value;
    }

   
}