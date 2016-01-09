package org.gusdb.wdk.service.stream;

import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.jspwrap.AnswerValueBean;
import org.gusdb.wdk.model.report.Reporter;
import org.gusdb.wdk.service.formatter.AnswerFormatter;
import org.gusdb.wdk.service.request.answer.AnswerRequestSpecifics;
import org.json.JSONException;
import org.json.JSONObject;

public class AnswerStreamer {

  private static final Logger LOG = Logger.getLogger(AnswerStreamer.class);

  public static StreamingOutput getAnswerAsStream(AnswerValueBean answerValue,
      AnswerRequestSpecifics specifics) throws WdkModelException {
    // FIXME: currently do not support real streaming; need to implement in AnswerValueBean
    final JSONObject resultJson = AnswerFormatter.formatAnswer(answerValue, specifics);
    return new StreamingOutput() {
      @Override
      public void write(OutputStream stream) throws IOException, WebApplicationException {
        LOG.debug("Returning the following answer: " + resultJson.toString(2));
        stream.write(resultJson.toString().getBytes("UTF-8"));
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
