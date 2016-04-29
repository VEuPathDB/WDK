package org.gusdb.wdk.model.report;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.dbms.ResultList;
import org.gusdb.wdk.model.record.FieldScope;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.TableValueRow;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.PrimaryKeyAttributeValue;
//import org.apache.log4j.Logger;


public class TableRowProvider implements TabularReporterRowsProvider {
  //  private static final Logger logger = Logger.getLogger(TableRowProvider.class);

  AnswerValue answerValuePage;
  private TableField tableField;
  private boolean hasNext;
  private ResultList resultList;
 
  TableRowProvider(AnswerValue answerValuePage, TableField tableField) {
    this.answerValuePage = answerValuePage;
    this.tableField = tableField;
  }

  private ResultList getResultList() throws WdkModelException, WdkUserException {
    if (resultList == null) 
      resultList =  answerValuePage.getTableFieldResultList(tableField);
    return resultList;
  }
   
  // play games with flags to workaround ResultList not having a hasNext() method
  @Override
  public boolean hasNext() throws WdkModelException, WdkUserException {
    if (!hasNext) hasNext = getResultList().next();
    return hasNext;
  }
  
  @Override
  public List<Object> next() throws WdkModelException, WdkUserException {
    if (!hasNext()) throw new NoSuchElementException();
    hasNext = false;
    ResultList resultList = getResultList();

    // make a tableValueRow for this row in the result set.  provides the record's formatting of a row in this table
    PrimaryKeyAttributeValue primaryKey = AnswerValue.getPrimaryKeyFromResultList(resultList, answerValuePage.getPrimaryKeyAttributeField());
    TableValueRow tableValueRow = new TableValueRow(primaryKey, tableField);
    tableValueRow.initializeFromResultList(resultList);

    List<Object> values = new ArrayList<Object>();
    AttributeField[] fields = tableField.getAttributeFields(FieldScope.REPORT_MAKER);
    for (AttributeField field : fields) {
      Object value = tableValueRow.get(field.getName());
      values.add((value == null) ? "N/A" : value);
    }
    return values;
  }

  @Override
  public void close() throws WdkModelException, WdkUserException {
    getResultList().close();
  }
  
}
