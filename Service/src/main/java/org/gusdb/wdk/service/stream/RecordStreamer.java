package org.gusdb.wdk.service.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.service.formatter.RecordFormatter;

public class RecordStreamer {

  public static StreamingOutput getRecordAsStream(RecordInstance recordInstance,
      List<String> attributeNames, List<String> tableNames) throws WdkModelException, WdkUserException {
    // FIXME: currently do not support real streaming; need to implement in AnswerValueBean
    final String result = RecordFormatter.getRecordJson(recordInstance, attributeNames, tableNames).toString();
    return new StreamingOutput() {
      @Override
      public void write(OutputStream stream) throws IOException, WebApplicationException {
        stream.write(result.getBytes("UTF-8"));
      }
    };
  }
}
