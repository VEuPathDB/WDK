package org.gusdb.wdk.model.question;

import java.util.LinkedHashMap;
import java.util.Map;

import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;

/**
 * This class represents the @{code &lt;question>/&lt;attributeList>} tag. It is used
 * to define summary attribute list and sorting attribute list.
 *
 * @author Jerric
 */
public class AttributeList extends WdkModelBase {

  private String[] summaryAttributeNames;
  private Map<String, Boolean> sortingAttributeMap;

  public AttributeList() {
    sortingAttributeMap = new LinkedHashMap<>();
  }

  public void setSummary(String summaryList) {
    this.summaryAttributeNames = summaryList.split(",\\s*");
  }

  public void setSorting(String sortList) throws WdkModelException {
    sortingAttributeMap = Utilities.parseSortList(sortList);
  }

  /**
   * @return the sortingAttributeMap
   */
  public Map<String, Boolean> getSortingAttributeMap() {
    return this.sortingAttributeMap;
  }

  /**
   * @return the summaryAttributeNames
   */
  public String[] getSummaryAttributeNames() {
    return this.summaryAttributeNames;
  }

  @Override
  public void excludeResources(String projectId) {
    // no resource to release, do nothing
  }

  @Override
  public void resolveReferences(WdkModel wodkModel) throws WdkModelException {
    // nothing to resolve.
  }
}
