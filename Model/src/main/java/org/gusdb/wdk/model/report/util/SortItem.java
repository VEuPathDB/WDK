package org.gusdb.wdk.model.report.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.SortDirection;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.user.UserPreferences;

/**
 * Represents a single sort characteristic, a tuple of which field to sort on
 * and which direction to sort.
 * 
 * @author rdoherty
 */
public class SortItem {

  private static final Logger LOG = Logger.getLogger(SortItem.class);

  private AttributeField _attributeField;
  private SortDirection _direction;

  public SortItem(AttributeField attributeField, SortDirection direction) {
    _attributeField = attributeField;
    _direction = direction;
  }

  public AttributeField getAttributeField() { return _attributeField; }
  public SortDirection getDirection() { return _direction; }

  public static List<SortItem> convertSorting(Map<String, Boolean> sortingAttributeMap, Map<String, AttributeField> allowedValues) {
    List<SortItem> sorting = new ArrayList<>();
    for (Entry<String, Boolean> sortingAttribute : sortingAttributeMap.entrySet()) {
      if (allowedValues.containsKey(sortingAttribute.getKey())) {
        sorting.add(new SortItem(
            allowedValues.get(sortingAttribute.getKey()),
            SortDirection.getFromIsAscending(sortingAttribute.getValue())));
      }
      else {
        LOG.warn("Sort attribute [ " + sortingAttribute.getKey() + "] passed in but not found in allowed values.  Skipping...");
      }
    }
    return sorting;
  }

  public static Map<String, Boolean> convertSorting(List<SortItem> sorting) {
    Map<String, Boolean> conversion = new LinkedHashMap<>();
    int numSorts = 0;
    for (SortItem sort : sorting) {
      conversion.put(sort.getAttributeField().getName(), sort.getDirection().isAscending());
      numSorts++;
      // don't sort by more than maximum number of fields
      if (numSorts >= UserPreferences.SORTING_LEVEL) break;
    }
    return conversion;
  }
}
