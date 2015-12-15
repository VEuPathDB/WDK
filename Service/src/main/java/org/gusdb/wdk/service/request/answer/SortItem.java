package org.gusdb.wdk.service.request.answer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.user.User;

/**
 * Represents a single sort characteristic, a tuple of which field to sort on
 * and which direction to sort.
 * 
 * @author rdoherty
 */
public class SortItem {

  private static final Logger LOG = Logger.getLogger(SortItem.class);

  private AttributeField _attributeField;
  private Direction _direction;

  public SortItem(AttributeField attributeField, Direction direction) {
    _attributeField = attributeField;
    _direction = direction;
  }

  public AttributeField getAttributeField() { return _attributeField; }
  public Direction getDirection() { return _direction; }

  /**
   * Enumeration of sort directions. Also provides translation between legacy
   * sort direction indicators (booleans) and these values.
   * 
   * @author rdoherty
   */
  public static enum Direction {

    ASC  (true),
    DESC (false);

    private boolean _boolValue;

    private Direction(boolean boolValue) {
      _boolValue = boolValue;
    }

    public boolean getBoolValue() {
      return _boolValue;
    }

    public static boolean isDirection(String str) {
      try {
        valueOf(str);
        return true;
      }
      catch (IllegalArgumentException | NullPointerException e) {
        return false;
      }
    }

    public static Direction fromBoolean(Boolean value) {
      for (Direction d : values()) {
        if (d._boolValue == value) return d;
      }
      throw new IllegalArgumentException("No direction has bool value " + value);
    }
  }

  public static List<SortItem> convertSorting(Map<String, Boolean> sortingAttributeMap, Map<String, AttributeField> allowedValues) {
    List<SortItem> sorting = new ArrayList<>();
    for (Entry<String, Boolean> sortingAttribute : sortingAttributeMap.entrySet()) {
      if (allowedValues.containsKey(sortingAttribute.getKey())) {
        sorting.add(new SortItem(
            allowedValues.get(sortingAttribute.getKey()),
            Direction.fromBoolean(sortingAttribute.getValue())));
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
      conversion.put(sort.getAttributeField().getName(), sort.getDirection().getBoolValue());
      numSorts++;
      // don't sort by more than maximum number of fields
      if (numSorts >= User.SORTING_LEVEL) break;
    }
    return conversion;
  }
}
