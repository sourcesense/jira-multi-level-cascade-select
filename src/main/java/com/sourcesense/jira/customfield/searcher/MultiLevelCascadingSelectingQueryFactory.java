package com.sourcesense.jira.customfield.searcher;

import static com.atlassian.jira.util.dbc.Assertions.notBlank;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;


import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.query.ClauseQueryFactory;
import com.atlassian.jira.jql.query.QueryCreationContext;
import com.atlassian.jira.jql.query.QueryFactoryResult;
import com.atlassian.jira.jql.util.JqlCascadingSelectLiteralUtil;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.util.NonInjectableComponent;
import com.atlassian.query.clause.TerminalClause;
import com.atlassian.query.operator.Operator;


/**
 * Factory for producing clauses for the multi level cascading select custom fields.
 * This class has the responsibility of building the lucene query from the literals in input.
 * To modify the query generation behaviour ,starting from the options retrieved from the searcher, you have to modify this class.
 * Now it builds the query , using the ID of the last Child (obviously , for costruction if an issue  has a multi level custom field, if 
 * it has a child, it must own also the parents).
 * 
 * @since v4.0
 * @author Alessandro Benedetti
 *
 */
@NonInjectableComponent
public class MultiLevelCascadingSelectingQueryFactory implements ClauseQueryFactory {
  private static final Logger log = Logger.getLogger(MultiLevelCascadingSelectingQueryFactory.class);

  private final JqlCascadingSelectLiteralUtil jqlCascadingSelectLiteralUtil;

  private final JqlSelectOptionsUtil jqlSelectOptionsUtil;

  private final JqlOperandResolver jqlOperandResolver;

  private final String parentFieldName;

  private final String childFieldName;

  private final CustomField customField;
  
  private final OptionsManager optionsManager;
  
  public static String EMPTY_VALUE = "_none_";
  public static String EMPTY_VALUE_ID ="-2";
  public static long EMPTY_VALUE_ID_LONG =-2;

  public MultiLevelCascadingSelectingQueryFactory(final CustomField customField, final String luceneField, final JqlSelectOptionsUtil jqlSelectOptionsUtil,
          final JqlOperandResolver jqlOperandResolver, final JqlCascadingSelectLiteralUtil jqlCascadingSelectLiteralUtil) {
    this.customField = notNull("customField", customField);
    this.jqlSelectOptionsUtil = notNull("jqlSelectOptionsUtil", jqlSelectOptionsUtil);
    this.jqlOperandResolver = notNull("jqlOperandResolver", jqlOperandResolver);
    this.jqlCascadingSelectLiteralUtil = notNull("jqlCascadingSelectLiteralUtil", jqlCascadingSelectLiteralUtil);
    this.parentFieldName = notBlank("luceneField", luceneField);
    this.childFieldName = notBlank("luceneField", luceneField) + ":";
    this.optionsManager=ComponentAccessor.getOptionsManager();
  }

  /**
   * The call to
   * {@link com.atlassian.jira.jql.operand.JqlOperandResolver#getValues(QueryCreationContext,com.atlassian.query.operand.Operand,com.atlassian.query.clause.TerminalClause)}
   * potentially returns positive and negative ids as literals. The presence of negative ids usually
   * signifies that the original clause meant to specify an exclusion of some options, whilst
   * demanding the inclusion of others. For example: the cascadeOption(Parent, none) JQL function
   * can be invoked to specify that only the issues which have the parent option set, but no child
   * option set, should be returned. In this example, getValues() would return positive ids for all
   * the parent options matched by the argument, and then negative ids for all the child options of
   * those parents.
   * 
   * We split the positive and negative ids into two groups, and then generate a query for each such
   * that the positives are included (or excluded if the operator is negating), and the negatives
   * are excluded (or included if the operator is negating). We then combine these to form the total
   * query.
   * 
   * @param queryCreationContext
   *          the context of query creation
   * @param terminalClause
   *          the clause for which this factory is generating a query.
   * @return the result which contains the generated query
   */
  public QueryFactoryResult getQuery(final QueryCreationContext queryCreationContext, final TerminalClause terminalClause) {
    final Operator operator = terminalClause.getOperator();
    final boolean isNegationOperator = isNegationOperator(operator);

    if (!handlesOperator(operator)) {
      log.warn(String.format("Unable to handle operator '%s' for clause '%s'.", operator.getDisplayString(), terminalClause.toString()));
      return QueryFactoryResult.createFalseResult();
    }

    // split the literals into positive and negative, so that we can generate separate queries for
    // each
    final List<QueryLiteral> literals = jqlOperandResolver.getValues(queryCreationContext, terminalClause.getOperand(), terminalClause);
    final List<QueryLiteral> positiveLiterals = new ArrayList<QueryLiteral>();
    final List<QueryLiteral> negativeLiterals = new ArrayList<QueryLiteral>();

    if (literals != null) {
      processPositiveNegativeOptionLiterals(literals, positiveLiterals, negativeLiterals);
    }

    BooleanQuery combined = new BooleanQuery();

    // the terms for each of the queries need to be joined in opposite ways (positives will be
    // SHOULDed, negatives will be MUST_NOTed)
    // unless the operator was negating, in which case this is switched.
    final BooleanQuery positiveQuery = getQueryFromLiterals(isNegationOperator, positiveLiterals);
    final BooleanQuery negativeQuery = getQueryFromLiterals(!isNegationOperator, negativeLiterals);

    if (positiveQuery != null && negativeQuery != null) {
      // if we were given both ids to include and exclude, then if the operator was positive then
      // both queries
      // must be satisfied. however if the operator was negating, then we only require that one
      // matches. (DeMorgan's Law)
      // to understand why, debug here and look at the queries generated.
      final BooleanClause.Occur occur = isNegationOperator ? BooleanClause.Occur.SHOULD : BooleanClause.Occur.MUST;
      combined.add(positiveQuery, occur);
      combined.add(negativeQuery, occur);
    } else if (positiveQuery != null) {
      combined = positiveQuery;
    } else if (negativeQuery != null) {
      combined = negativeQuery;
    }

    return new QueryFactoryResult(combined, false);
  }

  // /CLOVER:OFF
  void processPositiveNegativeOptionLiterals(final List<QueryLiteral> literals, final List<QueryLiteral> positiveLiterals, final List<QueryLiteral> negativeLiterals) {
    jqlCascadingSelectLiteralUtil.processPositiveNegativeOptionLiterals(literals, positiveLiterals, negativeLiterals);
  }

  // /CLOVER:ON

  BooleanQuery getQueryFromLiterals(final boolean negationOperator, final List<QueryLiteral> literals) {
    final List<Long> parentIds = new ArrayList<Long>();
    final List<Long> childIds = new ArrayList<Long>();
    boolean emptyLiteralFound = processParentChildOptionLiterals(literals, parentIds, childIds);
    boolean nonEmptyLiteralsFound = !parentIds.isEmpty() || !childIds.isEmpty();
    boolean anyLiteralsFound = emptyLiteralFound || nonEmptyLiteralsFound;

    // if we didn't actually find anything, return now with null
    if (!anyLiteralsFound) {
      return null;
    }

    // we will handle all negation manually instead of deferring using the QueryFactoryResult
    // this is because the result can be complicated by the addition of the NonEmptyQuery
    BooleanClause.Occur occur = negationOperator ? BooleanClause.Occur.MUST_NOT : BooleanClause.Occur.SHOULD;
    BooleanQuery combined = new BooleanQuery();
    for (Long parentId : parentIds) {
      if (childIds.size() == 0)
        combined.add(createParentTerm(parentId), occur);
    }
    int count = 1;
    for (Long childId : childIds) {
      if (count == childIds.size())
        if(childId==EMPTY_VALUE_ID_LONG){
          combined.add(exactValuePhraseQuery(createStringValueFromLiterals(literals), 0), occur);
        }
        else
        combined.add(createChildTerm(childId, count), occur);
      else
        count++;
    }

    // if we are negating then also exclude EMPTY by default, but only if we actually resolved some
    // options
    if (negationOperator) {
      final BooleanQuery nonEmptyQuery = createNonEmptyQuery();
      if (nonEmptyLiteralsFound) {
        combined.add(nonEmptyQuery, BooleanClause.Occur.MUST);

        // also must add visibility query, since we had at least one MUST_NOT queries added
        combined.add(TermQueryFactory.visibilityQuery(parentFieldName), BooleanClause.Occur.MUST);
      } else {
        // don't unnecessarily nest the query
        // also don't need visibility query, since non empty query is positive
        combined = nonEmptyQuery;
      }
    }
    // if we are not negating and we saw an empty literal, we need to also include empty values
    else if (emptyLiteralFound) {
      final BooleanQuery emptyQuery = createEmptyQuery();
      // don't unnecessarily nest the query
      if (nonEmptyLiteralsFound) {
        combined.add(emptyQuery, BooleanClause.Occur.SHOULD);
      } else {
        combined = emptyQuery;
      }
    }
   this.cleanNoneOptions();
    return combined;
  }

  private void cleanNoneOptions() {
    List<Option> noneOptions = this.optionsManager.findByOptionValue(EMPTY_VALUE);
    for(Option noneOpt:noneOptions)
      this.optionsManager.deleteOptionAndChildren(noneOpt);
    
  }

  private Query exactValuePhraseQuery(String value, int count) {
    PhraseQuery pQuery=new PhraseQuery();
    pQuery.add(new Term(childFieldName+ count, value));
    
   return pQuery;
  }

  /**
   * Accumulates parent and child option ids represented by the specified literals.
   * 
   * @param literals
   *          the literals
   * @param parentIds
   *          the collection of parent ids to add to
   * @param childIds
   *          the collection of child ids to add to
   * @return whether or not an empty literal was seen
   */
  boolean processParentChildOptionLiterals(final List<QueryLiteral> literals, final List<Long> parentIds, final List<Long> childIds) {
    boolean emptyLiteralFound = false;
    if (literals != null && !literals.isEmpty()) {
      for (QueryLiteral literal : literals) {
        if(literal.asString()!=null && literal.asString().contains(":")){
          this.splitMultiLiteral(literal,parentIds,childIds);
        }else{
	        final List<Option> optionList = jqlSelectOptionsUtil.getOptions(customField, literal, true);
	        for (Option option : optionList) {
	          if (option != null) {
	            if (option.getParentOption() == null&&!(option.getValue().equals(EMPTY_VALUE))) {
	              parentIds.add(option.getOptionId());
	            } else if(option.getValue().equals(EMPTY_VALUE)){
	              childIds.add(EMPTY_VALUE_ID_LONG);
	              this.optionsManager.deleteOptionAndChildren(option);
	            }
	            else {
	              childIds.add(option.getOptionId());
	            }
	          } else {
	            // caller needs to know if an empty literal was seen
	            emptyLiteralFound = true;
	          }
	        }
	    }
      }
    }

    return emptyLiteralFound;
  }
  
  
  /**
   * returns the serialized string that contain the values of the multi level cascading levels.
   * It's the value indexed in custom_field_id:0
   * @param literals
   * @return
   */
  private String createStringValueFromLiterals(final List<QueryLiteral> literals) {
    StringBuilder resultString=new StringBuilder();
    if (literals != null && !literals.isEmpty()) {
      for (QueryLiteral literal : literals) {
          final List<Option> optionList = jqlSelectOptionsUtil.getOptions(customField, literal, true);
          for (Option option : optionList) {
            if (option != null) {
                resultString.append(option.getValue()+" - ");
            }
          }
      
      }
    }
    return resultString.substring(0, resultString.length()-3);
  }

  private void splitMultiLiteral(QueryLiteral literal, List<Long> parentIds, List<Long> childIds) {
    String[] splittedOptions = literal.asString().split(":");
    for(int i=0;i<splittedOptions.length;i++){
      if(i==0)
        parentIds.add(new Long(splittedOptions[i]));
      else
        childIds.add(new Long(splittedOptions[i]));
    }
    
  }

  private Query createParentTerm(final Long parentId) {
    return new TermQuery(new Term(parentFieldName, parentId.toString()));
  }

  private Query createChildTerm(final Long childId, int level) {
    return new TermQuery(new Term(childFieldName+ level, childId.toString()));
  }

  private BooleanQuery createNonEmptyQuery() {
    // for this field to be NonEmpty, either the parent or the child should be specified
    final BooleanQuery query = new BooleanQuery();
    query.add(TermQueryFactory.nonEmptyQuery(parentFieldName), BooleanClause.Occur.SHOULD);
    query.add(TermQueryFactory.nonEmptyQuery(childFieldName), BooleanClause.Occur.SHOULD);
    return query;
  }

  private BooleanQuery createEmptyQuery() {
    final BooleanQuery parentQuery = new BooleanQuery();
    parentQuery.add(TermQueryFactory.nonEmptyQuery(parentFieldName), BooleanClause.Occur.MUST_NOT);
    parentQuery.add(TermQueryFactory.visibilityQuery(parentFieldName), BooleanClause.Occur.MUST);

    final BooleanQuery query = new BooleanQuery();
    query.add(parentQuery, BooleanClause.Occur.MUST);
    query.add(TermQueryFactory.nonEmptyQuery(childFieldName), BooleanClause.Occur.MUST_NOT);
    return query;
  }

  private boolean handlesOperator(final Operator operator) {
    return OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY.contains(operator);
  }

  private boolean isNegationOperator(final Operator operator) {
    return Operator.NOT_EQUALS == operator || Operator.NOT_IN == operator || Operator.IS_NOT == operator;
  }

}
