package com.sourcesense.jira.customfield.searcher.indexer;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

import java.util.Collection;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.converters.SelectConverter;
import com.atlassian.jira.issue.customfields.impl.CascadingSelectCFType;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.indexers.impl.AbstractCustomFieldIndexer;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.util.NonInjectableComponent;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.sourcesense.jira.customfield.MultiLevelCascadingSelectValue;
import com.sourcesense.jira.customfield.type.MultiLevelCascadingSelectCFType;

/**
 * A custom field indexer for the multi level cascading select custom fields.
 * This class indexes the options saved for each multi level cascading entry in each multilevel cascading select custom field.
 * 
 * @since v4.0
 * @author Alessandro Benedetti
 *
 */
/**
 * @author developer
 *
 */
@NonInjectableComponent
public class MultiLevelCascadingSelectIndexer extends AbstractCustomFieldIndexer {
  public static final String CHILD_INDEX_SUFFIX = ":" + CascadingSelectCFType.CHILD_KEY;

  private static final Logger log = Logger.getLogger(MultiLevelCascadingSelectIndexer.class);



  private final CustomField customField;

  // /CLOVER:OFF

  public MultiLevelCascadingSelectIndexer(final FieldVisibilityManager fieldVisibilityManager, final CustomField customField, JqlSelectOptionsUtil jqlSelectOptionsUtil, SelectConverter selectConverter) {
    super(fieldVisibilityManager, notNull("customField", customField));
    this.customField = customField;
  
  }

  @Override
  public void addDocumentFieldsSearchable(final Document doc, final Issue issue) {
    addDocumentFields(doc, issue, Field.Index.NOT_ANALYZED);
  }

  @Override
  public void addDocumentFieldsNotSearchable(final Document doc, final Issue issue) {
    addDocumentFields(doc, issue, Field.Index.NO);
  }

  /**
   * indexes the custom field params extracting the options from the custom field and building a Document (to use in Lucene Indexing)
   * @param doc
   * @param issue
   * @param indexType
   */
  private void addDocumentFields(final Document doc, final Issue issue, final Field.Index indexType) {
    final Object value = customField.getValue(issue);
    if (value instanceof CustomFieldParams) {
      final CustomFieldParams customFieldParams = (CustomFieldParams) value;
      
      // Add a field with a string containing all options (cfid:0)
      addField(doc, getDocumentFieldId()+":0", serializeOptions(customFieldParams), indexType);
      // Add a field with the first option .. (cfid)
      indexParentField(customFieldParams, doc, indexType);
      // .. and other fields with the remaining option  (cfid:1, cfid:2,...)
      this.indexAllLevels(customFieldParams, doc, indexType);
      
      
    }
  }

  /**
   * indexes all the info contained in all child-level of the custom field.
   * Remember that the Multi level cascading select allows you to create n levels of children from the parent node.
   * @param customFieldParams
   * @param doc
   * @param indexType
   */
  private void indexAllLevels(final CustomFieldParams customFieldParams, final Document doc, final Field.Index indexType) {
    final Collection values = customFieldParams.getAllValues();
    if ((values != null) && !values.isEmpty()) {
      for (String level : customFieldParams.getAllKeys()) {
        Option currentOption;
        if (level != MultiLevelCascadingSelectCFType.PARENT_KEY) {
          currentOption = getOpt(customFieldParams.getValuesForKey(level));
          if (currentOption != null) {
            String indexFieldName = getDocumentFieldId();
            indexFieldName += ":" + level;
            log.debug("Indexing :" + currentOption + "With ID=" + indexFieldName);
            addField(doc, indexFieldName, currentOption.getOptionId().toString(), indexType);
          }
        }
      }
    }
  }
  
  /**
   * Serialize option to the string "opt1 - opt2 - opt3 ..."
   * 
   * @param customFieldParams
   * @return
   */
  private String serializeOptions(final CustomFieldParams customFieldParams){
	  String serializedOption= "";
	  TreeMap<String,String> level2value=new TreeMap<String,String>(new LevelStringComparator());
	  	
	  for (String level : customFieldParams.getAllKeys()) {
	      Option currentOption;
          currentOption = getOpt(customFieldParams.getValuesForKey(level));
          //System.out.println("Level:"+level+"value:"+currentOption.getValue());
          if (currentOption != null) {
            level2value.put(level, currentOption.getValue());
          }
	  }
	  for(String levelOrdered:level2value.keySet()){
	  
	  if(serializedOption.equals("")){
      serializedOption +=level2value.get(levelOrdered);
    }else{
      serializedOption += " - "+level2value.get(levelOrdered);
    }}
	  System.out.println("Serialized Options:"+serializedOption);
	  return serializedOption;
  }
  

  
  /**
   * indexes the Parent Option value in the Lucene Doc.
   * @param customFieldParams
   * @param doc
   * @param indexType
   */
  private void indexParentField(final CustomFieldParams customFieldParams, final Document doc, final Field.Index indexType) {
    final Collection values = customFieldParams.getValuesForKey(MultiLevelCascadingSelectCFType.PARENT_KEY);
    if ((values != null) && !values.isEmpty()) {
      final Option selectedValue = (Option) values.iterator().next();
      addField(doc, getDocumentFieldId(), selectedValue.getOptionId().toString(), indexType);
    }
  }



  private void addField(final Document doc, final String indexFieldName, final String value, final Field.Index indexType) {
    doc.add(new Field(indexFieldName, value, Field.Store.YES, indexType));
  }



  private Option getOpt(final Collection values) throws NumberFormatException {
    if (values == null || values.isEmpty()) {
      return null;
    }
    Option current = (Option) values.iterator().next();
    return current;
  }

  // /CLOVER:ON
}
