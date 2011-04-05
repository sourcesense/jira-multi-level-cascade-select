package com.sourcesense.jira.customfield;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.OptionUtils;

/**
 * @author Alessandro Benedetti
 *
 */
public class MultiLevelCascadingSelectValue {
    private String value;
    private OptionsManager optionsManager;

    public MultiLevelCascadingSelectValue(OptionsManager optionsManager, String value) {
        this.optionsManager = optionsManager;
        this.value = value;
    }

    public String getSearchValue() {
        return value;
    }

   
    /**
     * 
     * return the list of options, splitting the string value
     * c'è da capire perchè alcune option gli arrivano per valore e altre per idùindagare
     * @return
     */
    private List<String> getOptionList() {
        List<String> list = new ArrayList<String>();
        
        if (value!=null&&!value.equals("")) {
          System.out.println("VALUE:"+value);
          List<String> values = Arrays.asList(value.split(":"));
          for (String value : values) {
              Long optionId = OptionUtils.safeParseLong(value);
              System.out.println("Searching via OptionManager :"+optionId);
              if(optionId!=null){
              Option option = optionsManager.findByOptionId(optionId);
              list.add(option.toString());}
              else{
                List<Option> option = optionsManager.findByOptionValue(value);
                System.out.println(option.size()+"OPT 0 :"+option.get(0).toString());
                list.add(option.get(0).toString());
              }
          }
        }else{
          System.out.println("Value not present");
        }
        return list;
    }

    @Override
    public String toString() {
        return printList(getOptionList(), " - ");
    }

    private String printList(List<String> list, String separator) {
        StringBuffer result =  new StringBuffer();
        for(String value:list) {
            if (result.length() > 0) result.append(separator);
            result.append(value);
        }
        return result.toString();
    }
}