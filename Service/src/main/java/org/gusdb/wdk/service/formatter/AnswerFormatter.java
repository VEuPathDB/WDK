package org.gusdb.wdk.service.formatter;

import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.service.request.answer.AnswerRequestSpecifics;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Formats WDK answer service responses.  JSON will have the following form:
 * {
 *   meta: {
 *     class: String,
 *     totalCount: Number,
 *     responseCount: Number,
 *     attributes: [ String ],
 *     tables: [ String ]
 *   },
 *   records: [ see RecordFormatter ]
 * }
 */
public class AnswerFormatter {

  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(AnswerFormatter.class);

  public static JSONObject formatAnswer(AnswerValueBean answerValue,
      AnswerRequestSpecifics specifics) throws WdkModelException {

    Set<String> attributeNames = specifics.getAttributes().keySet();
    Set<String> tableNames = specifics.getTables().keySet();
    try {
      JSONObject parent = new JSONObject();
      JSONArray records = new JSONArray();
      int numRecordsReturned = 0;
      for (RecordInstance record : answerValue.getAnswerValue().getRecordInstances()) {
        records.put(RecordFormatter.getRecordJson(record, attributeNames, tableNames));
        numRecordsReturned++;
      }
      parent.put(Keys.RECORDS, records);
      parent.put(Keys.META, getMetaData(answerValue, attributeNames, tableNames, numRecordsReturned));
      return parent;
    }
    catch (WdkUserException e) {
      // should already have validated any user input
      throw new WdkModelException("Internal validation failure", e);
    }
  }

  public static JSONObject getMetaData(AnswerValueBean answerValue,
      Set<String> includedAttributes, Set<String> includedTables, int numRecordsReturned)
      throws WdkModelException, WdkUserException {
    JSONObject meta = new JSONObject();
    meta.put(Keys.RECORD_CLASS_NAME, answerValue.getRecordClass().getFullName());
    meta.put(Keys.TOTAL_COUNT, answerValue.getResultSize());
    meta.put(Keys.RESPONSE_COUNT, numRecordsReturned);
    meta.put(Keys.ATTRIBUTES, FormatUtil.stringCollectionToJsonArray(includedAttributes));
    meta.put(Keys.TABLES, FormatUtil.stringCollectionToJsonArray(includedTables));
    return meta;
  }
}
