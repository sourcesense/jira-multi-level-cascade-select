package com.sourcesense.jira.portlets.statistic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.HitCollector;

import com.atlassian.jira.issue.search.parameters.lucene.sort.JiraLuceneFieldCache;

/**
 * User: fabio
 * Date: Jun 25, 2007
 * Time: 11:55:54 AM
 */
public class TwoDimensionalTermHitCollectorForCascading extends HitCollector {
    private static final Logger log = Logger.getLogger(TwoDimensionalTermHitCollectorForCascading.class);
    private final TwoDimensionalStatsMapForCascading statsMap;
    private List docToXTerms;
    private List docToYTerms;

    public TwoDimensionalTermHitCollectorForCascading(TwoDimensionalStatsMapForCascading statsMap, IndexReader indexReader, int xCascadingLevel, int yCascadingLevel)
    {
        this.statsMap = statsMap;
        try
        {
            docToXTerms = getMatches(indexReader, statsMap.getxAxisMapper().getDocumentConstant(), xCascadingLevel);
            docToYTerms = getMatches(indexReader, statsMap.getyAxisMapper().getDocumentConstant(), yCascadingLevel);
        }
        catch (IOException e)
        {
            //ignore
        }
    }

    private boolean checkMatches(Object[] matches) {
        for(int i=0; i<matches.length; i++) {
            if (matches[i]!=null) return true;
        }
        return false;
    }

    private List getMatches(IndexReader indexReader, String documentConstant, int maxLevel) throws IOException {
        int cascadingIndex = 0;
        Collection[] matches;
        List terms = new ArrayList();

        matches = JiraLuceneFieldCache.FIELD_CACHE.getMatches(indexReader, documentConstant);
        while ((maxLevel==-1 || cascadingIndex<maxLevel) && matches!=null && checkMatches(matches)) {
            log.debug("Find matches for="+(cascadingIndex>0?documentConstant:documentConstant+":"+cascadingIndex));
            terms.add(matches);

            cascadingIndex++;
            matches = JiraLuceneFieldCache.FIELD_CACHE.getMatches(indexReader, documentConstant+":"+cascadingIndex);
        }
        return terms;
    }

    private List getCollectionsForIndex(List terms, int i) {
        List termsList = new ArrayList();
        for(Iterator iterator = terms.iterator(); iterator.hasNext();) {
            Collection[] termsCollection = (Collection[]) iterator.next();
            if ((termsCollection == null) || (termsCollection.length<i) || (termsCollection[i]==null))
                return termsList;
            termsList.add(termsCollection[i]);
        }
        return termsList;
    }

    @Override
    public void collect(int i,float v1) {
        adjustMapForValues(statsMap, getCollectionsForIndex(docToXTerms, i), getCollectionsForIndex(docToYTerms, i));
    }

    private Collection getCollectionFromList(List xAxis, List yAxis, int index) {
        int xSize = 0;
        int ySize;

        if (xAxis == null && yAxis == null) return null;

        if (xAxis!=null) {
            xSize = xAxis.size();
            if (index < xSize) return (Collection) xAxis.get(index);
        }

        if (yAxis!=null) {
            ySize = yAxis.size();
            if (index < xSize+ySize) return (Collection) yAxis.get(index-xSize);
        }

        return null;
    }

    private boolean getIsXAxis(List xAxis, int index) {
        if (xAxis==null) return false;
        return (index<xAxis.size());
    }

    private String appendValue(String value, String toAppend) {
        if (value == null) return toAppend;
        return value + ':' + toAppend;
    }

    private void addValueToMap(TwoDimensionalStatsMapForCascading statsMap, String xValue, String yValue) {
        if ((xValue==null) && (yValue==null)) return;

        statsMap.addValue(xValue, yValue, 1);
        statsMap.addToYTotal(yValue, 1);
        statsMap.addToXTotal(xValue, 1);
    }

    private void addValuesToMap(TwoDimensionalStatsMapForCascading statsMap, List xAxis, List yAxis, int index, String xValue, String yValue) {
        boolean isXAxis;
        Collection firstAxis;
        String xValueDL;
        String yValueDL = null;

        firstAxis = getCollectionFromList(xAxis, yAxis, index);
        if (firstAxis == null) {
            // caso limite
            addValueToMap(statsMap, xValue, yValue);
            return;
        }
        isXAxis = getIsXAxis(xAxis, index);

        for(Iterator iterator = firstAxis.iterator(); iterator.hasNext();) {
            String iteratorValue = (String) iterator.next();
            if (isXAxis) {
                xValueDL = appendValue(xValue, iteratorValue);
            } else {
                xValueDL = xValue;
                yValueDL = appendValue(yValue, iteratorValue);
            }
            addValuesToMap(statsMap, xAxis, yAxis, index+1, xValueDL, yValueDL);
        }
    }

    private void addValuesToMap(TwoDimensionalStatsMapForCascading statsMap, List xAxis, List yAxis) {
        if ((xAxis==null || xAxis.size()==0) && (yAxis==null || yAxis.size()==0)) {
            statsMap.addValue(null, null, 1);
            statsMap.addToXTotal(null, 1);
            statsMap.addToYTotal(null, 1);
            return;
        }
        addValuesToMap(statsMap, xAxis, yAxis, 0, null, null);
    }

    private void adjustMapForValues(TwoDimensionalStatsMapForCascading statsMap, List xAxisValues, List yAxisValues)
    {
        addValuesToMap(statsMap, xAxisValues, yAxisValues);
        // Always log one hit per unique issue.
        statsMap.addToEntireTotal(1);
    }

   

}
