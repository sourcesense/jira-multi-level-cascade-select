package com.sourcesense.jira.customfield.searcher;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

import java.util.List;

import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.jql.validator.ClauseValidator;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operand.Operand;
import com.atlassian.crowd.embedded.api.User;

/**
 * A validator for select custom fields. Takes into account if the user has any context under which
 * she can see the given options.
 * 
 * @since v4.0
 */
public class MultiSelectCustomFieldValidator implements ClauseValidator {
  private final SupportedOperatorsValidator supportedOperatorsValidator;

  private final CustomField customField;

  private final JqlSelectOptionsUtil jqlSelectOptionsUtil;

  private final JqlOperandResolver jqlOperandResolver;

  private final I18nHelper.BeanFactory beanFactory;

  public MultiSelectCustomFieldValidator(final CustomField customField, final JqlSelectOptionsUtil jqlSelectOptionsUtil, final JqlOperandResolver jqlOperandResolver,
          final I18nHelper.BeanFactory beanFactory) {
    this.beanFactory = notNull("beanFactory", beanFactory);
    this.customField = notNull("customField", customField);
    this.jqlSelectOptionsUtil = notNull("jqlSelectOptionsUtil", jqlSelectOptionsUtil);
    this.jqlOperandResolver = notNull("jqlOperandResolver", jqlOperandResolver);
    this.supportedOperatorsValidator = getSupportedOperatorsValidator();
  }

  public MessageSet validate(final User searcher, final TerminalClause terminalClause) {
    MessageSet messageSet = supportedOperatorsValidator.validate(searcher, terminalClause);
    if (!messageSet.hasAnyMessages()) {
      return validateValues(searcher, terminalClause);
    } else {
      return messageSet;
    }
  }

  private MessageSet validateValues(final User searcher, final TerminalClause terminalClause) {
    I18nHelper i18n = getI18n(searcher);
    final MessageSet messageSet = new MessageSetImpl();

    final Operand operand = terminalClause.getOperand();
    final List<QueryLiteral> literals = jqlOperandResolver.getValues(searcher, operand, terminalClause);
    if (literals == null || literals.isEmpty()) {
      return messageSet;
    }

    for (QueryLiteral literal : literals) {
      if (!literal.isEmpty() && !literal.asString().contains(":")) {
        final List<Option> options = getOptionsFromLiteral(searcher, literal);
        if (options.isEmpty()) {
          if (jqlOperandResolver.isFunctionOperand(literal.getSourceOperand())) {
            messageSet.addErrorMessage(i18n.getText("jira.jql.clause.select.option.does.not.exist.function", literal.getSourceOperand().getName(), terminalClause.getName()));
          } else {
            messageSet.addErrorMessage(i18n.getText("jira.jql.clause.select.option.does.not.exist", literal.asString(), terminalClause.getName()));
          }
        }
      }
    }
    return messageSet;
  }

  protected List<Option> getOptionsFromLiteral(final User searcher, final QueryLiteral literal) {
    return jqlSelectOptionsUtil.getOptions(customField, (com.atlassian.crowd.embedded.api.User) searcher, literal, false);
  }

  I18nHelper getI18n(User user) {
    return beanFactory.getInstance(user);
  }

  // /CLOVER:OFF

  SupportedOperatorsValidator getSupportedOperatorsValidator() {
    return new SupportedOperatorsValidator(OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY);
  }

  // /CLOVER:ON
}
