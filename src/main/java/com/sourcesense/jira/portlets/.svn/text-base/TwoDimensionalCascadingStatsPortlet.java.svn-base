package com.sourcesense.jira.portlets;

import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.search.HitCollector;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestManager;
import com.atlassian.jira.issue.statistics.FilterStatisticsValuesGenerator;
import com.atlassian.jira.issue.statistics.ProjectStatisticsMapper;
import com.atlassian.jira.issue.statistics.StatisticsMapper;
import com.atlassian.jira.issue.statistics.TwoDimensionalStatsMap;
import com.atlassian.jira.portal.PortletConfiguration;
import com.atlassian.jira.portal.PortletImpl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.query.QueryImpl;
import com.sourcesense.jira.common.helpers.IndexHelper;
import com.sourcesense.jira.portlets.statistic.TwoDimensionalStatsMapForCascading;
import com.sourcesense.jira.portlets.statistic.TwoDimensionalTermHitCollectorForCascading;


/**
 * User: fabio
 * Date: Jun 25, 2007
 * Time: 11:55:08 AM
 */
@SuppressWarnings("unchecked")
public class TwoDimensionalCascadingStatsPortlet extends PortletImpl {
    private static final Logger log = Logger.getLogger(TwoDimensionalCascadingStatsPortlet.class);
    private SearchProvider searchProvider;
    private SearchRequestManager searchRequestManager;
    private CustomFieldManager customFieldManager;
    private int xCascadingLevel = -1;
    private int yCascadingLevel = -1;
    private final SearchService searchService;

    public TwoDimensionalCascadingStatsPortlet(JiraAuthenticationContext authCtx, PermissionManager permissionManager, ApplicationProperties appProps,
                                               SearchRequestManager searchRequestManager, CustomFieldManager customFieldManager, SearchProvider searchProvider,SearchService searchService) {
        super(authCtx, permissionManager, appProps);
        this.searchProvider = searchProvider;
        this.customFieldManager = customFieldManager;
        this.searchRequestManager = searchRequestManager;
        this.searchService = searchService;
    }

    
    protected TwoDimensionalStatsMapForCascading searchCountMap(SearchRequest request, StatisticsMapper xAxisStatsMapper, StatisticsMapper yAxisStatsMapper) throws SearchException {
        
            TwoDimensionalStatsMapForCascading statsMap = new TwoDimensionalStatsMapForCascading(xAxisStatsMapper, yAxisStatsMapper);
            HitCollector hitCollector = new TwoDimensionalTermHitCollectorForCascading(statsMap, IndexHelper.getIndexReader(), xCascadingLevel, yCascadingLevel);
            searchProvider.search(request.getQuery(), authenticationContext.getUser(), hitCollector);
            return statsMap;
        
      
    }

    private int safeInt(String value) {
        try {
            return Long.valueOf(value).intValue(); 
        } catch (Exception e ) {
            return -1;
        }
    }

    @Override
    protected Map<String, Object> getVelocityParams(final PortletConfiguration portletConfiguration)
    {
        Map<String, Object> params = super.getVelocityParams(portletConfiguration);
        try
        {
            //params particular to the stats filter

            this.xCascadingLevel = safeInt(portletConfiguration.getProperty("xCascadingLevel"));
            this.yCascadingLevel = safeInt(portletConfiguration.getProperty("yCascadingLevel"));

            final String filterId = portletConfiguration.getProperty("filterid");
            final String xAxisType = portletConfiguration.getProperty("xAxis");
            final String yAxisOrder = portletConfiguration.getProperty("yAxisOrder");
            final String yAxisDirection = portletConfiguration.getProperty("yAxisDirection");
            final String yAxisType = portletConfiguration.getProperty("yAxis");
            final StatisticsMapper xAxisMapper = new FilterStatisticsValuesGenerator().getStatsMapper(xAxisType);
            final StatisticsMapper yAxisMapper = new FilterStatisticsValuesGenerator().getStatsMapper(yAxisType);
            final Boolean showTotals = Boolean.valueOf(portletConfiguration.getProperty("showTotals"));
            final String numberToShowString = portletConfiguration.getProperty("numberToShow");
            final Integer numberToShow =
                    (numberToShowString == null || numberToShowString.length() == 0)
                            ? new Integer(Integer.MAX_VALUE)    // if not specified set max integer = unlimited
                            : new Integer(numberToShowString);  // otherwise set to specified value

            final SearchRequest request = searchRequestManager.getRequest(authenticationContext.getUser(), new Long(filterId));
            params.put("searchRequest", request);

            if (request == null)
            {
                params.put("user", authenticationContext.getUser());
            }
            else
            {
                TwoDimensionalStatsMapForCascading groupedCounts = searchCountMap(request, xAxisMapper, yAxisMapper);
                params.put("twoDStatsMap", groupedCounts);
                params.put("xAxisType", xAxisType);
                params.put("yAxisType", yAxisType);
                params.put("customFieldManager", customFieldManager);
                params.put("showTotals", showTotals);
                params.put("yAxisOrder", yAxisOrder);
                params.put("yAxisDirection", yAxisDirection);
                params.put("numberToShow", numberToShow);
                params.put("portlet", this);
            }
        }
        catch (Exception e)
        {
            log.error("Could not create velocity parameters " + e.getMessage(), e);
        }
        return params;
    }

    //scoprire cosa non va qui

    
    public String getSearchUrlForHeaderCell(Object xAxisObject, StatisticsMapper xAxisMapper, SearchRequest searchRequest)
    {
        SearchRequest searchUrlSuffix = xAxisMapper.getSearchUrlSuffix(xAxisObject, searchRequest);
        String result=searchUrlSuffix != null ? searchService.getQueryString(authenticationContext.getUser(), (searchUrlSuffix == null) ? new QueryImpl() : searchUrlSuffix.getQuery()) : "";;
        return result;
    }

    public String getSearchUrlForCoordCell(Object xAxisObject, Object yAxisObject, TwoDimensionalStatsMapForCascading  statsMap, SearchRequest searchRequest)
    {
        StatisticsMapper xAxisMapper = statsMap.getxAxisMapper();
        StatisticsMapper yAxisMapper = statsMap.getyAxisMapper();

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

        return srAfterSecond != null ? searchService.getQueryString(authenticationContext.getUser(), srAfterSecond.getQuery()) : "";
    }
    
    
    
    // ---------------------------------------------------------------------------------------------- Known view helpers
    /*public String getSearchUrlForHeaderCell(Object xAxisObject, StatisticsMapper<Object> xAxisMapper, SearchRequest searchRequest)
    {
        SearchRequest searchUrlSuffix = xAxisMapper.getSearchUrlSuffix(xAxisObject, searchRequest);   
       if( searchUrlSuffix != null)
        System.out.println(searchUrlSuffix.getQuery().toString());
        return searchUrlSuffix != null ? searchUrlSuffix.getQuery().toString(): "";
    }

    public String getSearchUrlForCell(Object xAxisObject, Object yAxisObject, TwoDimensionalStatsMap statsMap, SearchRequest searchRequest)
    {
        StatisticsMapper<Object> xAxisMapper = statsMap.getxAxisMapper();
        StatisticsMapper<Object> yAxisMapper = statsMap.getyAxisMapper();

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

        return srAfterSecond != null ? srAfterSecond.getQuery().getQueryString() : "";
    }*/

    // -------------------------------------------------------------------------------------------------- Private helper
    private boolean isFirst(StatisticsMapper<Object> a, StatisticsMapper<Object> b)
    {
        if (a instanceof ProjectStatisticsMapper)
        {
            return true;
        }
        else if (b instanceof ProjectStatisticsMapper)
        {
            return false;
        }/*
        else if (a instanceof IssueTypeStatisticsMapper)
        {
            return true;
        }
        else if (b instanceof IssueTypeStatisticsMapper)
        {
            return false;
        }*/
        else
        {
            return true;
        }
    }
}
