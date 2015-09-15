package org.gusdb.wdk.service.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;
import org.gusdb.wdk.model.report.Reporter;
import org.gusdb.wdk.service.formatter.AnswerFormatter;
import org.json.JSONException;

public class AnswerStreamer {

  public static StreamingOutput getAnswerAsStream(AnswerValueBean answerValue) throws WdkModelException {
    // FIXME: currently do not support real streaming; need to implement in AnswerValueBean
    final String result = AnswerFormatter.formatAnswer(answerValue).toString();
    return new StreamingOutput() {
      @Override
      public void write(OutputStream stream) throws IOException, WebApplicationException {
        stream.write(result.getBytes("UTF-8"));
      }
    };
  }
  
  public static StreamingOutput getAnswerAsStream(final Reporter reporter)  {
    // FIXME: currently do not support real streaming; need to implement in AnswerValueBean
    return new StreamingOutput() {
      @Override
      public void write(OutputStream stream) throws IOException, WebApplicationException {
        try {
          reporter.write(stream);
        }
        catch (WdkModelException | NoSuchAlgorithmException | JSONException | WdkUserException
            | SQLException e) {
          throw new WebApplicationException(e);
        }
      }
    };
  }

}
