package com.sourcesense.jira.customfield;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.OptionUtils;

/**
 * @author Alessandro Benedetti
 *
 */
public class MultiLevelCascadingSelectValue {
    private String value;
    private OptionsManager optionsManager;

    public MultiLevelCascadingSelectValue(OptionsManager optionsManager, String value) {
        this.optionsManager = optionsManager;
        this.value = value;
    }

    public String getSearchValue() {
        return value;
    }

   
    /**
     * 
     * return the list of options, splitting the string value
     * @return
     */
    private List<String> getOptionList() {
        List<String> list = new ArrayList<String>();
        List<String> values = Arrays.asList(value.split(":"));
        for (String value:values) {
            Long optionId = OptionUtils.safeParseLong(value);
            Option option = optionsManager.findByOptionId(optionId);
            list.add(option.toString());
        }
        return list;
    }

    @Override
    public String toString() {
        return printList(getOptionList(), " - ");
    }

    private String printList(List<String> list, String separator) {
        StringBuffer result =  new StringBuffer();
        for(String value:list) {
            if (result.length() > 0) result.append(separator);
            result.append(value);
        }
        return result.toString();
    }
}