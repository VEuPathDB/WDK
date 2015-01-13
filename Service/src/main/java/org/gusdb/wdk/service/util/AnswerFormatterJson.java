package org.gusdb.wdk.service.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map.Entry;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;
import org.gusdb.wdk.model.jspwrap.AttributeFieldBean;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.attribute.AttributeValue;
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
  records: [ {
    id: String,
    attributes: [
     { name: String, value: Any }
    ],
    tables: [
      { 
        name: String,
        value: [ { name: String, value: Any } ]
      }
    ]
  } ]
}
*/
public class AnswerFormatterJson implements AnswerFormatter<JSONObject> {

  @Override
  public JSONObject formatAnswer(AnswerValueBean answerValue) throws WdkModelException {
    try {
      JSONObject parent = new JSONObject();
      parent.put("meta", getMetaData(answerValue));
      JSONArray records = new JSONArray();
      for (RecordInstance record : answerValue.getAnswerValue().getRecordInstances()) {
        records.put(getRecordJson(record));
      }
      parent.put("records", records);
      return parent;
    }
    catch (WdkUserException e) {
      // should already have validated any user input
      throw new WdkModelException("Internal validation failure", e);
    }
  }

  private JSONObject getMetaData(AnswerValueBean answerValue)
      throws WdkModelException, WdkUserException {
    JSONObject meta = new JSONObject();
    meta.put("count", answerValue.getResultSize());
    meta.put("class", answerValue.getRecordClass().getName());
    JSONArray attributes = new JSONArray();
    for (AttributeFieldBean attrib : answerValue.getDisplayableAttributes()) {
      JSONObject attribJson = new JSONObject();
      attribJson.put("name", attrib.getName());
      attribJson.put("displayName", attrib.getDisplayName());
      attributes.put(attribJson);
    }
    meta.put("attributes", attributes);
    return meta;
  }

  private JSONObject getRecordJson(RecordInstance record)
      throws WdkModelException, WdkUserException {
    JSONObject json = new JSONObject();
    json.put("id", record.getPrimaryKey().getValue());
    JSONArray attributes = new JSONArray();
    for (Entry<String,AttributeValue> attrib : record.getAttributeValueMap().entrySet()) {
      JSONObject attribJson = new JSONObject();
      attribJson.put("name", attrib.getKey());
      attribJson.put("value", attrib.getValue().getValue());
      attributes.put(attribJson);
    }
    json.put("attributes", attributes);
    JSONArray tables = new JSONArray();
    // FIXME: tables not yet supported
    json.put("tables", tables);
    return json;
  }

  @Override
  public String getAnswerAsString(AnswerValueBean answerValue) throws WdkModelException {
    return formatAnswer(answerValue).toString();
  }

  @Override
  public StreamingOutput getAnswerAsStream(AnswerValueBean answerValue) throws WdkModelException {
    // currently do not support real streaming; need to implement in AnswerValueBean
    final String result = getAnswerAsString(answerValue);
    return new StreamingOutput() {
      @Override
      public void write(OutputStream stream) throws IOException, WebApplicationException {
        stream.write(result.getBytes());
      }
    };
  }

}
