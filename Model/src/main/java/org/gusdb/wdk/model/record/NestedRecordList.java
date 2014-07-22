package org.gusdb.wdk.model.record;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.question.Question;

/**
 * <p>
 * A nestedRecordList can be defined in the {@code <recordClas>} tag, so that
 * the current {@link RecordInstance} can reuse other record instance's data and
 * display its attributes in the record page.
 * </p>
 * 
 * <p>
 * The nestedRecordList has a reference to a {@link Question}, and the
 * {@link Query} in that {@link Question} should be defined in a similar way as
 * a table query, which means that query should not have any {@link Param}s, and
 * it will return the {@link Column}s that match the primary key columns of the
 * current {@link RecordClass}. Moreover, since this query is also an ID query
 * of that other {@link RecordClass}, so it should also return the primary key
 * columns of that record as well.
 * </p>
 * 
 * <p>
 * Furthermore, since this query is similar to a table query, for a given
 * record's primary key, the query can have zero or more rows matched.
 * </p>
 * 
 * @author jerric
 * 
 */
public class NestedRecordList extends WdkModelBase {

  private RecordClass parentRecordClass;
  private String questionTwoPartName;
  private Question question;

  // todo:
  // validate links between nested record query and parent record instance

  public NestedRecordList() {

  }

  public void setQuestionRef(String questionTwoPartName) {
    this.questionTwoPartName = questionTwoPartName;
  }

  public String getTwoPartName() {
    return questionTwoPartName;
  }

  public Question getQuestion() {
    return this.question;
  }

  /**
   * @return the parentRecordClass this nested one reference to.
   */
  public RecordClass getParentRecordClass() {
    return parentRecordClass;
  }

  /**
   * @param parentRecordClass
   *          the parentRecordClass to set
   */
  public void setParentRecordClass(RecordClass parentRecordClass) {
    this.parentRecordClass = parentRecordClass;
  }

  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    this.question = (Question) wdkModel.resolveReference(questionTwoPartName);
    question.resolveReferences(wdkModel);

    // validate the query
    Query query = question.getQuery();
    query.resolveReferences(wdkModel);
    parentRecordClass.validateQuery(query);
    String[] paramNames = parentRecordClass.getPrimaryKeyAttributeField().getColumnRefs();

    // prepare the query and add primary key params
    query = RecordClass.prepareQuery(wdkModel, query, paramNames);
    question.setQuery(query);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.WdkModelBase#excludeResources(java.lang.String)
   */
  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    // do nothing
  }
}
