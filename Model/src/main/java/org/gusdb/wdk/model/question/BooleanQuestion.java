package org.gusdb.wdk.model.question;

import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.BooleanQuery;
import org.gusdb.wdk.model.query.QuerySet;
import org.gusdb.wdk.model.record.RecordClass;

public class BooleanQuestion extends Question {

  public static final String BOOLEAN_QUESTION_PREFIX = "boolean_question_";

  public static String getQuestionName(RecordClass recordClass) {
    return getInternalQuestionName(BOOLEAN_QUESTION_PREFIX, recordClass);
  }

  public BooleanQuestion(RecordClass recordClass) throws WdkModelException {
    WdkModel wdkModel = recordClass.getWdkModel();
    setName(getQuestionName(recordClass));
    setDisplayName("Combine " + recordClass.getDisplayName() + " results");
    setRecordClassRef(recordClass.getFullName());
    BooleanQuery booleanQuery = addBooleanQuery(recordClass);
    setQueryRef(booleanQuery.getFullName());
    excludeResources(wdkModel.getProjectId());
    resolveReferences(wdkModel);
  }

  private BooleanQuery addBooleanQuery(RecordClass recordClass) throws WdkModelException {
    // check if the boolean query already exists
    WdkModel wdkModel = recordClass.getWdkModel();
    String queryName = BooleanQuery.getQueryName(recordClass);
    QuerySet internalQuerySet = wdkModel.getQuerySet(Utilities.INTERNAL_QUERY_SET);

    BooleanQuery booleanQuery;
    if (internalQuerySet.contains(queryName)) {
      booleanQuery = (BooleanQuery) internalQuerySet.getQuery(queryName);
    }
    else {
      booleanQuery = recordClass.getBooleanQuery();

      // make sure we create index on primary keys
      booleanQuery.setIndexColumns(recordClass.getIndexColumns());

      internalQuerySet.addQuery(booleanQuery);

      booleanQuery.excludeResources(wdkModel.getProjectId());
      booleanQuery.resolveReferences(wdkModel);
      booleanQuery.setDoNotTest(true);
      booleanQuery.setIsCacheable(true); // cache the boolean query
    }
    return booleanQuery;
  }
}
