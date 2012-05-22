package com.sourcesense.jira.customfield.statistic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.statistics.AbstractCustomFieldStatisticsMapper;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestUtils;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.jql.util.JqlCustomFieldId;
import com.atlassian.query.operator.Operator;
import com.atlassian.crowd.embedded.api.User;
import com.sourcesense.jira.customfield.MultiLevelCascadingSelectComparator;
import com.sourcesense.jira.customfield.MultiLevelCascadingSelectValue;

/**
 * This class is the same of the Plug-in 3.0. At the moment seems to not need any change or
 * adaptation.
 * 
 * @author Fabio Date: 5/11/2010 (date of the conversion by Alessandro Benedetti)
 */
public class MultiLevelCascadingSelectStatisticsMapper extends AbstractCustomFieldStatisticsMapper {
  private OptionsManager optionsManager;

  public static String EMPTY_VALUE = "_none_";

  public static String EMPTY_VALUE_ID = "-2";

  public static long EMPTY_VALUE_ID_LONG = -2;

  private static final Logger log = Logger.getLogger(MultiLevelCascadingSelectStatisticsMapper.class);

  public MultiLevelCascadingSelectStatisticsMapper(CustomField customField, OptionsManager optionsManager) {
    super(customField);
    this.optionsManager = optionsManager;
  }

  @Override
  protected String getSearchValue(Object object) {
    return ((MultiLevelCascadingSelectValue) object).getSearchValue();
  }

  @Override
  public String getDocumentConstant() {
    return customField.getId() + ":0";
  }

  public Object getValueFromLuceneField(String string) {
    return new MultiLevelCascadingSelectValue(optionsManager, string);
  }

  @Override
  public Comparator getComparator() {
    return new MultiLevelCascadingSelectComparator();
  }

  @Override
  public SearchRequest getSearchUrlSuffix(Object value, SearchRequest searchRequest) {
    if (searchRequest == null) {
      return null;
    } else {
      if (value != null) {
        log.debug("VALUE:" + ((MultiLevelCascadingSelectValue) value).getSearchValue());
        final SearchService searchService = ComponentManager.getComponentInstanceOfType(SearchService.class);
        final User user = ComponentManager.getInstance().getJiraAuthenticationContext().getUser();
        SearchContext searchRequestContext = searchService.getSearchContext(user, searchRequest.getQuery());
        SearchContext possibleContext = getSearchContextFromValue(value);
        log.debug("possible : " + possibleContext.toString());
        SearchContext combinedSearchContext = SearchRequestUtils.getCombinedSearchContext(searchRequestContext, possibleContext);

        CustomFieldSearcher searcher = customField.getCustomFieldSearcher();
        if (searcher.getSearchRenderer().isShown(user, combinedSearchContext)) {
          JqlClauseBuilder whereClauseBuilder = JqlQueryBuilder.newClauseBuilder(searchRequest.getQuery()).defaultAnd();

          // Include the project/issue types from the context
          final List<Long> projectIds = combinedSearchContext.getProjectIds();
          if (projectIds != null && !projectIds.isEmpty()) {
            log.debug("projectIDs");
            for (Long l : projectIds) {
              log.debug(l);
              whereClauseBuilder.project().inNumbers(projectIds);
            }
          }
          final List<String> issueTypeIds = combinedSearchContext.getIssueTypeIds();
          if (issueTypeIds != null && !issueTypeIds.isEmpty()) {
            log.debug("IssueTypeId");
            for (String s : issueTypeIds) {
              log.debug(s);
            }
            whereClauseBuilder.issueType().inStrings(issueTypeIds);
          }/*
            * the problem is the search that search at least which option we need. it's not
            * exclusive, but we need the precise cooccorences
            */
          if (getSearchValue(value) != null) {
            Set<String> idArray = this.generateOptionIdListFromValue(value);
            ArrayList<String> idArrayList = new ArrayList<String>(idArray);
            idArrayList.add(EMPTY_VALUE_ID);// this ends the sequence of id, making exact search
                                            // possible
            whereClauseBuilder.addFunctionCondition(JqlCustomFieldId.toString(customField.getIdAsLong()), Operator.IN, "multilevelcascadeOption", idArrayList);
          }

          return new SearchRequest(whereClauseBuilder.buildQuery());
        } else {
          // Custom field cannot be shown on the issue navigator at
          // this stage.
          return null;
        }
      } else {
        // This hopes that the value will never be null, the previous
        // impl never catered for a null
        // value...
        return null;
      }
    }
  }

  private Set<String> generateOptionIdListFromValue(Object value) {
    Set<String> uniqueIds = new TreeSet<String>();
    String valueString = getSearchValue(value);
    String[] arrayValues = valueString.split("-");
    for (String singleValue : arrayValues) {
      List<Option> optionListByValue = optionsManager.findByOptionValue(singleValue.trim());
      for (Option o : optionListByValue) {
        long optionCfId = o.getRelatedCustomField().getCustomField().getIdAsLong();
        long cfId = customField.getIdAsLong();
        if (optionCfId == cfId)
          uniqueIds.add(o.getOptionId().toString());
      }
    }
    return uniqueIds;
  }
}
