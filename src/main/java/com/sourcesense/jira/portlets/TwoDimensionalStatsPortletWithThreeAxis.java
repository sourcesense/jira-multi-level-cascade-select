package com.sourcesense.jira.portlets;

import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.search.HitCollector;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestManager;
import com.atlassian.jira.issue.statistics.FilterStatisticsValuesGenerator;
import com.atlassian.jira.issue.statistics.IssueTypeStatisticsMapper;
import com.atlassian.jira.issue.statistics.ProjectStatisticsMapper;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.portal.PortletImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.sourcesense.jira.common.helpers.IndexHelper;
import com.sourcesense.jira.portlets.statistic.ThreeDimensionalStatsMap;
import com.sourcesense.jira.portlets.statistic.ThreeDimensionalTermHitCollector;

/**
 * User: fabio
 * Date: Jun 27, 2007
 * Time: 3:53:07 PM
 */
public class TwoDimensionalStatsPortletWithThreeAxis extends PortletImpl {
    private static final Logger log = Logger.getLogger(TwoDimensionalStatsPortletWithThreeAxis.class);
    private final SearchRequestManager searchRequestManager;
    private final CustomFieldManager customFieldManager;
    private final SearchProvider searchProvider;

    public TwoDimensionalStatsPortletWithThreeAxis(JiraAuthenticationContext authenticationContext, PermissionManager permissionManager, ApplicationProperties applicationProperties, SearchRequestManager searchRequestManager, CustomFieldManager customFieldManager, SearchProvider searchProvider)
    {
        super(authenticationContext, permissionManager, applicationProperties);
        this.searchRequestManager = searchRequestManager;
        this.customFieldManager = customFieldManager;
        this.searchProvider = searchProvider;
    }

    @Override
    protected Map getVelocityParams(final PortletConfiguration portletConfiguration)
    {
        Map params = super.getVelocityParams(portletConfiguration);
        try
        {
            //params particular to the stats filter
            final String filterId = portletConfiguration.getProperty("filterid");
            final String xAxisType = portletConfiguration.getProperty("xAxis");
            final String yAxisOrder = portletConfiguration.getProperty("yAxisOrder");
            final String yAxisDirection = portletConfiguration.getProperty("yAxisDirection");
            final String yAxisType = portletConfiguration.getProperty("yAxis");
            final String zAxisType = portletConfiguration.getProperty("zAxis");

            final StatisticsMapper xAxisMapper = new FilterStatisticsValuesGenerator().getStatsMapper(xAxisType);
            final StatisticsMapper yAxisMapper = new FilterStatisticsValuesGenerator().getStatsMapper(yAxisType);
            final StatisticsMapper zAxisMapper = new FilterStatisticsValuesGenerator().getStatsMapper(zAxisType);
            final Boolean showTotals = Boolean.valueOf(portletConfiguration.getProperty("showTotals"));
            final String numberToShowString = portletConfiguration.getProperty("numberToShow");
            final Integer numberToShow = (numberToShowString == null || numberToShowString.equals("") ? new Integer(Integer.MAX_VALUE) : new Integer(numberToShowString));
            final Long xCascadingLevel = portletConfiguration.getLongProperty("xCascadingLevel");
            final Long yCascadingLevel = portletConfiguration.getLongProperty("yCascadingLevel");
            final Long zCascadingLevel = portletConfiguration.getLongProperty("zCascadingLevel");


            final SearchRequest request = searchRequestManager.getRequest(authenticationContext.getUser(), new Long(filterId));
            params.put("searchRequest", request);

            if (request != null)
            {
                ThreeDimensionalStatsMap groupedCounts = searchCountMap(request, xAxisMapper, yAxisMapper, zAxisMapper, xCascadingLevel, yCascadingLevel, zCascadingLevel);
                params.put("threeDStatsMap", groupedCounts);
                params.put("xAxisType", xAxisType);
                params.put("yAxisType", yAxisType);
                params.put("zAxisType", zAxisType);
                params.put("customFieldManager", customFieldManager);
                params.put("showTotals", showTotals);
                params.put("yAxisOrder", yAxisOrder);
                params.put("yAxisDirection", yAxisDirection);
                params.put("numberToShow", numberToShow);
                params.put("portlet", this);
            }
            else
                params.put("user", authenticationContext.getUser());

        }
        catch (Exception e)
        {
            log.error("Could not create velocity parameters " + e.getMessage(), e);
        }
        return params;
    }

    protected ThreeDimensionalStatsMap searchCountMap(SearchRequest request, StatisticsMapper xAxisStatsMapper, StatisticsMapper yAxisStatsMapper, StatisticsMapper zAxisStatsMapper,
                                                      Long xCascadingLevel, Long yCascadingLevel, Long zCascadingLevel) throws SearchException {
        log.debug("Start searchCountMap");

        
            ThreeDimensionalStatsMap statsMap = new ThreeDimensionalStatsMap(xAxisStatsMapper, yAxisStatsMapper, zAxisStatsMapper);
            HitCollector hitCollector = new ThreeDimensionalTermHitCollector(statsMap, IndexHelper.getIndexReader(), xCascadingLevel.intValue(), yCascadingLevel.intValue(), zCascadingLevel.intValue());

            searchProvider.search(request.getQuery(), authenticationContext.getUser(), hitCollector);

            return statsMap;
       
    }


    // ---------------------------------------------------------------------------------------------- Known view helpers
    public String getSearchUrlForHeaderCell(Object xAxisObject, StatisticsMapper xAxisMapper, SearchRequest searchRequest)
    {
        SearchRequest searchUrlSuffix = xAxisMapper.getSearchUrlSuffix(xAxisObject, searchRequest);
        return searchUrlSuffix != null ? searchUrlSuffix.getQuery().getQueryString() : "";
    }

    // ---------------------------------------------------------------------------------------------- Known view helpers
    public String getSearchUrlForTwoHeaderCell(Object xAxisObject, StatisticsMapper xAxisMapper, Object yAxisObject, StatisticsMapper yAxisMapper, SearchRequest searchRequest)
    {
        SearchRequest searchUrlSuffix = xAxisMapper.getSearchUrlSuffix(xAxisObject, searchRequest);
        searchUrlSuffix = yAxisMapper.getSearchUrlSuffix(yAxisObject, searchUrlSuffix);
        return searchUrlSuffix != null ? searchUrlSuffix.getQuery().getQueryString() : "";
    }

    public String getSearchUrlForCell(Object xAxisObject, Object yAxisObject, Object zAxisObject, ThreeDimensionalStatsMap statsMap, SearchRequest searchRequest)
    {
        StatisticsMapper xAxisMapper = statsMap.getxAxisMapper();
        StatisticsMapper yAxisMapper = statsMap.getyAxisMapper();
        StatisticsMapper zAxisMapper = statsMap.getzAxisMapper();

        SearchRequest srAfterSecond;
        if (isFirst(yAxisMapper, xAxisMapper))
        {
            SearchRequest srAfterFirst = yAxisMapper.getSearchUrlSuffix(yAxisObject, searchRequest);
            srAfterSecond = xAxisMapper.getSearchUrlSuffix(xAxisObject, srAfterFirst);
        }
        else
        {
            SearchRequest srAfterFirst = xAxisMapper.getSearchUrlSuffix(xAxisObject, searchRequest);
            srAfterSecond = yAxisMapper.getSearchUrlSuffix(yAxisObject, srAfterFirst);
        }
        srAfterSecond = zAxisMapper.getSearchUrlSuffix(zAxisObject, srAfterSecond);

        return srAfterSecond != null ? srAfterSecond.getQuery().getQueryString() : "";
    }

    // -------------------------------------------------------------------------------------------------- Private helper
    private boolean isFirst(StatisticsMapper a, StatisticsMapper b)
    {
        if (a instanceof ProjectStatisticsMapper)
        {
            return true;
        }
        else if (b instanceof ProjectStatisticsMapper)
        {
            return false;
        }
        else if (a instanceof IssueTypeStatisticsMapper)
        {
            return true;
        }
        else if (b instanceof IssueTypeStatisticsMapper)
        {
            return false;
        }
        else
        {
            return true;
        }
    }
}
