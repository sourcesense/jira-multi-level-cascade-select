package com.sourcesense.jira.customfield;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.customfields.impl.CascadingSelectCFType;
import com.atlassian.jira.issue.customfields.impl.rest.AbstractCustomFieldOperationsHandler;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.rest.StandardOperation;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.beans.CustomFieldOptionJsonBean;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @since v5.0
 */
public class MultiLevelCascadingSelectCustomFieldOperationsHandler extends AbstractCustomFieldOperationsHandler<Map<String, Option>>
{
    private final OptionsManager optionsManager;

    public MultiLevelCascadingSelectCustomFieldOperationsHandler(OptionsManager optionsManager, CustomField field, I18nHelper i18nHelper)
    {
        super(field, i18nHelper);
        this.optionsManager = optionsManager;
    }

    @Override
    public Set<String> getSupportedOperations()
    {
        return ImmutableSet.of(StandardOperation.SET.getName());
    }

    
    /**
     * Updated to 5.04 TO CHECK
     * */
    @Override
    protected Map<String, Option> handleSetOperation(IssueContext issueCtx, Issue issue, Map<String, Option> currentFieldValue, JsonData operationValue, ErrorCollection errors)
    {
        if (operationValue.isNull())
        {
            return null;
        }

        // The set should be an object containing a parent and optionally a child OptionBean
        // Options can be specified by Id or Value.  Id has priority as always.
        Option parent = null;
        Option child = null;
        CustomFieldOptionJsonBean bean = operationValue.convertValue(field.getId(), CustomFieldOptionJsonBean.class, errors);
        if (bean == null)
        {
            return null;
        }
        FieldConfig config = field.getRelevantConfig(issueCtx);
        Collection<Option> allowedOptions = optionsManager.getOptions(config);
        if (bean.getId() != null)
        {
            parent = findOptionById(bean.getId(), field, errors);
            if (parent == null || !allowedOptions.contains(parent))
            {
                errors.addError(field.getId(), i18nHelper.getText("rest.custom.field.option.id.invalid", bean.getId()), ErrorCollection.Reason.VALIDATION_FAILED);
                parent = null;
            }
        }
        else
        {
            String value = bean.getValue();
            if (value != null)
            {
                for (Option option : allowedOptions)
                {
                    if (option.getValue().equals(value))
                    {
                        parent = option;
                        break;
                    }
                }
                if (parent == null)
                {
                    errors.addError(field.getId(), i18nHelper.getText("rest.custom.field.option.value.invalid", value), ErrorCollection.Reason.VALIDATION_FAILED);
                }
            }
            else
            {
                errors.addError(field.getId(), i18nHelper.getText("rest.custom.field.option.parent.no.name.or.id"), ErrorCollection.Reason.VALIDATION_FAILED);
            }
        }
        Map<String, Option> options = new HashMap<String, Option>();
        options.put(CascadingSelectCFType.PARENT_KEY, parent);
        List<Option> children;
        int i=1;
        while (parent != null)
        {
            children = parent.getChildOptions();
            // Get the child option id if present
            CustomFieldOptionJsonBean beanChild = bean.getChild();
            if (beanChild != null)
            {
                if (beanChild.getId() != null)
                {
                    child = findOptionById(beanChild.getId(), field, errors);
                    if (child == null || !children.contains(child))
                    {
                        errors.addError(field.getId(), i18nHelper.getText("rest.custom.field.child.option.id.invalid", beanChild.getId()), ErrorCollection.Reason.VALIDATION_FAILED);
                    }
                }
                else if (beanChild.getValue() != null)
                {
                    for (Option option : children)
                    {
                        if (option.getValue().equals(beanChild.getValue()))
                        {
                            child = option;
                            break;
                        }
                    }
                    if (child == null)
                    {
                        errors.addError(field.getId(), i18nHelper.getText("rest.custom.field.child.option.value.invalid", beanChild.getValue()), ErrorCollection.Reason.VALIDATION_FAILED);
                    }
                }
                else
                {
                    errors.addError(field.getId(), i18nHelper.getText("rest.custom.field.child.option.parent.no.name.or.id"), ErrorCollection.Reason.VALIDATION_FAILED);
                }
                if (child != null)
                {
                    options.put(""+i, child);
                   
                }
                parent=child;
                i++;
            }
        }
        return options;
    }

    /**
     * compute the "currentValue" to be passed to applyOperation()
     */
    protected Map<String, Option> getInitialValue(Issue issue, ErrorCollection errors)
    {
        return (Map<String, Option>) field.getValue(issue);
    }

    /**
     * compute the "currentValue" to be passed to applyOperation()
     * @param issueCtx
     */
    protected Map<String, Option> getInitialCreateValue(IssueContext issueCtx)
    {
        FieldConfig config = field.getRelevantConfig(issueCtx);
        return (Map<String, Option>) field.getCustomFieldType().getDefaultValue(config);
    }

    /**
     * Updated to 5.0.4 TO CHECK
     */
    @Override
    protected void finaliseOperation(Map<String, Option> finalValue, IssueInputParameters parameters, ErrorCollection errors)
    {
        if (finalValue != null)
        {
          for(int i=0;i<finalValue.size();i++){
            if (finalValue.get(""+i) != null)
            {
                parameters.addCustomFieldValue(field.getId(), finalValue.get(""+i).getOptionId().toString());
            }
          }
        }
        else
        {
            parameters.addCustomFieldValue(field.getId(), null);
        }
    }

    /**
     * Returns the Option that has the given <code>optionId</code>, or null if there is no Option with the given id or
     * if the given id is not valid. When returning null, the
     *
     * @param optionId a String containing an option id
     * @param field the Field
     * @param errors an ErrorCollection where errors will be added
     * @return an Option or null
     */
    private Option findOptionById(String optionId, CustomField field, ErrorCollection errors)
    {
        try
        {
            return optionsManager.findByOptionId(Long.valueOf(optionId));
        }
        catch (NumberFormatException e)
        {
            errors.addError(field.getId(),  i18nHelper.getText("rest.custom.field.option.id.invalid", optionId), ErrorCollection.Reason.VALIDATION_FAILED);
            return null;
        }
    }
}

