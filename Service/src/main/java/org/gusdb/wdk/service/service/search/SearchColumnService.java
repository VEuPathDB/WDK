package org.gusdb.wdk.service.service.search;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.FieldScope;
import org.gusdb.wdk.service.formatter.AttributeFieldFormatter;
import org.gusdb.wdk.service.service.AbstractWdkService;
import org.gusdb.wdk.service.service.QuestionService;
import org.gusdb.wdk.service.service.RecordService;
import org.json.JSONArray;
import org.json.JSONObject;

@Path(SearchColumnService.COLUMNS_PATH)
public class SearchColumnService extends AbstractWdkService {

  public static final String
    COLUMNS_SEGMENT = "columns",
    COLUMN_PATH_PARAM = "columnName",
    COLUMN_PARAM_SEGMENT = "{" + COLUMN_PATH_PARAM + "}",
    NAMED_COLUMN_SEGMENT_PAIR = "/" + COLUMNS_SEGMENT + "/" + COLUMN_PARAM_SEGMENT,
    COLUMNS_PATH = QuestionService.NAMED_SEARCH_PATH + "/" + COLUMNS_SEGMENT,
    NAMED_COLUMN_PATH = QuestionService.NAMED_SEARCH_PATH + NAMED_COLUMN_SEGMENT_PAIR;

  private final Question search;

  public SearchColumnService(
    @PathParam(RecordService.RECORD_TYPE_PATH_PARAM) final String recordType,
    @PathParam(QuestionService.SEARCH_PATH_PARAM) final String searchType
  ) {
    this.search = getQuestionOrNotFound(recordType, searchType);
  }

  @GET
  @Produces(APPLICATION_JSON)
  public JSONArray getColumns(
    @QueryParam("format") final String format
  ) {
    return AttributeFieldFormatter.getAttributesJson(
      this.search.getAttributeFieldMap().values(),
      FieldScope.ALL,
      "expanded".equals(format)
    );
  }

  @GET
  @Path(COLUMN_PARAM_SEGMENT)
  @Produces(APPLICATION_JSON)
  public JSONObject getColumn(@PathParam(COLUMN_PATH_PARAM) final String col) {
    return AttributeFieldFormatter.getAttributeJson(requireColumn(search, col));
  }
}
