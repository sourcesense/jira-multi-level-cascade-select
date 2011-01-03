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
 * Date: Jun 27, 2007
 * Time: 3:55:09 PM
 */
public class ThreeDimensionalTermHitCollector extends HitCollector {
    private static final Logger log = Logger.getLogger(ThreeDimensionalTermHitCollector.class);
    private List<Collection[]> docToXTerms;
    private List<Collection[]> docToYTerms;
    private List<Collection[]> docToZTerms;
    private ThreeDimensionalStatsMap statsMap;

    public ThreeDimensionalTermHitCollector(ThreeDimensionalStatsMap statsMap, IndexReader indexReader, int xCascadingLevel, int yCascadingLevel, int zCascadingLevel)
    {
        log.debug("Creating object");
        this.statsMap = statsMap;

        try
        {
            docToXTerms = getMatches(indexReader, statsMap.getxAxisMapper().getDocumentConstant(), xCascadingLevel);
            docToYTerms = getMatches(indexReader, statsMap.getyAxisMapper().getDocumentConstant(), yCascadingLevel);
            docToZTerms = getMatches(indexReader, statsMap.getzAxisMapper().getDocumentConstant(), zCascadingLevel);
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

    private List<Collection[]> getMatches(IndexReader indexReader, String documentConstant, int maxLevel) throws IOException {
        int cascadingIndex = 0;
        Collection[] matches;
        List<Collection[]> terms = new ArrayList<Collection[]>();

        matches = JiraLuceneFieldCache.FIELD_CACHE.getMatches(indexReader, documentConstant);
        while (cascadingIndex<maxLevel && matches!=null && checkMatches(matches)) {
            log.debug("Find matches for="+(cascadingIndex==0?documentConstant:documentConstant+":"+cascadingIndex));
            terms.add(matches);

            cascadingIndex++;
            matches = JiraLuceneFieldCache.FIELD_CACHE.getMatches(indexReader, documentConstant+":"+cascadingIndex);
        }
        return terms;
    }

    private List<Collection> getCollectionsForIndex(List<Collection[]> terms, int i) {
        List<Collection> termsList = new ArrayList<Collection>();
        for(Iterator<Collection[]> iterator = terms.iterator(); iterator.hasNext();) {
            Collection[] termsCollection = iterator.next();
            if ((termsCollection == null) || (termsCollection.length<i) || (termsCollection[i]==null))
                return termsList;
            termsList.add(termsCollection[i]);
        }
        return termsList;
    }

    @Override
    public void collect(int i,float v) {
        adjustMapForValues(getCollectionsForIndex(docToXTerms, i), getCollectionsForIndex(docToYTerms, i), getCollectionsForIndex(docToZTerms, i));
    }

    private Collection getCollectionFromList(List<Collection> xAxis, List<Collection> yAxis, List<Collection> zAxis, int index) {
        int xSize = 0;
        int ySize = 0;
        int zSize;

        if (xAxis == null && yAxis == null && zAxis == null) return null;

        if (xAxis!=null) {
            xSize = xAxis.size();
            if (index < xSize) return xAxis.get(index);
        }

        if (yAxis!=null) {
            ySize = yAxis.size();
            if (index < xSize+ySize) return yAxis.get(index-xSize);
        }

        if (zAxis!=null) {
            zSize = zAxis.size();
            if (index < xSize+ySize+zSize) return zAxis.get(index-xSize-ySize);
        }

        return null;
    }

    private boolean getIsXAxis(List<Collection> xAxis, int index) {
        if (xAxis==null) return false;
        return (index<xAxis.size());
    }

    private boolean getIsYAxis(List<Collection> xAxis, List<Collection> yAxis, int index) {
        int xSize = 0;
        int ySize;

        if (yAxis==null) return false;
        if (xAxis!=null) xSize = xAxis.size();

        ySize = xSize+yAxis.size();
        return ((index >= xSize) && (index < ySize));
    }

    private String appendValue(String value, String toAppend) {
        if (value == null) return toAppend;
        return value + ':' + toAppend;
    }

    private void addValueToMap(String xValue, String yValue, String zValue) {
        log.debug("addValueToMap. xValue:"+xValue+" - yValue:"+yValue+" - zValue:"+zValue);
        if ((xValue==null) && (yValue==null) && (zValue==null)) return;

        statsMap.addValue(xValue, yValue, zValue, 1);
        statsMap.addToXTotal(xValue, 1);
        statsMap.addToYTotal(yValue, 1);
        statsMap.addToZTotal(zValue, 1);
    }

    private void addValuesToMap(List<Collection> xAxis, List<Collection> yAxis, List<Collection> zAxis, int index, String xValue, String yValue, String zValue) {
        boolean isXAxis;
        boolean isYAxis;
        Collection firstAxis;
        String xValueDL;
        String yValueDL = null;
        String zValueDL = null;

        firstAxis = getCollectionFromList(xAxis, yAxis, zAxis, index);
        if (firstAxis == null) {
            // caso limite
            addValueToMap(xValue, yValue, zValue);
            return;
        }
        isXAxis = getIsXAxis(xAxis, index);
        isYAxis = getIsYAxis(xAxis, yAxis, index);

        log.debug("AddValuesToMap. ");
        log.debug("               IsXAxis="+isXAxis);
        log.debug("               IsYAxis="+isYAxis);
        log.debug("                xValue:"+xValue);
        log.debug("                yValue:"+yValue);
        log.debug("                zValue:"+zValue);
        for(Iterator iterator = firstAxis.iterator(); iterator.hasNext();) {
            String iteratorValue = (String) iterator.next();
            log.debug("Iterator:"+iteratorValue);
            if (isXAxis) {
                xValueDL = appendValue(xValue, iteratorValue);
            } else if (isYAxis) {
                xValueDL = xValue;
                yValueDL = appendValue(yValue, iteratorValue);
            } else {
                xValueDL = xValue;
                yValueDL = yValue;
                zValueDL = appendValue(zValue, iteratorValue);
            }
            addValuesToMap(xAxis, yAxis, zAxis, index+1, xValueDL, yValueDL, zValueDL);
        }
    }

    private void addValuesToMap(List<Collection> xAxis, List<Collection> yAxis, List<Collection> zAxis) {
        if ((xAxis==null || xAxis.size()==0) && (yAxis==null || yAxis.size()==0)) {
            statsMap.addValue(null, null, null, 1);
            statsMap.addToXTotal(null, 1);
            statsMap.addToYTotal(null, 1);
            statsMap.addToZTotal(null, 1);
            return;
        }
        addValuesToMap(xAxis, yAxis, zAxis, 0, null, null, null);
    }

    private void adjustMapForValues(List<Collection> xAxisValues, List<Collection> yAxisValues, List<Collection> zAxisValues)
    {
        log.debug("adjustMapForValues");

        addValuesToMap(xAxisValues, yAxisValues, zAxisValues);
        // Always log one hit per unique issue.
        statsMap.addToEntireTotal(1);
    }

    

}
