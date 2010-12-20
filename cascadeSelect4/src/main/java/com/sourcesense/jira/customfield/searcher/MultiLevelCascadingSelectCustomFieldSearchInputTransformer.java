package com.sourcesense.jira.customfield.searcher;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

import com.atlassian.jira.bc.issue.search.QueryContextConverter;
import com.atlassian.jira.issue.customfields.converters.SelectConverter;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.searchers.transformer.AbstractCustomFieldSearchInputTransformer;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.search.searchers.transformer.SimpleNavigatorCollectorVisitor;
import com.atlassian.jira.jql.context.QueryContext;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.util.JqlCascadingSelectLiteralUtil;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.util.NonInjectableComponent;
import com.atlassian.query.Query;
import com.atlassian.query.clause.Clause;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.clause.TerminalClauseImpl;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import com.atlassian.query.operator.Operator;
import com.opensymphony.user.User;
import com.sourcesense.jira.customfield.type.MultiLevelCascadingSelectCFType;

/**
 * The {@link com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer} for
 * multi cascading select custom fields.
 * 
 * @since v4.0
 * @author Alessandro Benedetti
 * 
 */
@NonInjectableComponent
public class MultiLevelCascadingSelectCustomFieldSearchInputTransformer extends AbstractCustomFieldSearchInputTransformer implements SearchInputTransformer {
  private final ClauseNames clauseNames;

  private final CustomField customField;

  private final SelectConverter selectConverter;

  private final JqlOperandResolver jqlOperandResolver;

  private final JqlSelectOptionsUtil jqlSelectOptionsUtil;

  private final JqlCascadingSelectLiteralUtil jqlCascadingSelectLiteralUtil;

  private final QueryContextConverter queryContextConverter;

  public MultiLevelCascadingSelectCustomFieldSearchInputTransformer(final ClauseNames clauseNames, final CustomField field, final String urlParameterName, final SelectConverter selectConverter,
          final JqlOperandResolver jqlOperandResolver, final JqlSelectOptionsUtil jqlSelectOptionsUtil, final JqlCascadingSelectLiteralUtil jqlCascadingSelectLiteralUtil,
          final QueryContextConverter queryContextConverter, final CustomFieldInputHelper customFieldInputHelper) {
    super(field, urlParameterName, customFieldInputHelper);
    this.queryContextConverter = notNull("queryContextConverter", queryContextConverter);
    this.jqlCascadingSelectLiteralUtil = notNull("jqlCascadingSelectLiteralUtil", jqlCascadingSelectLiteralUtil);
    this.clauseNames = notNull("clauseNames", clauseNames);
    this.customField = notNull("field", field);
    this.selectConverter = notNull("selectConverter", selectConverter);
    this.jqlOperandResolver = notNull("jqlOperandResolver", jqlOperandResolver);
    this.jqlSelectOptionsUtil = notNull("jqlSelectOptionsUtil", jqlSelectOptionsUtil);
  }

  public boolean doRelevantClausesFitFilterForm(final User searcher, final Query query, final SearchContext searchContext) {
    return getParamsFromSearchRequest(searcher, query, searchContext) != null;
  }

  /**
   * returns the Clause built from the customFieldParams in input.
   * 
   * 
   * */
  protected Clause getClauseFromParams(final User searcher, final CustomFieldParams customFieldParams) {
    TreeMap<Integer, String> orderedFunctionArgs = new TreeMap<Integer, String>();
    final LinkedList<String> functionArgs = new LinkedList<String>();
    String invalidStringOperand = null;
    Long invalidLongOperand = null;
    for (String level : customFieldParams.getAllKeys()) {
      Long longOptionValue = null;
      String stringOptionValue = null;
      if (level != null)
        stringOptionValue = getValue(customFieldParams.getValuesForKey(level));
      else
        stringOptionValue = getValue(customFieldParams.getValuesForKey(MultiLevelCascadingSelectCFType.PARENT_KEY));
      try {
        longOptionValue = new Long(stringOptionValue);
      } catch (NumberFormatException e) {
        // invalid inputs - we will use the string values instead to build our clause
      }
      if (longOptionValue != null) {
        final Option option = jqlSelectOptionsUtil.getOptionById(longOptionValue);
        if (option != null) {
          int levelNumber;
          if (level != null)
            levelNumber = Integer.parseInt(level);
          else
            levelNumber = 0;
          orderedFunctionArgs.put(levelNumber, option.getOptionId().toString());
        } else {
          invalidLongOperand = longOptionValue;
        }
      } else if (stringOptionValue != null) {
        invalidStringOperand = stringOptionValue;
      }
    }
    /* the funcion args is build in order, from the level 0 to the level N */
    for (Integer s : orderedFunctionArgs.keySet()) {
      functionArgs.addLast(orderedFunctionArgs.get(s));
    }
    final String clauseName = getClauseName(searcher, clauseNames);
    if (invalidStringOperand != null || invalidLongOperand != null) {
      final Operand o = invalidStringOperand != null ? new SingleValueOperand(invalidStringOperand) : new SingleValueOperand(invalidLongOperand);
      return new TerminalClauseImpl(clauseName, Operator.EQUALS, o);
    } else if (!functionArgs.isEmpty()) {
      return new TerminalClauseImpl(clauseName, Operator.IN, new FunctionOperand(MultiLevelCascadeOptionFunction.FUNCTION_CASCADE_OPTION, functionArgs));
    } else {
      return null;
    }
  }

  protected CustomFieldParams getParamsFromSearchRequest(final User searcher, final Query query, final SearchContext searchContext) {
    if (query != null && query.getWhereClause() != null) {
      SimpleNavigatorCollectorVisitor visitor = createSimpleNavigatorCollectingVisitor();
      query.getWhereClause().accept(visitor);

      // check that the structure is valid
      if (!visitor.isValid()) {
        return null;
      }
      final List<TerminalClause> clauses = visitor.getClauses();

      // check that we only have one clause
      if (clauses.size() != 1) {
        return null;
      }

      final TerminalClause clause = clauses.get(0);

      // check that we have a valid operator
      final Operator operator = clause.getOperator();
      if (operator != Operator.EQUALS && operator != Operator.IS && operator != Operator.IN) {
        return null;
      }

      final List<QueryLiteral> literals = jqlOperandResolver.getValues(searcher, clause.getOperand(), clause);
      if (literals == null || jqlCascadingSelectLiteralUtil.isNegativeLiteral(literals.get(0))) {
        if (clause.getOperand() instanceof FunctionOperand) {
          return handleInvalidFunctionOperand(clause);
        }
        return null;
      }

      // check that we are searching for non-empty value
      final QueryLiteral literal = literals.get(0);
      if (literal.isEmpty()) {
        return null;
      }

      // check that the options resolved are in context
      final QueryContext queryContext = queryContextConverter.getQueryContext(searchContext);
      List<Option> options = new ArrayList<Option>();
      for (QueryLiteral l : literals)
        options.addAll(jqlSelectOptionsUtil.getOptions(customField, l, true));
      // options.addAll(jqlSelectOptionsUtil.getOptions(customField, queryContext, l, true));
      CustomFieldParams customFieldParams = new CustomFieldParamsImpl(customField);
      if (options.size() == 0) {
        customFieldParams.put(MultiLevelCascadingSelectCFType.PARENT_KEY, Collections.singleton(literal.asString()));
      } else {
        int counter = options.size() - 1;
        for (Option opt : options) {
          if (counter > 0) {
            String key = "" + counter;
            customFieldParams.put(key, Collections.singleton(opt.getOptionId().toString()));
          } else {
            String key = null;
            customFieldParams.put(key, Collections.singleton(opt.getOptionId().toString()));
          }
          counter--;
        }
      }
      //secondo me sono al contrario????
      return customFieldParams;
    }

    return null;
  }

  /**
   * this method came from the simple cascading select, probably doesn't need any change for
   * multilevel
   */
  private CustomFieldParams handleInvalidFunctionOperand(final TerminalClause clause) {
    CustomFieldParams customFieldParams = null;
    FunctionOperand fop = (FunctionOperand) clause.getOperand();
    if (fop.getName().equals(MultiLevelCascadeOptionFunction.FUNCTION_CASCADE_OPTION)) {
      if (fop.getArgs().size() == 2) {
        customFieldParams = new CustomFieldParamsImpl(customField);
        customFieldParams.put(MultiLevelCascadingSelectCFType.PARENT_KEY, Collections.singleton(fop.getArgs().get(0)));
        customFieldParams.put(MultiLevelCascadingSelectCFType.CHILD_KEY, Collections.singleton(fop.getArgs().get(1)));
      } else if (fop.getArgs().size() == 1) {
        customFieldParams = new CustomFieldParamsImpl(customField);
        customFieldParams.put(MultiLevelCascadingSelectCFType.PARENT_KEY, Collections.singleton(fop.getArgs().get(0)));
      }
    }
    return customFieldParams;
  }

  private String getValue(final Collection values) throws NumberFormatException {
    if (values == null || values.isEmpty()) {
      return null;
    }
    return selectConverter.getObject((String) values.iterator().next());
  }

  // /CLOVER:OFF
  SimpleNavigatorCollectorVisitor createSimpleNavigatorCollectingVisitor() {
    return new SimpleNavigatorCollectorVisitor(clauseNames.getJqlFieldNames());
  }
  // /CLOVER:ON
}
