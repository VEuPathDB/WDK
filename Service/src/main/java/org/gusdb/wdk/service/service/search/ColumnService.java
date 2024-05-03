package org.gusdb.wdk.service.service.search;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;

import org.gusdb.wdk.service.formatter.AttributeFieldFormatter;
import org.gusdb.wdk.service.service.AbstractWdkService;
import org.gusdb.wdk.service.service.QuestionService;
import org.gusdb.wdk.service.service.RecordService;
import org.json.JSONArray;
import org.json.JSONObject;

@Path(ColumnService.COLUMNS_PATH)
public class ColumnService extends AbstractWdkService {

  /**
   * Define the path segments, paths, and parameters for column-based information and tooling
   */
  public static final String
    COLUMNS_SEGMENT = "columns",
    COLUMN_PATH_PARAM = "columnName",
    COLUMN_PARAM_SEGMENT = "{" + COLUMN_PATH_PARAM + "}",
    NAMED_COLUMN_SEGMENT_PAIR = "/" + COLUMNS_SEGMENT + "/" + COLUMN_PARAM_SEGMENT,
    COLUMNS_PATH = QuestionService.NAMED_SEARCH_PATH + "/" + COLUMNS_SEGMENT,
    NAMED_COLUMN_PATH = QuestionService.NAMED_SEARCH_PATH + NAMED_COLUMN_SEGMENT_PAIR;

  private final String _recordType;
  private final String _searchName;

  public ColumnService(
    @PathParam(RecordService.RECORD_TYPE_PATH_PARAM) final String recordType,
    @PathParam(QuestionService.SEARCH_PATH_PARAM) final String searchName
  ) {
    _recordType = recordType;
    _searchName = searchName;
  }

  /**
   * Returns the available columns for this search in regular (array of names) or expanded format
   *
   * @param format "expanded" or other
   * @return available columns for a search
   */
  @GET
  @Produces(APPLICATION_JSON)
  public JSONArray getColumns(
    @QueryParam("format") final String format
  ) {
    return AttributeFieldFormatter.getAttributesJson(
      getQuestionOrNotFound(_recordType, _searchName).getAttributeFieldMap().values(),
      "expanded".equals(format)
    );
  }

  /**
   * Returns a specifc attribute in expanded format
   *
   * @return information about a single column
   */
  @GET
  @Path(COLUMN_PARAM_SEGMENT)
  @Produces(APPLICATION_JSON)
  public JSONObject getColumn(@PathParam(COLUMN_PATH_PARAM) final String col) {
    return AttributeFieldFormatter.getAttributeJson(
        getColumnOrNotFound(getQuestionOrNotFound(_recordType, _searchName), col));
  }
}
