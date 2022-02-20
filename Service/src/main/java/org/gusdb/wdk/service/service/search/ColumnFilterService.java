package org.gusdb.wdk.service.service.search;

import static java.lang.String.format;

import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.service.service.QuestionService;
import org.gusdb.wdk.service.service.RecordService;
import org.json.JSONArray;

/**
 * Endpoints for getting info about or running column filters.
 */
@Path(ColumnFilterService.COLUMN_FILTERS_PATH)
public class ColumnFilterService extends ColumnToolService {

  /**
   * API Paths
   */
  public static final String COLUMN_FILTERS_PATH =
      ColumnService.NAMED_COLUMN_PATH + "/filters";

  /**
   * Filter not found message.
   */
  private static final String ERR_404 =
    "Column \"%s\" does not have a filter named \"%s\".";

  public ColumnFilterService(
    @PathParam(RecordService.RECORD_TYPE_PATH_PARAM) final String recordType,
    @PathParam(QuestionService.SEARCH_PATH_PARAM) final String searchName,
    @PathParam(ColumnService.COLUMN_PATH_PARAM) final String columnName
  ) {
    super(recordType, searchName, columnName);
  }

  /**
   * Builds a list of available filter names for the current column.
   *
   * @return a {@code JSONArray} containing the names of the available filters
   * for the current column
   */
  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public JSONArray getFilters() {
    return new JSONArray(getColumn().getColumnFilterNames());
  }

  /**
   * Retrieves and returns the input spec for the named filter on the current
   * column.
   *
   * @param filter
   *   name of the filter for which the input spec was requested.
   *
   * @return Input specification for what the named filter expects as config
   * input on run
   * @throws WdkModelException 
   */
  @GET
  @Path(COLUMN_TOOL_PARAM_SEGMENT)
  @Produces(MediaType.APPLICATION_JSON)
  public Object getFilterDetails(@PathParam(COLUMN_TOOL_PATH_PARAM) final String toolName) throws WdkModelException {
    AttributeField column = getColumn();
    if (!column.getColumnFilterNames().contains(toolName)) {
      throw new NotFoundException(format(ERR_404, getColumn().getName(), toolName));
    }
    return getWdkModel()
        .getColumnToolFactory()
        .getColumnFilterInstance(column, toolName)
        .getInputSpec();
  }

}
