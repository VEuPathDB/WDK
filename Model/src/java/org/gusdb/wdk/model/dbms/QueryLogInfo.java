package org.gusdb.wdk.model.dbms;

import org.gusdb.wdk.model.WdkModel;

public class QueryLogInfo {

  WdkModel wdkModel;
  String sql;
  String name;
  long startTime;
  long firstPageTime;

  public QueryLogInfo(WdkModel wdkModel, String sql, String name,
		      long startTime, long firstPageTime) {
    this.wdkModel = wdkModel;
    this.sql = sql;
    this.name = name;
    this.startTime = startTime;
    this.firstPageTime = firstPageTime;
  }
}
