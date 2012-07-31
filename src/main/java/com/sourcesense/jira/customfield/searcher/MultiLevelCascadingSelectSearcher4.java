package com.sourcesense.jira.customfield.searcher;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import webwork.action.Action;

import com.atlassian.jira.JiraDataTypes;
import com.atlassian.jira.bc.issue.search.QueryContextConverter;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.CustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.DefaultCustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.converters.SelectConverter;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.searchers.AbstractInitializationCustomFieldSearcher;
import com.atlassian.jira.issue.customfields.searchers.CustomFieldSearcherClauseHandler;
import com.atlassian.jira.issue.customfields.searchers.SimpleCustomFieldClauseContextHandler;
import com.atlassian.jira.issue.customfields.searchers.information.CustomFieldSearcherInformation;
import com.atlassian.jira.issue.customfields.searchers.renderer.CustomFieldRenderer;
import com.atlassian.jira.issue.customfields.searchers.transformer.CustomFieldInputHelper;
import com.atlassian.jira.issue.customfields.statistics.CustomFieldStattable;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.indexers.FieldIndexer;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.information.SearcherInformation;
import com.atlassian.jira.issue.search.searchers.renderer.SearchRenderer;
import com.atlassian.jira.issue.search.searchers.transformer.SearchInputTransformer;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.issue.transport.ActionParams;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.jql.context.CascadingSelectCustomFieldClauseContextFactory;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operator.OperatorClasses;
import com.atlassian.jira.jql.query.ClauseQueryFactory;
import com.atlassian.jira.jql.query.ValidatingDecoratorQueryFactory;
import com.atlassian.jira.jql.util.JqlCascadingSelectLiteralUtil;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.jql.validator.OperatorUsageValidator;
import com.atlassian.jira.util.ComponentFactory;
import com.atlassian.jira.util.ComponentLocator;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.atlassian.jira.web.bean.FieldVisibilityBean;
import com.sourcesense.jira.customfield.searcher.indexer.MultiLevelCascadingSelectIndexer;
import com.sourcesense.jira.customfield.statistic.MultiLevelCascadingSelectStatisticsMapper;

/**
 * This class is the main class for Multi Level Cascading Select Custom Field Searcher. It has the
 * responsibility of coordinate all the classes used with the JQL search model.
 * 
 * @author Alessandro Benedetti
 * 
 */
public class MultiLevelCascadingSelectSearcher4 extends AbstractInitializationCustomFieldSearcher implements CustomFieldSearcher,CustomFieldStattable {

  private volatile CustomFieldSearcherInformation searcherInformation;

  private volatile SearchInputTransformer searchInputTransformer;

  private volatile SearchRenderer searchRenderer;

  private volatile CustomFieldSearcherClauseHandler customFieldSearcherClauseHandler;

  private final ComponentLocator componentLocator;
 
  private final ComponentFactory componentFactory;
  private OptionsManager optionsManager;

  public MultiLevelCascadingSelectSearcher4(final ComponentLocator componentLocator, final ComponentFactory componentFactory,OptionsManager manager) {
    this.componentLocator = notNull("componentLocator", componentLocator);
    this.componentFactory = notNull("componentFactory", componentFactory);
    this.optionsManager=manager;
  }

  /**
   * This is the first time the searcher knows what its ID and names are
   * 
   * @param field
   *          the Custom Field for this searcher
   */
  public void init(CustomField field) {
    final FieldVisibilityManager fieldVisibilityManager = componentLocator.getComponentInstanceOfType(FieldVisibilityBean.class);
    final SelectConverter selectConverter = componentLocator.getComponentInstanceOfType(SelectConverter.class);
    final JqlOperandResolver jqlOperandResolver = componentLocator.getComponentInstanceOfType(JqlOperandResolver.class);
    final JqlSelectOptionsUtil jqlSelectOptionsUtil = componentLocator.getComponentInstanceOfType(JqlSelectOptionsUtil.class);
    final JqlCascadingSelectLiteralUtil jqlCascadingSelectLiteralUtil = componentLocator.getComponentInstanceOfType(JqlCascadingSelectLiteralUtil.class);
    final QueryContextConverter queryContextConverter = componentLocator.getComponentInstanceOfType(QueryContextConverter.class);
    final CustomFieldInputHelper customFieldInputHelper = componentLocator.getComponentInstanceOfType(CustomFieldInputHelper.class);
    final OperatorUsageValidator usageValidator = componentLocator.getComponentInstanceOfType(OperatorUsageValidator.class);
    final ClauseNames names = field.getClauseNames();
    final FieldIndexer indexer = new MultiLevelCascadingSelectIndexer(fieldVisibilityManager, field, jqlSelectOptionsUtil, selectConverter);
    //final FieldIndexer indexer = new ValueLeadMultiLevelCascadingSelectIndexer(fieldVisibilityManager, field, jqlSelectOptionsUtil, selectConverter);
    final CustomFieldValueProvider customFieldValueProvider = new DefaultCustomFieldValueProvider();
    this.searcherInformation = new CustomFieldSearcherInformation(field.getId(), field.getNameKey(), Collections.<FieldIndexer> singletonList(indexer), new AtomicReference<CustomField>(field));
    this.searchRenderer = new CustomFieldRenderer(names, getDescriptor(), field, customFieldValueProvider, fieldVisibilityManager);
    this.searchInputTransformer = new MultiLevelCascadingSelectCustomFieldSearchInputTransformer(names, field, searcherInformation.getId(), selectConverter, jqlOperandResolver, jqlSelectOptionsUtil,
            jqlCascadingSelectLiteralUtil, queryContextConverter, customFieldInputHelper);// ?

    ClauseQueryFactory queryFactory = new MultiLevelCascadingSelectingQueryFactory(field, field.getId(), jqlSelectOptionsUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil);
    //ClauseQueryFactory queryFactory = new ValueBasedMultiLevelCascadingSelectingQueryFactory(field, field.getId(), jqlSelectOptionsUtil, jqlOperandResolver, jqlCascadingSelectLiteralUtil);
    
    queryFactory = new ValidatingDecoratorQueryFactory(usageValidator, queryFactory);

    this.customFieldSearcherClauseHandler = new SimpleCustomFieldClauseContextHandler(componentFactory.createObject(MultiLevelCascadingSelectCustomFieldValidator.class, field), queryFactory, componentFactory
            .createObject(CascadingSelectCustomFieldClauseContextFactory.class, field), OperatorClasses.EQUALITY_OPERATORS_WITH_EMPTY, JiraDataTypes.CASCADING_OPTION);
  }

  public SearcherInformation<CustomField> getSearchInformation() {
    if (searcherInformation == null) {
      throw new IllegalStateException("Attempt to retrieve SearcherInformation off uninitialised custom field searcher.");
    }
    return searcherInformation;
  }

  public SearchInputTransformer getSearchInputTransformer() {
    if (searchInputTransformer == null) {
      throw new IllegalStateException("Attempt to retrieve searchInputTransformer off uninitialised custom field searcher.");
    }
    return searchInputTransformer;
  }

  public SearchRenderer getSearchRenderer() {
    if (searchRenderer == null) {
      throw new IllegalStateException("Attempt to retrieve searchRenderer off uninitialised custom field searcher.");
    }
    return searchRenderer;
  }

  public CustomFieldSearcherClauseHandler getCustomFieldSearcherClauseHandler() {
    if (customFieldSearcherClauseHandler == null) {
      throw new IllegalStateException("Attempt to retrieve customFieldSearcherClauseHandler off uninitialised custom field searcher.");
    }
    return customFieldSearcherClauseHandler;
  }

  
  public String getEditHtml(SearchContext searchContext, FieldValuesHolder fieldValuesHolder, Map displayParameters, Action action) {
    return searchRenderer.getEditHtml(/* user? */null, searchContext, fieldValuesHolder, displayParameters, action);
  }

  public void populateFromParams(FieldValuesHolder fieldValuesHolder, ActionParams arg2) {
    this.searchInputTransformer.populateFromParams(null/* user? */, fieldValuesHolder, arg2);
  }
  

  public StatisticsMapper getStatisticsMapper(CustomField customField) {
    return new MultiLevelCascadingSelectStatisticsMapper(customField, optionsManager);
}

 

}
