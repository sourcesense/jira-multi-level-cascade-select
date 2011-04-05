package com.sourcesense.jira.customfield.statistic;

import java.util.Comparator;
import java.util.List;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.customfields.CustomFieldSearcher;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.statistics.AbstractCustomFieldStatisticsMapper;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestUtils;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.jql.util.JqlCustomFieldId;
import com.opensymphony.user.User;
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

  public MultiLevelCascadingSelectStatisticsMapper(CustomField customField, OptionsManager optionsManager) {
    super(customField);
    this.optionsManager = optionsManager;
  }

  @Override
  protected String getSearchValue(Object object) {
    return ((MultiLevelCascadingSelectValue) object).getSearchValue();
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
        System.out.println("VALUE:"+((MultiLevelCascadingSelectValue) value).getSearchValue());
        final SearchService searchService = ComponentManager.getComponentInstanceOfType(SearchService.class);
        final User user = ComponentManager.getInstance().getJiraAuthenticationContext().getUser();
        SearchContext searchRequestContext = searchService.getSearchContext(user, searchRequest.getQuery());
        SearchContext possibleContext = getSearchContextFromValue(value);
        System.out.println("possible : "+possibleContext.toString());
        SearchContext combinedSearchContext = SearchRequestUtils.getCombinedSearchContext(searchRequestContext, possibleContext);

        CustomFieldSearcher searcher = customField.getCustomFieldSearcher();
        if (searcher.getSearchRenderer().isShown(user, combinedSearchContext)) {
          JqlClauseBuilder whereClauseBuilder = JqlQueryBuilder.newClauseBuilder(searchRequest.getQuery()).defaultAnd();

          // Include the project/issue types from the context
          final List<Long> projectIds = combinedSearchContext.getProjectIds();
          if (projectIds != null && !projectIds.isEmpty()) {
            System.out.println("projectIDs");
            for (Long l : projectIds)
              System.out.println(l);
            whereClauseBuilder.project().inNumbers(projectIds);
          }
          final List<String> issueTypeIds = combinedSearchContext.getIssueTypeIds();
          if (issueTypeIds != null && !issueTypeIds.isEmpty()) {
            System.out.println("IssueTypeId");
            for (String s : issueTypeIds) {
              System.out.println(s);
            }
            whereClauseBuilder.issueType().inStrings(issueTypeIds);
          }

          // Now lets and all of the query with the current custom field value we are looking for
          // teoricamente basta splittare value con i : e ciclare qui, vediamo pero prim se gunziona
          if (getSearchValue(value) != null) {
            whereClauseBuilder.addStringCondition(JqlCustomFieldId.toString(customField.getIdAsLong()), getSearchValue(value));
            System.out.println(whereClauseBuilder.buildQuery().toString());
          }

          return new SearchRequest(whereClauseBuilder.buildQuery());
        } else {
          // Custom field cannot be shown on the issue navigator at this stage.
          return null;
        }
      } else {
        // This hopes that the value will never be null, the previous impl never catered for a null
        // value...
        return null;
      }
    }
  }
}
