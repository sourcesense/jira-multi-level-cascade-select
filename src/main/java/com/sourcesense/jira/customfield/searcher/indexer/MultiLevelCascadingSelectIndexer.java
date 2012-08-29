package com.sourcesense.jira.customfield.searcher.indexer;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

import java.util.Collection;
import java.util.Map;
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
   * Updated to Jira 5.0.4 To Check in deep but probably OK
   * @param doc
   * @param issue
   * @param indexType
   */
  private void addDocumentFields(final Document doc, final Issue issue, final Field.Index indexType) {
    final Map<String, Option> cascadingOptions = (Map<String, Option>) customField.getValue(issue);

      // Add a field with a string containing all options (cfid:0)
      addField(doc, getDocumentFieldId()+":0", serializeOptions(cascadingOptions), indexType);
      // Add a field with the first option .. (cfid)
      indexParentField(cascadingOptions, doc, indexType);
      // .. and other fields with the remaining option  (cfid:1, cfid:2,...)
      this.indexAllLevels(cascadingOptions, doc, indexType);
      
      
    }
  
  /**
   * Serialize option to the string "opt1 - opt2 - opt3 ..."
   * updated jira 5.0.4 TO CHECK
   * @param cascadingOptions
   * @return
   */
  private String serializeOptions(Map<String, Option> cascadingOptions) {
    StringBuilder serializedOption=new StringBuilder();
    Option currentOption;
    currentOption = cascadingOptions.get(""+0);
    if(currentOption!=null)
    serializedOption.append(currentOption.getValue());
    for (int i=1;i<cascadingOptions.size();i++) {
          currentOption = cascadingOptions.get(""+i);
          if (currentOption != null) {
            serializedOption.append(" - "+currentOption.getValue());
          }
    }
    return serializedOption.toString();
  }

  
  /**
   * indexes all the info contained in all child-level of the custom field.
   * Remember that the Multi level cascading select allows you to create n levels of children from the parent node.
   * Updated to Jira 5.04 To Check
   * @param customFieldParams
   * @param doc
   * @param indexType
   */
  private void indexAllLevels(Map<String, Option> cascadingOptions, final Document doc, final Field.Index indexType) {
    Option currentOption;
    if ((cascadingOptions != null) && !cascadingOptions.isEmpty()) {
      for (int i=1;i<cascadingOptions.size();i++) {
          currentOption = cascadingOptions.get(""+i);
          if (currentOption != null) {
            String indexFieldName = getDocumentFieldId();
            indexFieldName += ":" + i;
            log.debug("Indexing :" + currentOption + "With ID=" + indexFieldName);
            addField(doc, indexFieldName, currentOption.getOptionId().toString(), indexType);
          }
        
      }
    }
  }
  
  /**
   * indexes the Parent Option value in the Lucene Doc.
   * @param customFieldParams
   * @param doc
   * @param indexType
   */
  private void indexParentField(Map<String, Option> cascadingOptions, final Document doc, final Field.Index indexType) {
    if ((cascadingOptions != null) && !cascadingOptions.isEmpty()) {
      final Option selectedValue = cascadingOptions.get(""+0);
      addField(doc, getDocumentFieldId(), selectedValue.getOptionId().toString(), indexType);
    }
  }
  

  private void addField(final Document doc, final String indexFieldName, final String value, final Field.Index indexType) {
    doc.add(new Field(indexFieldName, value, Field.Store.YES, indexType));
  }

}
