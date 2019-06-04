package org.gusdb.wdk.service.service.search;

import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.service.service.AbstractWdkService;
import org.gusdb.wdk.service.service.QuestionService;
import org.gusdb.wdk.service.service.RecordService;
import org.json.JSONArray;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.util.function.Supplier;

import static java.lang.String.format;

@Path(ColumnFilterService.BASE_PATH)
public class ColumnFilterService extends AbstractWdkService {

  /**
   * API Paths
   */
  public static final String
    ID_VAR = "filter",
    ID_PARAM = "{" + ID_VAR + "}",
    BASE_PATH = SearchColumnService.ID_PATH + "/filters";

  /**
   * Reporter not found message.
   */
  private static final String ERR_404 =
    "Column \"%s\" does not have a filter named \"%s\".";

  private final Question search;
  private final AttributeField column;

  public ColumnFilterService(
    @PathParam(RecordService.ID_VAR) final String recordType,
    @PathParam(QuestionService.ID_VAR) final String searchType,
    @PathParam(SearchColumnService.ID_VAR) final String columnName,
    @Context ServletContext ctx
  ) {
    setServletContext(ctx);
    this.search = getQuestionOrNotFound(recordType, searchType);
    this.column = requireColumn(this.search, columnName);
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
    return new JSONArray(this.column.getColumnFilterNames());
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
   */
  @GET
  @Path(ID_PARAM)
  @Produces(MediaType.APPLICATION_JSON)
  public Object getFilterDetails(@PathParam(ID_VAR) final String filter) {
    return column.getFilter(filter)
      .orElseThrow(makeNotFound(filter))
      .inputSpec();
  }

  private Supplier<NotFoundException> makeNotFound(final String name) {
    return () -> new NotFoundException(format(ERR_404, column.getName(), name));
  }
}
