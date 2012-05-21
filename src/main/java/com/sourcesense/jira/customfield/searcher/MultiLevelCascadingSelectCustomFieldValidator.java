package com.sourcesense.jira.customfield.searcher;


import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.jql.validator.ClauseValidator;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.NonInjectableComponent;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import com.atlassian.crowd.embedded.api.User;

import java.util.ArrayList;
import java.util.List;

/**
 * A validator for multi level cascading select custom fields. Takes into account
 * if the user has any context under which she can see the given options.
 *
 * @since v4.0
 */
@NonInjectableComponent
public class MultiLevelCascadingSelectCustomFieldValidator extends MultiSelectCustomFieldValidator implements ClauseValidator
{
    private final JqlSelectOptionsUtil jqlSelectOptionsUtil;
    private final CustomField customField;

    public MultiLevelCascadingSelectCustomFieldValidator(final CustomField customField, final JqlSelectOptionsUtil jqlSelectOptionsUtil, final JqlOperandResolver jqlOperandResolver, final I18nHelper.BeanFactory beanFactory)
    {
        super(customField, jqlSelectOptionsUtil, jqlOperandResolver, beanFactory);
        this.customField = notNull("customField", customField);
        this.jqlSelectOptionsUtil = notNull("jqlSelectOptionsUtil", jqlSelectOptionsUtil);
    }

    
    protected List<Option> getOptionsFromLiteral(final User searcher, final QueryLiteral literal)
    {
        final List<Option> options = new ArrayList<Option>();
        options.addAll(jqlSelectOptionsUtil.getOptions(customField, searcher, literal, true));
        if (literal.getLongValue() != null && literal.getLongValue() < 0)
        {
            options.addAll(jqlSelectOptionsUtil.getOptions(customField, searcher, new QueryLiteral(literal.getSourceOperand(), -literal.getLongValue()), true));
        }
        return options;
    }
    
}
