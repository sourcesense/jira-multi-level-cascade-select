package com.sourcesense.jira.common;

import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.customfields.option.Option;

import java.util.*;

/**
 * This class, implementing Java Map, associates a level (integer) to a Map
 * ParentId -> Options list
 * Generics are not used for jira compatibility issues.
 * User: fabio
 * Date: Jun 21, 2007
 * Time: 1:36:10 PM
 */
public class OptionsMap implements Map {
 
    private TreeMap map = new TreeMap(new OptionsMapKeyComparator());

    /*
     * this OptionMap is created to obtain the map of all the levels, does not contain the selecion of the user*/
    public OptionsMap(Options options) {
        super();
        putOptions(options);
    }

    public void putOptions(Options options) {
        transformOptionsToMap(options, 0, null);
    }

    
    /**
     * Converts from int to String, returning null for zero.
     * @param level
     * @return
     */
    private String getKeyFromLevel(int level) {
        return level == 0 ? null : String.valueOf(level);
    }

    //se nella map in memoria presente la stringa "level", ritorna una mappa che associa un parent id ad una lista di options
    //
    /**
     * If the Map contains the level in input (string built from the Integer) 
     * returns the map parentId -> option list
     * @param key
     * @return
     */
    private Map safeGetMap(String key) {
        if (!map.containsKey(key)) {
            map.put(key, new TreeMap(new OptionsMapKeyComparator()));
        }
        return (Map) map.get(key);
    }

    
    /**
     * checks that the list of Options in input is not empty and then it populate the result map,
     * descending through the levels
     * @param options
     * @param level
     * @param parentId
     * @return
     */
    private Map transformOptionsToMap(List options, int level, String parentId) {
        if (options != null && !options.isEmpty()) {
            Map mapLevel = safeGetMap(getKeyFromLevel(level));
            mapLevel.put(parentId, options);
            for (Iterator iterator = options.iterator(); iterator.hasNext();) {
                Option option = (Option) iterator.next();
                transformOptionsToMap(option.getChildOptions(), level + 1, getOptionKey(option));
            }
        }
        return map;
    }

    
    /**
     * returns the String ID of an Option
     * @param option
     * @return
     */
    private String getOptionKey(Option option) {
        if (option != null) {
            return option.getOptionId().toString();
        }
        return null;
    }
    
    
    /**
     * returns the ID of the parent of the Option
     * @param option
     * @return
     */
    private String getParentOptionKey(Option option) {
        if (option != null) {
            return getOptionKey(option.getParentOption());
        }
        return null;
    }

   
    /**
     * puts an option in an input level, if the level is empty, a new option list is created and populated
     * with the input option
     * @param level
     * @param option
     */
    public void putOptionInLevel(String level, Option option) {
        Map mapLevel = safeGetMap(level);
        String parentKey = getParentOptionKey(option);
        List options = (List) mapLevel.get(parentKey);
        if (options == null) {
            options = new ArrayList();
            mapLevel.put(parentKey, options);
        }
        if (!options.contains(option)) {
            options.add(option);
        }
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean containsKey(Object key) {
        return map.containsKey(key);
    }

    //in input abbiamo un livello e un valore
    //ottengo la mappa a tale livello
    //questo metodo controlla che a un dato livello, sia presente , come figlia di un qualsiasi parentId,
    // una specifica option
    
    /**
     * checks that the input value belong to the input level.
     * In the level it checks that the input value belong at least to one child list
     * @param level
     * @param value
     * @return
     */
    public boolean containsValueInLevel(Object level, String value) {
        Map levelMap = (Map) map.get(level);
        if (levelMap != null) {
          //mi prendo tutti i parentId e li scansiono uno per uno
            for (Iterator parentKeyIterator = levelMap.keySet().iterator(); parentKeyIterator.hasNext();) {
                Object parentKey = parentKeyIterator.next();
                List options = (List) levelMap.get(parentKey);
                //mi ottengo la lista di opzioni figlie del parentId
                if (options != null) {
                  //per ognuna verifico se soddisfa l'inpu
                    for (Iterator optionsIterator = options.iterator(); optionsIterator.hasNext();) {
                        Option option = (Option) optionsIterator.next();
                        if (option.getValue().equalsIgnoreCase(value)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    public Object get(Object key) {
        return map.get(key);
    }

    public Object put(Object key, Object value) {
        return map.put(key, value);
    }

    public Object remove(Object key) {
        return map.remove(key);
    }

    public void putAll(Map t) {
        map.putAll(t);
    }

    public void clear() {
        map.clear();
    }

    public Set keySet() {
        return map.keySet();
    }

    public Collection values() {
        return map.values();
    }

    public Set entrySet() {
        return map.entrySet();
    }


    public String toString() {
        return map.toString();
    }

    public Map getLevel(String level) {
        return safeGetMap(level);
    }

    /**
     * return all the options inside a level
     * @param level
     * @return
     */
    public List getOptionsInLevel(String level) {
        Map mapLevel = getLevel(level);
        List options = new ArrayList();
        for(Iterator mapIterator=mapLevel.keySet().iterator();mapIterator.hasNext();) {
            Object mapKey = mapIterator.next();
            options.addAll((Collection) mapLevel.get(mapKey));
        }
        return options;
    }

   
    
    public static String getFirstLevelKey() {
        return null;
    }

    /**
     * return the next level from the input one.
     * Remember that the level is a String.
     * @param level
     * @return
     */
    public static String getNextLevelKey(String level) {
        if (level==null) {
            return "1";
        }

        return String.valueOf(Integer.parseInt(level)+1);
    }
}
