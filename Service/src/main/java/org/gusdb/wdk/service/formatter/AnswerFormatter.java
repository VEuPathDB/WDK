package org.gusdb.wdk.service.formatter;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;
import org.gusdb.wdk.model.jspwrap.AttributeFieldBean;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.service.request.answer.AnswerRequestSpecifics;
import org.json.JSONArray;
import org.json.JSONObject;

/**
JSON output format:
{
  meta: {
    count: Number,
    class: String,
    attributes: [ {
      name: String,
      displayName: String,
      sortable: Boolean,
      removable: Boolean,
      category: String,
      type: string (comes from “type” property of attribute tag),
      className: String,
      properties: Object
    } ],
    tables: [ {
      name: String,
      displayName: String,
      attributes: [ {
        name: String,
        displayName: String,
        sortable: Boolean,
      } ]
    } ]
  },
  records: [ see record formatter ]
}
*/
public class AnswerFormatter {

  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(AnswerFormatter.class);

  public static JSONObject formatAnswer(AnswerValueBean answerValue,
      AnswerRequestSpecifics specifics) throws WdkModelException {

    List<String> attributeNames = (specifics == null ? null : new ArrayList<String>(specifics.getAttributes().keySet()));
    List<String> tableNames = (specifics == null ? null : new ArrayList<String>(specifics.getTables().keySet()));
    try {
      JSONObject parent = new JSONObject();
      parent.put("meta", getMetaData(answerValue, attributeNames));
      JSONArray records = new JSONArray();
      for (RecordInstance record : answerValue.getAnswerValue().getRecordInstances()) {
        records.put(RecordFormatter.getRecordJson(record, attributeNames, tableNames));
      }
      parent.put("records", records);
      return parent;
    }
    catch (WdkUserException e) {
      // should already have validated any user input
      throw new WdkModelException("Internal validation failure", e);
    }
  }

  public static JSONObject getMetaData(AnswerValueBean answerValue, List<String> includedAttributes)
      throws WdkModelException, WdkUserException {
    JSONObject meta = new JSONObject();
    meta.put("count", answerValue.getResultSize());
    meta.put("class", answerValue.getRecordClass().getFullName());
    // FIXME: attributes should be retrieved from the question service, not answer service
    JSONArray attributes = new JSONArray();
    for (String attribName : includedAttributes) {
      AttributeFieldBean attrib = answerValue.getQuestion().getAttributeFields().get(attribName);
      JSONObject attribJson = new JSONObject();
      attribJson.put("name", attrib.getName());
      attribJson.put("displayName", attrib.getDisplayName());
      attribJson.put("help", attrib.getHelp());
      attribJson.put("align", attrib.getAlign());
      attribJson.put("isSortable", attrib.isSortable());
      attribJson.put("isRemovable", attrib.isRemovable());
      attribJson.put("type", attrib.getType());
      attributes.put(attribJson);
    }
    meta.put("attributes", attributes);
    JSONArray summaryAttributes = new JSONArray();
    for (String attrib : includedAttributes) {
      summaryAttributes.put(attrib);
    }
    meta.put("summaryAttributes", summaryAttributes);
    return meta;
  }

 
}
