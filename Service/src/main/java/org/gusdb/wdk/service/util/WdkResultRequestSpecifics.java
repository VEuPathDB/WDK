package org.gusdb.wdk.service.util;

import java.util.Collections;
import java.util.List;

import org.gusdb.wdk.model.jspwrap.WdkModelBean;
import org.gusdb.wdk.model.query.Column;
import org.json.JSONException;
import org.json.JSONObject;

public class WdkResultRequestSpecifics {

  public static enum Direction {
    ASC(true),
    DESC(false);

    private boolean _boolValue;

    private Direction(boolean boolValue) {
      _boolValue = boolValue;
    }

    public boolean getBoolValue() {
      return _boolValue;
    }

    public Direction getFromBool(boolean boolValue) {
      return (boolValue == ASC._boolValue ? ASC : DESC);
    }
  }

  public static class SortItem {
    private Column _column;
    private Direction _direction;
    public SortItem(Column column, Direction direction) {
      _column = column;
      _direction = direction;
    }
    public Column getColumn() { return _column; }
    public Direction getDirection() { return _direction; }
  }
  
  /*
  {
    "pagination": {
      "offset": 0,
      "numRecords": 10
    },
    "columns": null,
    "sorting": null
  }
  */
  public static WdkResultRequestSpecifics createFromJson(
      JSONObject specJson, WdkModelBean wdkModelBean) throws JSONException {
    WdkResultRequestSpecifics specs = new WdkResultRequestSpecifics();
    JSONObject paging = specJson.getJSONObject("pagination");
    specs._offset = paging.getInt("offset");
    specs._numRecords = paging.getInt("numRecords");
    return specs;
  }

  private int _offset;
  private int _numRecords;

  // TODO: support sorting
  public List<SortItem> getSorting() {
    return Collections.<SortItem>emptyList();
  }

  public int getOffset() {
    return _offset;
  }

  public int getNumRecords() {
    return _numRecords;
  }

}
