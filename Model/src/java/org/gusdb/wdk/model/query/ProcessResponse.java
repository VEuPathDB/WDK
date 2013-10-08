package org.gusdb.wdk.model.query;

import java.util.Map;

import org.gusdb.wdk.model.WdkModelException;

public interface ProcessResponse {

  /**
   * get the result of the current page;
   * 
   * @return
   */
  String[][] getResult();

  /**
   * get the result of a given page; The result of a given page can only be
   * retrieved once. For example, if the current page index is 0, and
   * getResult() has been called before, calling getResult(0) will return null.
   * 
   * @param pageId
   * @return
   * @throws WdkModelException
   */
  String[][] getResult(int pageId) throws WdkModelException;

  String getMessage();

  int getSignal();

  int getInvokeId();

  int getPageCount();

  int getCurrentPage();

  Map<String, String> getAttachments();
}
