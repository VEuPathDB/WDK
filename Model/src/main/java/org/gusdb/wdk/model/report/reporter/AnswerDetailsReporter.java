package org.gusdb.wdk.model.report.reporter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;

import org.gusdb.fgputil.SortDirectionSpec;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Procedure;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.report.AbstractReporter;
import org.gusdb.wdk.model.report.Reporter;
import org.gusdb.wdk.model.report.ReporterConfigException;
import org.gusdb.wdk.model.report.config.AnswerDetails;
import org.gusdb.wdk.model.report.config.AnswerDetails.AttributeFormat;
import org.gusdb.wdk.model.report.config.AnswerDetailsFactory;
import org.gusdb.wdk.model.user.UserPreferences;
import org.json.JSONObject;

public abstract class AnswerDetailsReporter extends AbstractReporter {

  private static final long MAX_BUFFERED_RESPONSE_SIZE = 50 /* megabytes */ * (1024 * 1024) /* bytes per megabyte */;

  protected abstract void writeResponseBody(OutputStream out, Procedure checkResponseSize) throws WdkModelException;

  protected Map<String,AttributeField> _attributes;
  protected Map<String,TableField> _tables;
  private ContentDisposition _contentDisposition;
  protected AttributeFormat _attributeFormat;
  private boolean _isBufferEntireResponse;

  // entire buffered response; will only be populated if isBufferEntireResponse = true
  private ByteArrayOutputStream _bufferedResponse = new ByteArrayOutputStream();

  @Override
  public Reporter configure(JSONObject config) throws ReporterConfigException, WdkModelException {
    return configure(AnswerDetailsFactory.createFromJson(config, _baseAnswer.getAnswerSpec().getQuestion()));
  }

  protected Reporter configure(AnswerDetails config) throws WdkModelException {
    _baseAnswer = getConfiguredAnswer(_baseAnswer, config);
    _attributes = config.getAttributes();
    _tables = config.getTables();
    _contentDisposition = config.getContentDisposition();
    _attributeFormat = config.getAttributeFormat();
    _isBufferEntireResponse = config.isBufferEntireResponse();

    // if asked to buffer entire response up front (rather than stream), need to load
    //   the data, telling the writer to check occasionally whether the response is too big
    if (_isBufferEntireResponse) {
      writeResponseBody(_bufferedResponse, () -> {
        // check buffer size against max
        if (_bufferedResponse.size() > MAX_BUFFERED_RESPONSE_SIZE) {
          throw new BadRequestException("Response is too large to buffer.  Specify a smaller page or set 'bufferEntireResponse' to false.");
        }
      });
    }
    return this;
  }

  @Override
  protected final void write(OutputStream out) throws WdkModelException {
    try {
      // if entire response is already buffered, write it out
      if (_isBufferEntireResponse) {
        _bufferedResponse.writeTo(out);
      }
      else {
        writeResponseBody(out, () -> {});
      }
    }
    catch (IOException e) {
      throw new WdkModelException("Could not write buffered response", e);
    }
  }

  private static AnswerValue getConfiguredAnswer(AnswerValue answerValue, AnswerDetails config) {
    int startIndex = config.getOffset() + 1;
    int endIndex = config.getNumRecords() == AnswerDetails.ALL_RECORDS? 
        AnswerValue.UNBOUNDED_END_PAGE_INDEX : startIndex + config.getNumRecords() - 1;
    
    AnswerValue configuredAnswer = answerValue.cloneWithNewPaging(startIndex, endIndex);
    Map<String, Boolean> sorting = SortDirectionSpec.convertSorting(
        config.getSorting(), UserPreferences.MAX_NUM_SORTING_COLUMNS);
    configuredAnswer.setSortingMap(sorting);
    return configuredAnswer;
  }

  @Override
  public String getHttpContentType() {
    return MediaType.APPLICATION_JSON;
  }

  @Override
  public ContentDisposition getContentDisposition() {
    return _contentDisposition;
  }
}
