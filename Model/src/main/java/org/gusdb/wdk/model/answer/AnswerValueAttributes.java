package org.gusdb.wdk.model.answer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.wdk.model.FieldTree;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.FieldScope;
import org.gusdb.wdk.model.record.attribute.AttributeCategoryTree;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.PrimaryKeyAttributeField;
import org.gusdb.wdk.model.user.User;

/**
 * Encapsulates the methods available to retrieve and override the available
 * attributes in an answer value.  Attributes are available in the following
 * formats and groups:
 * 
 * Displayable attributes:
 *   List<AttributeField> getDisplayableAttributes()
 *   Map<String, AttributeField> getDisplayableAttributeMap()
 *   TreeNode getDisplayableAttributeTree()
 * ReportMaker attributes:
 *   TreeNode getReportMakerAttributeTree()
 * Summary:
 *   Map<String, AttributeField> getSummaryAttributeFieldMap()
 * 
 * @author rdoherty
 */
public class AnswerValueAttributes {

  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(AnswerValueAttributes.class);

  private final User _user;
  private final Question _question;

  // these fields may be used to override the default attributes associated with the answer value
  private FieldTree _displayableAttributeTree;
  private Map<String, AttributeField> _summaryAttributeMap;

  public AnswerValueAttributes(User user, Question question) {
    _user = user;
    _question = question;
  }

  public List<AttributeField> getDisplayableAttributes() {
    Map<String, AttributeField> map = getDisplayableAttributeMap();
    return new ArrayList<AttributeField>(map.values());
  }

  /**
   * The displayable includes all attributes that is not internal. It also contains all the summary attributes
   * that are currently displayed.
   * 
   * @return
   */
  public Map<String, AttributeField> getDisplayableAttributeMap() {
    Map<String, AttributeField> displayAttributes = new LinkedHashMap<String, AttributeField>();
    Map<String, AttributeField> attributes = _question.getAttributeFieldMap(FieldScope.NON_INTERNAL);
    // Map<String, AttributeField> summaryAttributes =
    // this.getSummaryAttributeFieldMap();
    for (String attriName : attributes.keySet()) {
      AttributeField attribute = attributes.get(attriName);
      displayAttributes.put(attriName, attribute);
    }
    return displayAttributes;
  }

  public void overrideDisplayableAttributeTree(FieldTree attributeTree) {
    _displayableAttributeTree = attributeTree;
  }

  public FieldTree getDisplayableAttributeTree() throws WdkModelException {
    if (_displayableAttributeTree == null) {
      _displayableAttributeTree = convertAttributeTree(
          _question.getAttributeCategoryTree(FieldScope.NON_INTERNAL));
    }
    return _displayableAttributeTree;
  }

  public FieldTree getReportMakerAttributeTree() throws WdkModelException {
    return convertAttributeTree(_question.getAttributeCategoryTree(FieldScope.REPORT_MAKER));
  }

  private FieldTree convertAttributeTree(AttributeCategoryTree rawAttributeTree) throws WdkModelException {
    FieldTree tree = rawAttributeTree.toFieldTree("category root", "Attribute Categories");
    List<String> currentlySelectedFields = new ArrayList<String>();
    for (AttributeField field : getSummaryAttributeFieldMap().values()) {
      currentlySelectedFields.add(field.getName());
    }
    tree.setSelectedLeaves(currentlySelectedFields);
    tree.addDefaultLeaves(new ArrayList<String>(_question.getSummaryAttributeFieldMap().keySet()));
    return tree;
  }

  public void overrideSummaryAttributeFieldMap(Map<String, AttributeField> summaryAttributeMap) {
    _summaryAttributeMap = summaryAttributeMap;
  }

  public Map<String, AttributeField> getSummaryAttributeFieldMap() throws WdkModelException {
    if (_summaryAttributeMap == null) {
      PrimaryKeyAttributeField pkField = _question.getRecordClass().getPrimaryKeyAttributeField();
      _summaryAttributeMap = buildSummaryAttributeFieldMap(_user, _question, User.DEFAULT_SUMMARY_VIEW_PREF_SUFFIX, pkField);
    }
    //LOG.debug("Returning summary field map with keys: " +
    FormatUtil.arrayToString(_summaryAttributeMap.keySet().toArray());
    return _summaryAttributeMap;
  }

  public static Map<String, AttributeField> buildSummaryAttributeFieldMap(
      User user, Question question, String keySuffix, AttributeField pkField) throws WdkModelException {
    // get preferred attribs from user and initialize map
    String[] userPrefAttributes = user.getSummaryAttributes(question.getFullName(), keySuffix);
    Map<String, AttributeField> summaryFields = new LinkedHashMap<String, AttributeField>();

    // always put the primary key as the first attribute
    summaryFields.put(pkField.getName(), pkField);

    // add remainder of attributes to map and return
    Map<String, AttributeField> allFields = question.getAttributeFieldMap();
    for (String attributeName : userPrefAttributes) {
      AttributeField field = allFields.get(attributeName);
      if (field != null)
        summaryFields.put(attributeName, field);
    }
    return summaryFields;
  }
}
