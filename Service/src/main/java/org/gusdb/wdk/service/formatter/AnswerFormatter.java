package org.gusdb.wdk.service.formatter;

import java.util.Map;
import java.util.Map.Entry;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;
import org.gusdb.wdk.model.jspwrap.AttributeFieldBean;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.TableValue;
import org.gusdb.wdk.model.record.attribute.AttributeValue;
import org.gusdb.wdk.model.record.attribute.LinkAttributeValue;
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
  records: [ see record formmatter
    ]
  } ]
}
*/
public class AnswerFormatter {

  public static JSONObject formatAnswer(AnswerValueBean answerValue) throws WdkModelException {
    try {
      JSONObject parent = new JSONObject();
      parent.put("meta", getMetaData(answerValue));
      JSONArray records = new JSONArray();
      for (RecordInstance record : answerValue.getAnswerValue().getRecordInstances()) {
        records.put(RecordFormatter.getRecordJson(record));
      }
      parent.put("records", records);
      return parent;
    }
    catch (WdkUserException e) {
      // should already have validated any user input
      throw new WdkModelException("Internal validation failure", e);
    }
  }

  public static JSONObject getMetaData(AnswerValueBean answerValue)
      throws WdkModelException, WdkUserException {
    JSONObject meta = new JSONObject();
    meta.put("count", answerValue.getResultSize());
    meta.put("class", answerValue.getRecordClass().getFullName());
    JSONArray attributes = new JSONArray();
    for (AttributeFieldBean attrib : answerValue.getDisplayableAttributes()) {
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
    for (String attrib : answerValue.getSummaryAttributeNames()) {
      summaryAttributes.put(attrib);
    }
    meta.put("summaryAttributes", summaryAttributes);
    return meta;
  }

 
}
