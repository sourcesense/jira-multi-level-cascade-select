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
import org.apache.log4j.Logger;

import com.atlassian.jira.issue.comparator.util.DelegatingComparator;
import com.atlassian.jira.issue.statistics.StatisticsMapper;

/**
 * User: fabio
 * Date: Jun 27, 2007
 * Time: 3:53:45 PM
 */
public class ThreeDimensionalStatsMap {
    private static final Logger log = Logger.getLogger(ThreeDimensionalStatsMap.class);
    public final static String TOTAL_ORDER = "total";
    public final static String NATURAL_ORDER = "natural";
    public final static String DESC = "desc";
    public final static String ASC = "asc";

    private final StatisticsMapper<Object> xAxisMapper;
    private final StatisticsMapper<Object> yAxisMapper;
    private final StatisticsMapper<Object> zAxisMapper;

    private Map<String, Object> xValuesCache = new HashMap<String, Object>();
    private Map<String, Object> yValuesCache = new HashMap<String, Object>();
    private Map<String, Object> zValuesCache = new HashMap<String, Object>();
    private Map<Object, Object> xAxis;
    private Map<Object, Integer> xAxisTotals;
    private Map<Object, Integer> yAxisTotals;
    private Map<Object, Integer> zAxisTotals;
    private int entireTotal;

    public ThreeDimensionalStatsMap(StatisticsMapper<Object> xAxisMapper, StatisticsMapper<Object> yAxisMapper, StatisticsMapper<Object> zAxisMapper) {
        this.xAxisMapper = xAxisMapper;
        this.yAxisMapper = yAxisMapper;
        this.zAxisMapper = zAxisMapper;
        xAxis = new TreeMap<Object, Object>(xAxisMapper.getComparator());
        xAxisTotals = new TreeMap<Object, Integer>(xAxisMapper.getComparator());
        yAxisTotals = new TreeMap<Object, Integer>(yAxisMapper.getComparator());
        zAxisTotals = new TreeMap<Object, Integer>(zAxisMapper.getComparator());
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
        final Object xValue = getXValue(xKey);

        if (!xAxisMapper.isValidValue(xValue)) return;

        addToTotal(xAxisTotals, xValue, i);
    }

    /**
     * This method will increment the unique totals count for the y row identified by yKey.
     *
     * @param yKey identifies the yValue we are keying on, null is valid.
     * @param i    the amount to increment the total by, usually 1.
     */
    public void addToYTotal(String yKey, int i)
    {
        final Object yValue = getYValue(yKey);

        if (!yAxisMapper.isValidValue(yValue)) return;

        addToTotal(yAxisTotals, yValue, i);
    }

    /**
     * This method will increment the unique totals count for the y row identified by yKey.
     *
     * @param zKey identifies the yValue we are keying on, null is valid.
     * @param i    the amount to increment the total by, usually 1.
     */
    public void addToZTotal(String zKey, int i)
    {
        final Object zValue = getZValue(zKey);

        if (!zAxisMapper.isValidValue(zValue)) return;

        addToTotal(zAxisTotals, zValue, i);
    }

    private void addToTotal(Map<Object, Integer> axisTotals, Object key, int i) {
        Integer total = axisTotals.get(key);
        if (total == null) {
            total = new Integer(i);
        } else {
            total = new Integer(i + total.intValue());
        }
        axisTotals.put(key, total);
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

    public void addValue(String xKey, String yKey, String zKey, int i)
    {
        final Object xValue = getXValue(xKey);
        final Object yValue = getYValue(yKey);
        final Object zValue = getZValue(zKey);

        //only valid values should be added to the fieldMap
        if (!xAxisMapper.isValidValue(xValue) || !yAxisMapper.isValidValue(yValue) || !zAxisMapper.isValidValue(zValue)) return;

        Map<Object, Object> yValues = getTreeMapValues(xAxis, yAxisMapper, xValue);
        Map<Object, Object> zValues = getTreeMapValues(yValues, zAxisMapper, yValue);
        addValue(zValues, zValue, i);
    }

    private void addValue(Map<Object, Object> map, Object key, int value) {
        Integer existingValue = (Integer) map.get(key);
        Integer newValue;
        if (existingValue == null)
            newValue = new Integer(value);
        else
            newValue = new Integer(existingValue.intValue() + value);
        map.put(key, newValue);
    }

    private Map<Object, Object> getTreeMapValues(Map<Object, Object> map, StatisticsMapper<Object> axisMapper, Object value) {
        Map<Object, Object> treeMapValues = (Map<Object, Object>) map.get(value);
        if (treeMapValues == null)
        {
            treeMapValues = new TreeMap<Object, Object>(axisMapper.getComparator());
            map.put(value, treeMapValues);
        }
        return treeMapValues;
    }

    public Collection<Object> getXAxis()
    {
        return xAxis.keySet();
    }

    public Collection getYAxis()
    {
        return getYAxis(NATURAL_ORDER, ASC);
    }

    public Collection getYAxis(String orderBy, String direction)
    {
        Comparator<Object> comp;

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

    public Collection getYAxis(Comparator<Object> comp)
    {
        Set yAxisKeys = new TreeSet(comp);

        for (Iterator<Object> iterator = xAxis.values().iterator(); iterator.hasNext();)
        {
            Map yAxisValues = (Map) iterator.next();
            yAxisKeys.addAll(yAxisValues.keySet());
        }
        return yAxisKeys;
    }

    public Collection getZAxis()
    {
        Comparator<Object> comp = getZAxisComparator(NATURAL_ORDER, ASC);
        return getZAxis(comp);
    }

    public Collection getZAxis(String orderBy, String direction)
    {
        Comparator<Object> comp = getZAxisComparator(orderBy, direction);
        return getZAxis(comp);
    }

    public Collection getZAxis(Object yValue, String orderBy, String direction)
    {
        Comparator<Object> comp = getZAxisComparator(orderBy, direction);
        return getZAxis(yValue, comp);
    }

    public Comparator<Object> getZAxisComparator(String orderBy, String direction)
    {
        Comparator<Object> comp;

        if (orderBy != null && orderBy.equals(TOTAL_ORDER))
        {
            // Compare by total
            comp = new Comparator(){
                    public int compare(Object o1, Object o2) {
                        Long o1Long = new Long(getZAxisUniqueTotal(o1));
                        Long o2Long = new Long(getZAxisUniqueTotal(o2));
                        return o1Long.compareTo(o2Long);
                    }
                };

            // Only reverse total Comaparator, not field Comparator
            if (direction != null && direction.equals(DESC))
            {
                comp = new ReverseComparator(comp);
            }

            // If totals are equal, delagate back to field comparator
            comp = new DelegatingComparator(comp, zAxisMapper.getComparator());
        }
        else
        {
            comp = zAxisMapper.getComparator();
            if (direction != null && direction.equals(DESC))
            {
                comp = new ReverseComparator(comp);
            }
        }

        return comp;
    }

    public Collection getZAxis(Object yValue, Comparator<Object> comp)
    {
        Set zAxisKeys = new TreeSet(comp);

        log.debug("getZAxis. Comp = "+comp);
        for (Iterator<Object> xIterator = xAxis.values().iterator(); xIterator.hasNext();)
        {
            Map yAxisValues = (Map) xIterator.next();
            log.debug("getZAxis. yAxisValues = "+yAxisValues);
            Map zAxisValues = (Map) yAxisValues.get(yValue);
            log.debug("getZAxis. zAxisValues = "+zAxisValues);
            if (zAxisValues!=null) {
                zAxisKeys.addAll(zAxisValues.keySet());
            }
/*
            for (Iterator yIterator = yAxisValues.values().iterator(); yIterator.hasNext();)
            {
                Map zAxisValues = (Map) yIterator.next();
                log.debug("getZAxis. zAxisValues = "+zAxisValues);
                zAxisKeys.addAll(zAxisValues.keySet());
            }
*/
        }
        return zAxisKeys;
    }

    public Collection getZAxis(Comparator<Object> comp)
    {
        Set zAxisKeys = new TreeSet(comp);

        log.debug("getZAxis. Comp = "+comp);
        for (Iterator<Object> xIterator = xAxis.values().iterator(); xIterator.hasNext();)
        {
            Map yAxisValues = (Map) xIterator.next();
            log.debug("getZAxis. yAxisValues = "+yAxisValues);
            for (Iterator yIterator = yAxisValues.values().iterator(); yIterator.hasNext();)
            {
                Map zAxisValues = (Map) yIterator.next();
                log.debug("getZAxis. zAxisValues = "+zAxisValues);
                zAxisKeys.addAll(zAxisValues.keySet());
            }
        }
        return zAxisKeys;
    }

    public int getCoordinate(Object xAxis, Object yAxis, Object zAxis)
    {
        Map yValues = (Map) this.xAxis.get(xAxis);
        if (yValues == null) return 0;

        Map zValues = (Map) yValues.get(yAxis);
        if (zValues == null) return 0;

        Integer value = (Integer) zValues.get(zAxis);
        return value == null ? 0 : value.intValue();
    }

    private Object getZValue(String zKey)
    {
        return getValue(zValuesCache, zAxisMapper, zKey);
    }

    private Object getYValue(String yKey)
    {
        return getValue(yValuesCache, yAxisMapper, yKey);
    }

    private Object getXValue(String xKey)
    {
        return getValue(xValuesCache, xAxisMapper, xKey);
    }

    private Object getValue(Map<String, Object> valuesCache, StatisticsMapper<Object> axisMapper, String xKey) {
        if (xKey==null) return null;
        Object xValue = valuesCache.get(xKey);
        if (xValue == null)
        {
            xValue = axisMapper.getValueFromLuceneField(xKey);
            valuesCache.put(xKey, xValue);
        }
        return xValue;
    }

    public StatisticsMapper<Object> getzAxisMapper()
    {
        return zAxisMapper;
    }

    public StatisticsMapper<Object> getyAxisMapper()
    {
        return yAxisMapper;
    }

    public StatisticsMapper<Object> getxAxisMapper()
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
        return getAxisUniqueTotal(xAxisTotals, xAxis);
    }

    /**
     * Returns the value of unique issues contained in the column identified by xAxis.
     *
     * @param yAxis identifies the row who's total is requested, null is valid.
     * @return number of unique issues for the identified row.
     */
    public long getYAxisUniqueTotal(Object yAxis)
    {
        return getAxisUniqueTotal(yAxisTotals, yAxis);
    }

    /**
     * Returns the value of unique issues contained in the column identified by xAxis.
     *
     * @param zAxis identifies the row who's total is requested, null is valid.
     * @return number of unique issues for the identified row.
     */
    public long getZAxisUniqueTotal(Object zAxis)
    {
        return getAxisUniqueTotal(zAxisTotals, zAxis);
    }

    /**
     * Returns the value of unique issues contained in the column identified by xAxis.
     *
     * @param axis identifies the row who's total is requested, null is valid.
     * @return number of unique issues for the identified row.
     */
    public long getAxisUniqueTotal(Map<Object, Integer> axisTotals, Object axis)
    {
        long total = 0;
        Integer mapTotal = axisTotals.get(axis);
        if (mapTotal != null) {
            total += mapTotal.intValue();
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
        for (Iterator<Object> iterator = xAxis.values().iterator(); iterator.hasNext();)
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
        for (Iterator<Object> iterator = xAxis.keySet().iterator(); iterator.hasNext();)
        {
            Object xKey = iterator.next();
            total += getXAxisTotal(xKey);
        }
        return total;
    }

}
