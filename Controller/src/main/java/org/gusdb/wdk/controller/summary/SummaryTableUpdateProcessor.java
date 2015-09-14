package org.gusdb.wdk.controller.summary;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.wdk.controller.CConstants;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.user.Step;
import org.gusdb.wdk.model.user.User;

/**
 * This class handles user preference updates for any summary view that uses
 * the WDK result table.  Result tables depend on user preferences for column
 * selection, ordering, and sorting.  Since each summary view's preferences
 * are independent of each other's, the 'standard' key used to fetch them is
 * postpended with a SummaryView-specific suffix.
 * 
 * @author rdoherty
 */
public class SummaryTableUpdateProcessor {

  private static final Logger logger = Logger.getLogger(SummaryTableUpdateProcessor.class);

  private static final String PARAM_COMMAND = "command";
  private static final String PARAM_ATTRIBUTE = "attribute";
  private static final String PARAM_SORT_ORDER = "sortOrder";
  private static final String PARAM_PAGER_OFFSET = "pager.offset";

  private static String getFirstValueOrNull(String[] values) {
    if (values == null || values.length == 0) return null;
    return values[0];
  }

  public static String processUpdates(Step step, Map<String, String[]> params,
      User user, WdkModel model, String preferenceSuffix) throws WdkModelException {

    logger.info("Applying summary table preference changes with suffix '" + preferenceSuffix + "' and params: " +
        FormatUtil.paramsToString(params));
    
    try {
      String command = getFirstValueOrNull(params.get(PARAM_COMMAND));
      String attributeName = getFirstValueOrNull(params.get(PARAM_ATTRIBUTE));
      String sortingOrder = getFirstValueOrNull(params.get(PARAM_SORT_ORDER));
      String pagerOffset = getFirstValueOrNull(params.get(PARAM_PAGER_OFFSET));

      Question question = step.getQuestion();
      String questionName = question.getFullName();

      // handle sorting
      String sorting = getFirstValueOrNull(params.get(CConstants.WDK_SORTING_KEY));
      if (sorting != null && sorting.length() > 0) {
        // apply the current sorting to the question first, then do other commands
        user.setSortingAttributes(questionName, sorting, preferenceSuffix);
      }

      // get command
      if (command != null) {
        if (command.equalsIgnoreCase("sort")) { // sorting
          boolean ascending = !sortingOrder.equalsIgnoreCase("DESC");
          user.addSortingAttribute(questionName, attributeName, ascending, preferenceSuffix);
        }
        else if (command.equalsIgnoreCase("reset")) {
          user.resetSummaryAttributes(questionName, preferenceSuffix);
        }
        else {
          String[] summary = user.getSummaryAttributes(questionName, preferenceSuffix);
          List<String> summaryList = new ArrayList<String>();
          String[] attributeNames = params.get(CConstants.WDK_SUMMARY_ATTRIBUTE_KEY);
          if (attributeNames == null) attributeNames = new String[0];

          if (command.equalsIgnoreCase("update")) {
            List<String> attributeNamesList = new ArrayList<String>(Arrays.asList(attributeNames));
            for (String summaryName : summary) {
              if (attributeNamesList.contains(summaryName)) {
                summaryList.add(summaryName);
                attributeNamesList.remove(summaryName);
              }
            }
            for (String attrName : attributeNamesList) {
              summaryList.add(attrName);
            }
          }
          else {
            for (String attribute : summary) {
              summaryList.add(attribute);
            }

            if (command.equalsIgnoreCase("add")) {
              for (String attrName : attributeNames) {
                if (!summaryList.contains(attrName))
                  summaryList.add(attrName);
              }
            }
            else if (command.equalsIgnoreCase("remove")) {
              for (String attrName : attributeNames) {
                summaryList.remove(attrName);
              }
            }
            else if (command.equalsIgnoreCase("arrange")) {
              // Get the attribute that will be to the left of
              // attributeName after attributeName is moved
              String attributeToLeft = getFirstValueOrNull(params.get(CConstants.WDK_SUMMARY_ARRANGE_ORDER_KEY));
              // If attributeToLeft is null (or not in list), make
              // attributeName the first element.
              // Otherwise, make it the first element AFTER
              // attributeToLeft
              for (String attrName : attributeNames) {
                summaryList.remove(attrName);
                int toIndex = summaryList.indexOf(attributeToLeft) + 1;
                summaryList.add(toIndex, attrName);
              }
            }
            else {
              throw new WdkModelException("Unknown command: " + command);
            }
          }

          summary = new String[summaryList.size()];
          summaryList.toArray(summary);
          user.setSummaryAttributes(questionName, summary, preferenceSuffix);
        }

        user.save();
      }

      if (pagerOffset != null && pagerOffset.length() != 0) {
        return "&pager.offset=" + pagerOffset;
      }
      return null;
    }
    catch (Exception e) {
      logger.error("Error executing summary view update.", e);
      throw new WdkModelException(e);
    }
  }
}
