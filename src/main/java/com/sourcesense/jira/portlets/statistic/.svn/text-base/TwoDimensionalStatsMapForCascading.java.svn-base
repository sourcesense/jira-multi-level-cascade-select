package com.sourcesense.jira.portlets.statistic;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.collections.comparators.ReverseComparator;

import com.atlassian.jira.issue.comparator.util.DelegatingComparator;
import com.atlassian.jira.issue.statistics.StatisticsMapper;

/**
 * User: fabio
 * Date: Oct 15, 2007
 * Time: 12:02:13 PM
 */
public class TwoDimensionalStatsMapForCascading {
    public final static String TOTAL_ORDER = "total";
    public final static String NATURAL_ORDER = "natural";
    public final static String DESC = "desc";
    public final static String ASC = "asc";

    private final StatisticsMapper xAxisMapper;
    private final StatisticsMapper yAxisMapper;

    private Map xValuesCache = new HashMap();
    private Map yValuesCache = new HashMap();
    private Map xAxis;
    private Map xAxisTotals;
    private Map yAxisTotals;
    private int entireTotal;

    public TwoDimensionalStatsMapForCascading(StatisticsMapper xAxisMapper, StatisticsMapper yAxisMapper)
    {
        this.xAxisMapper = xAxisMapper;
        this.yAxisMapper = yAxisMapper;
        xAxis = new TreeMap(xAxisMapper.getComparator());
        xAxisTotals = new TreeMap(xAxisMapper.getComparator());
        yAxisTotals = new TreeMap(yAxisMapper.getComparator());
    }

    /**
     * This method will increment the unique totals count for the provided
     * xKey.
     *
     * @param xKey identifies the xValue we are keying on, null is valid.
     * @param i    the amount to increment the total by, usually 1.
     */
    public void addToXTotal(String xKey, int i)
    {
        final Object xValue;
        if (xKey != null)
            xValue = getXValue(xKey);
        else
            xValue = null;

        if (!xAxisMapper.isValidValue(xValue))
            return;

        Integer total = (Integer) xAxisTotals.get(xValue);
        if (total == null)
        {
            total = new Integer(i);
        }
        else
        {
            total = new Integer(i + total.intValue());
        }
        xAxisTotals.put(xValue, total);
    }

    /**
     * This method will increment the unique totals count for the y row identified by yKey.
     *
     * @param yKey identifies the yValue we are keying on, null is valid.
     * @param i    the amount to increment the total by, usually 1.
     */
    public void addToYTotal(String yKey, int i)
    {
        final Object yValue;
        if (yKey != null)
            yValue = getYValue(yKey);
        else
            yValue = null;

        if (!yAxisMapper.isValidValue(yValue))
            return;

        Integer total = (Integer) yAxisTotals.get(yValue);
        if (total == null)
        {
            total = new Integer(i);
        }
        else
        {
            total = new Integer(i + total.intValue());
        }
        yAxisTotals.put(yValue, total);
    }

    /**
     * Increments the total count of unique issues added to this StatsMap.
     *
     * @param i the amount to increment the total by, usually 1.
     */
    public void addToEntireTotal(int i)
    {
        entireTotal += i;
    }

    public void addValue(String xKey, String yKey, int i)
    {
        final Object xValue;
        if (xKey != null)
            xValue = getXValue(xKey);
        else
            xValue = null;

        final Object yValue;
        if (yKey != null)
            yValue = getYValue(yKey);
        else
            yValue = null;

        //only valid values should be added to the map
        if (!xAxisMapper.isValidValue(xValue) || !yAxisMapper.isValidValue(yValue))
            return;

        Map yValues = (Map) xAxis.get(xValue);
        if (yValues == null)
        {
            yValues = new TreeMap(yAxisMapper.getComparator());
            xAxis.put(xValue, yValues);
        }

        Integer existingValue = (Integer) yValues.get(yValue);
        Integer newValue;
        if (existingValue == null)
            newValue = new Integer(i);
        else
            newValue = new Integer(existingValue.intValue() + i);

        yValues.put(yValue, newValue);
    }

    public Collection getXAxis()
    {
        return xAxis.keySet();
    }

    public Collection getYAxis()
    {
        return getYAxis(NATURAL_ORDER, ASC);
    }
    public Collection getYAxis(String orderBy, String direction)
    {
        Comparator comp;

        if (orderBy != null && orderBy.equals(TOTAL_ORDER))
        {
            // Compare by total
            comp = new Comparator(){

                public int compare(Object o1, Object o2) {

                    Long o1Long = new Long(getYAxisUniqueTotal(o1));
                    Long o2Long = new Long(getYAxisUniqueTotal(o2));
                    return o1Long.compareTo(o2Long);

                }
            };

            // Only reverse total Comaparator, not field Comparator
            if (direction != null && direction.equals(DESC))
            {
                comp = new ReverseComparator(comp);
            }

            // If totals are equal, delagate back to field comparator
            comp = new DelegatingComparator(comp, yAxisMapper.getComparator());
        }
        else
        {
            comp = yAxisMapper.getComparator();
            if (direction != null && direction.equals(DESC))
            {
                comp = new ReverseComparator(comp);
            }
        }

        return getYAxis(comp);
    }

    public Collection getYAxis(Comparator comp)
    {
        Set yAxisKeys = new TreeSet(comp);

        for (Iterator iterator = xAxis.values().iterator(); iterator.hasNext();)
        {
            Map yAxisValues = (Map) iterator.next();
            yAxisKeys.addAll(yAxisValues.keySet());
        }
        return yAxisKeys;
    }

    public int getCoordinate(Object xAxis, Object yAxis)
    {
        Map yValues = (Map) this.xAxis.get(xAxis);
        if (yValues == null)
            return 0;

        Integer value = (Integer) yValues.get(yAxis);
        return value == null ? 0 : value.intValue();
    }

    private Object getYValue(String yKey)
    {
        Object yValue = yValuesCache.get(yKey);
        if (yValue == null)
        {
            yValue = yAxisMapper.getValueFromLuceneField(yKey);
            yValuesCache.put(yKey, yValue);
        }
        return yValue;
    }

    private Object getXValue(String xKey)
    {
        Object xValue = xValuesCache.get(xKey);
        if (xValue == null)
        {
            xValue = xAxisMapper.getValueFromLuceneField(xKey);
            xValuesCache.put(xKey, xValue);
        }
        return xValue;
    }

    public StatisticsMapper getyAxisMapper()
    {
        return yAxisMapper;
    }

    public StatisticsMapper getxAxisMapper()
    {
        return xAxisMapper;
    }

    /**
     * Returns the value of unique issues contained in the column identified by xAxis.
     *
     * @param xAxis identifies the column who's total is requested, null is valid.
     * @return number of unique issues for the identified column.
     */
    public long getXAxisUniqueTotal(Object xAxis)
    {
        long total = 0;
        Integer xTotal = (Integer) xAxisTotals.get(xAxis);
        if (xTotal != null)
        {
            total += xTotal.intValue();
        }
        return total;
    }

    /**
     * Returns the value of unique issues contained in the column identified by xAxis.
     *
     * @param yAxis identifies the row who's total is requested, null is valid.
     * @return number of unique issues for the identified row.
     */
    public long getYAxisUniqueTotal(Object yAxis)
    {
        long total = 0;
        Integer yTotal = (Integer) yAxisTotals.get(yAxis);
        if (yTotal != null)
        {
            total += yTotal.intValue();
        }
        return total;
    }

    /**
     * Returns the value of all unique issues identified within this StatsMap.
     *
     * @return number of unique issues identified within this StatsMap.
     */
    public long getUniqueTotal()
    {
        return entireTotal;
    }

    /**
     * Returns an additive total of the identified column. This method is currently
     * not used, think of using @see getXAxisUniqueTotal instead.
     *
     * @param xAxis identifies the column who's total is requested, null is valid.
     * @return the additive total of the identified column.
     */
    public long getXAxisTotal(Object xAxis)
    {
        long total = 0;
        Map yValues = (Map) this.xAxis.get(xAxis);
        if (yValues == null)
            return 0;

        for (Iterator iterator = yValues.values().iterator(); iterator.hasNext();)
        {
            Integer value = (Integer) iterator.next();
            if (value == null)
                continue;

            total += value.intValue();
        }
        return total;
    }

    /**
     * Returns an additive total of the identified column. This method is currently
     * not used, think of using @see getYAxisUniqueTotal instead.
     *
     * @param yAxis identifies the row who's total is requested, null is valid.
     * @return the additive total of the identified row.
     */
    public long getYAxisTotal(Object yAxis)
    {
        long total = 0;
        for (Iterator iterator = xAxis.values().iterator(); iterator.hasNext();)
        {
            Map yValues = (Map) iterator.next();
            Integer value = (Integer) yValues.get(yAxis);
            if (value == null)
                continue;
            total += value.intValue();
        }
        return total;
    }

    /**
     * Returns the additive total of all issue totals in each cell in this StatsMap.
     * This method is currently not used, think of using @see getUniqueTotal instead.
     *
     * @return additive total of all issue totals in each cell in this StatsMap.
     */
    public long getTotal()
    {
        long total = 0;
        for (Iterator iterator = xAxis.keySet().iterator(); iterator.hasNext();)
        {
            Object xKey = iterator.next();
            total += getXAxisTotal(xKey);
        }
        return total;
    }
}
