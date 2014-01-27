package org.gusdb.wdk.controller.actionutil;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.upload.FormFile;
import org.gusdb.wdk.controller.form.QuestionForm;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.RequestParams;

public class QuestionRequestParams implements RequestParams {

  private final HttpServletRequest request;
  private final QuestionForm questionForm;

  public QuestionRequestParams(HttpServletRequest request, QuestionForm questionForm) {
    this.request = request;
    this.questionForm = questionForm;
  }

  @Override
  public String getParam(String name) {
    Object value = questionForm.getValue(name);
    if (value != null) {
      if (value instanceof FormFile) {
        FormFile file = (FormFile) value;
        return file.getFileName();
      }
      else {
        return value.toString();
      }
    }
    else {
      return request.getParameter(name);
    }
  }

  @Override
  public String[] getArray(String name) {
    return questionForm.getArray(name);
  }

  @Override
  public String getUploadFileContent(String name) throws WdkModelException {
    Object value = questionForm.getValue(name);
    if (value != null && value instanceof FormFile) {
      FormFile file = (FormFile) value;
      try {
        return new String(file.getFileData());
      }
      catch (IOException ex) {
        throw new WdkModelException(ex);
      }
    }
    else
      return null;
  }
}
