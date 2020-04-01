package org.gusdb.wdk.model.report.reporter;

import java.util.Map;

import javax.ws.rs.core.MediaType;

import org.gusdb.fgputil.SortDirectionSpec;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.report.AbstractReporter;
import org.gusdb.wdk.model.report.Reporter;
import org.gusdb.wdk.model.report.ReporterConfigException;
import org.gusdb.wdk.model.report.config.AnswerDetails;
import org.gusdb.wdk.model.report.config.AnswerDetailsFactory;
import org.gusdb.wdk.model.user.UserPreferences;
import org.json.JSONObject;

public abstract class AnswerDetailsReporter extends AbstractReporter {

  protected Map<String,AttributeField> _attributes;
  protected Map<String,TableField> _tables;
  private ContentDisposition _contentDisposition;

  protected AnswerDetailsReporter(AnswerValue answerValue) {
    super(answerValue);
  }

  @Override
  public Reporter configure(JSONObject config) throws ReporterConfigException, WdkModelException {
    return configure(AnswerDetailsFactory.createFromJson(config, _baseAnswer.getAnswerSpec().getQuestion()));
  }

  private Reporter configure(AnswerDetails config) {
    _baseAnswer = getConfiguredAnswer(_baseAnswer, config);
    _attributes = config.getAttributes();
    _tables = config.getTables();
    _contentDisposition = config.getContentDisposition();
    return this;
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
