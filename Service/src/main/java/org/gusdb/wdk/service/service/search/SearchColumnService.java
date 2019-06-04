package org.gusdb.wdk.service.service.search;

import org.gusdb.wdk.model.question.Question;
import org.gusdb.wdk.model.record.FieldScope;
import org.gusdb.wdk.service.formatter.AttributeFieldFormatter;
import org.gusdb.wdk.service.service.AbstractWdkService;
import org.gusdb.wdk.service.service.QuestionService;
import org.gusdb.wdk.service.service.RecordService;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.servlet.ServletContext;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@Path(SearchColumnService.BASE_PATH)
public class SearchColumnService extends AbstractWdkService {

  public static final String
    ID_VAR = "columnName",
    ID_PARAM = "{" + ID_VAR + "}",
    BASE_PATH = QuestionService.ID_PATH + "/columns",
    ID_PATH = BASE_PATH + "/" + ID_PARAM;

  private final Question search;

  public SearchColumnService(
    @PathParam(RecordService.ID_VAR) final String recordType,
    @PathParam(QuestionService.ID_VAR) final String searchType,
    @Context ServletContext ctx
  ) {
    setServletContext(ctx);
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
  @Path(ID_PARAM)
  @Produces(APPLICATION_JSON)
  public JSONObject getColumn(@PathParam(ID_VAR) final String col) {
    return AttributeFieldFormatter.getAttributeJson(requireColumn(search, col));
  }
}
