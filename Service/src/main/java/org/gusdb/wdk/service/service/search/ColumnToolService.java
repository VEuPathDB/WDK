package org.gusdb.wdk.service.service.search;

import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.service.service.AbstractWdkService;

public abstract class ColumnToolService extends AbstractWdkService {

  /**
   * Constants shared by both column tool elements
   */
  public static final String
    COLUMN_TOOL_PATH_PARAM = "columnToolName",
    COLUMN_TOOL_PARAM_SEGMENT = "{" + COLUMN_TOOL_PATH_PARAM + "}";

  private final String _recordType;
  private final String _searchName;
  private final String _columnName;

  public ColumnToolService(String recordType, String searchName, String columnName) {
    _recordType = recordType;
    _searchName = searchName;
    _columnName = columnName;
  }

  protected Question getQuestion() {
    return getQuestionOrNotFound(_recordType, _searchName);
  }

  protected AttributeField getColumn() {
    return getColumnOrNotFound(getQuestion(), _columnName);
  }
}
