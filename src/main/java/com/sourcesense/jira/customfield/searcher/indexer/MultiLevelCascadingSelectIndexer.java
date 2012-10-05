package com.sourcesense.jira.customfield.searcher.indexer;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.converters.SelectConverter;
import com.atlassian.jira.issue.customfields.impl.CascadingSelectCFType;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.index.indexers.impl.AbstractCustomFieldIndexer;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.util.NonInjectableComponent;
import com.atlassian.jira.web.FieldVisibilityManager;

/**
 * A custom field indexer for the multi level cascading select custom fields.
 * This class indexes the options saved for each multi level cascading entry in
 * each multilevel cascading select custom field.
 *
 * @since v4.0
 * @author Alessandro Benedetti
 */
@NonInjectableComponent
public class MultiLevelCascadingSelectIndexer extends AbstractCustomFieldIndexer {

  private static final Logger log = Logger.getLogger(MultiLevelCascadingSelectIndexer.class);

  private final CustomField customField;

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

  /*
   * indexes the custom field params extracting the options from the custom field and building a Document (to use in Lucene Indexing)
   * Updated to Jira 5.0.4 To Check in deep but probably OK
   */
  private void addDocumentFields(final Document doc, final Issue issue, final Field.Index indexType) {
      final Map<String, Option> cascadingOptions = (Map<String, Option>) customField.getValue(issue);

      if (cascadingOptions != null) {
          // Add a field with a string containing all options (cfid:0)
          addField(doc, getDocumentFieldId() + ":0", serializeOptions(cascadingOptions), indexType);
          // Add a field with the first option .. (cfid)
          indexParentField(cascadingOptions, doc, indexType);
          // .. and other fields with the remaining option  (cfid:1, cfid:2,...)
          this.indexAllLevels(cascadingOptions, doc, indexType);
      }
  }
  
  /*
   * Serialize option to the string "opt1 - opt2 - opt3 ..."
   * updated jira 5.0.4 TO CHECK
   */
  private String serializeOptions(Map<String, Option> cascadingOptions) {
      StringBuilder serializedOption = new StringBuilder();
      Option currentOption = cascadingOptions.get("0");

      if (currentOption != null) {
          serializedOption.append(currentOption.getValue());
      }

      for (int i = 1; i < cascadingOptions.size(); i++) {
          currentOption = cascadingOptions.get("" + i);
          if (currentOption != null) {
              serializedOption.append(" - ").append(currentOption.getValue());
          }
      }

      return serializedOption.toString();
  }

  
  /*
   * indexes all the info contained in all child-level of the custom field.
   * Remember that the Multi level cascading select allows you to create n levels of children from the parent node.
   * Updated to Jira 5.04 To Check
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
  
  /*
   * indexes the Parent Option value in the Lucene Doc.
   */
  private void indexParentField(Map<String, Option> cascadingOptions, final Document doc, final Field.Index indexType) {
    if ((cascadingOptions != null) && !cascadingOptions.isEmpty()) {
      final Option selectedValue = cascadingOptions.get("0");
      addField(doc, getDocumentFieldId(), selectedValue.getOptionId().toString(), indexType);
    }
  }
  

  private void addField(final Document doc, final String indexFieldName, final String value, final Field.Index indexType) {
    doc.add(new Field(indexFieldName, value, Field.Store.YES, indexType));
  }

}
