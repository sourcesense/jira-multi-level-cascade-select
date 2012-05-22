package com.sourcesense.jira.customfield.admin_config;

import java.util.List;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;

/**
 * This class identifies a specific FieldConfigItemType for the Multi Level Cascading Select custom field.
 * @author Alessandro Benedetti
 *
 */

public class SettableMultiLevelOptionsConfigItem4 implements FieldConfigItemType {
    private OptionsManager optionsManager;

    public SettableMultiLevelOptionsConfigItem4(OptionsManager optionsManager)
    {
        this.optionsManager = optionsManager;
    }

    public String getDisplayName()
    {
        return "Multi level options";
    }

    /**
     * Takes in input a context and a fieldlayoutItem and returns the options from the FileConfig.
     * Then it prints these Options with the appropriate method.
     * (non-Javadoc)
     * @see com.atlassian.jira.issue.fields.config.FieldConfigItemType#getViewHtml(com.atlassian.jira.issue.fields.config.FieldConfig, com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem)
     */
    public String getViewHtml(FieldConfig fieldConfig, FieldLayoutItem fieldLayoutItem)
    {
        final Options options = optionsManager.getOptions(fieldConfig);
        return prettyPrintOptions(options);
    }

    public String getObjectKey()
    {
        return "options";
    }

    public Object getConfigurationObject(Issue issue, FieldConfig config)
    {
        return optionsManager.getOptions(config);
    }

    public String getBaseEditUrl()
    {
        return "EditCustomFieldMultiLevelOptions!default.jspa";
    }


    
    /**
     * returns the list of options formatted in html
     * @param sb
     * @param options
     * @return
     */
    private StringBuffer prettyPrintOptions(StringBuffer sb, List<Option> options) {
        if (options != null && !options.isEmpty()) {
            sb.append("<ul class=\"optionslist\">");
            for (Option option:options) {
                sb.append("<li>"+ option.getValue()+"</li>");
                sb = prettyPrintOptions(sb, option.getChildOptions());
            }
            sb.append("</ul>");
        }
        return sb;
    }


    private String prettyPrintOptions(Options options)
    {
        StringBuffer sb = new StringBuffer();

        if (options != null && !options.isEmpty()) {
            sb = prettyPrintOptions(sb, options);
        }
        else {
            sb.append("No options configured.");
        }

        return sb.toString();
    }

    public String getDisplayNameKey() {
      return "Multi level options";
    }
}
