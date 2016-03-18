package org.gusdb.wdk.model.report;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import javax.sql.DataSource;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.AttributeValue;
import org.gusdb.wdk.model.record.attribute.PrimaryKeyAttributeValue;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.TableValueRow;
import org.gusdb.wdk.model.record.FieldScope;
import org.apache.log4j.Logger;


import org.gusdb.wdk.model.dbms.ResultList;

public class TableRowProvider implements SingleTableReporterRowsProvider {
     private static Logger LOG = Logger.getLogger(SingleTableReporterRowsProvider.class);

  AnswerValue answerValuePage;
  private int recordInstancesCursor = 0;
  private TableField tableField;
  private boolean hasNext;
  private ResultList resultList;
 
  TableRowProvider(AnswerValue answerValuePage, TableField tableField) {
            AttributeField[] fields = tableField.getAttributeFields(FieldScope.REPORT_MAKER);
    this.answerValuePage = answerValuePage;
    this.tableField = tableField;
  }

  private ResultList getResultList() throws WdkModelException, WdkUserException {
    if (resultList == null)
      resultList =  answerValuePage.getTableFieldResultList(tableField);
    return resultList;
  }
   
  // play games with flags to workaround ResultList not having a hasNext() method
  public boolean hasNext() throws WdkModelException, WdkUserException {
    if (!hasNext) hasNext = getResultList().next();
    return hasNext;
  }
  
  public List<Object> next() throws WdkModelException, WdkUserException {
    if (!hasNext()) throw new NoSuchElementException();
    hasNext = false;

    ResultList resultList = getResultList();
    resultList.next();

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

  public void close() throws WdkModelException, WdkUserException {
    getResultList().close();
  }
  
}
