package org.gusdb.wdk.controller.summary;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;
import org.gusdb.wdk.model.jspwrap.QuestionBean;
import org.gusdb.wdk.model.jspwrap.UserBean;

public class ResultTablePaging {

  public static int getPageStart(Map<String, String[]> params) {
      int start = 1;
      if (params.containsKey("pager.offset")) {
          start = Integer.parseInt(params.get("pager.offset")[0]);
          start++;
          if (start < 1) start = 1;
      }
      return start;
  }

  public static int getPageSize(Map<String, String[]> params,
          QuestionBean question, UserBean user) throws WdkModelException {
      int pageSize = user.getItemsPerPage();
      // check if the question is supposed to make answers containing all
      // records in one page
      if (question.isFullAnswer()) {
          pageSize = Utilities.MAXIMUM_RECORD_INSTANCES;
      }
      else {
          String[] pageSizeKey = params.get(CConstants.WDK_PAGE_SIZE_KEY);
          if (pageSizeKey != null) {
              pageSize = Integer.parseInt(pageSizeKey[0]);
              user.setItemsPerPage(pageSize);
          }
          else {
              String[] altPageSizeKey = params.get(CConstants.WDK_ALT_PAGE_SIZE_KEY);
              if (altPageSizeKey != null)
                  pageSize = Integer.parseInt(altPageSizeKey[0]);
          }
          // set the minimal page size
          if (pageSize < CConstants.MIN_PAGE_SIZE)
              pageSize = CConstants.MIN_PAGE_SIZE;
      }
      return pageSize;
  }

  public static Map<String, Object> processPaging(Map<String, String[]> params, QuestionBean question, UserBean user, AnswerValueBean answerValue)
          throws WdkModelException, WdkUserException {
      int start = getPageStart(params);
      int pageSize = getPageSize(params, question, user);
      int totalSize = answerValue.getResultSize();

      if (start > totalSize) {
          int pages = totalSize / pageSize;
          start = (pages * pageSize) + 1;
      }

      int end = start + pageSize - 1;

      answerValue.setPageIndex(start, end);

      List<String> editedParamNames = new ArrayList<String>();
      for (String key : params.keySet()) {
          if (!key.equals(CConstants.WDK_PAGE_SIZE_KEY)
                  && !key.equals(CConstants.WDK_ALT_PAGE_SIZE_KEY)
                  && !"start".equals(key) && !"pager.offset".equals(key)) {
              editedParamNames.add(key);
          }
      }

      Map<String, Object> model = new HashMap<>();
      model.put("wdk_paging_total", new Integer(totalSize));
      model.put("wdk_paging_pageSize", new Integer(pageSize));
      model.put("wdk_paging_start", new Integer(start));
      model.put("wdk_paging_end", new Integer(end));
      model.put("wdk_paging_params", editedParamNames);
      return model;
  }
}
