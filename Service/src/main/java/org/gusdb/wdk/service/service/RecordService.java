package org.gusdb.wdk.service.service;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.report.config.AnswerDetails.AttributeFormat;
import org.gusdb.wdk.model.report.util.RecordFormatter;
import org.gusdb.wdk.service.annotation.OutSchema;
import org.gusdb.wdk.service.formatter.RecordClassFormatter;
import org.gusdb.wdk.service.request.RecordRequest;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.gusdb.wdk.service.statustype.MultipleChoicesStatusType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.prometheus.client.Counter;

@Path(RecordService.RECORD_TYPES_PATH)
public class RecordService extends AbstractWdkService {

  public static final String RECORD_TYPES_PATH = "/record-types";
  public static final String RECORD_TYPE_PATH_PARAM = "recordClassUrlSegment";
  public static final String RECORD_TYPE_PARAM_SEGMENT = "{" + RECORD_TYPE_PATH_PARAM + "}";
  public static final String NAMED_RECORD_TYPE_SEGMENT_PAIR = RECORD_TYPES_PATH + "/" + RECORD_TYPE_PARAM_SEGMENT;

  @SuppressWarnings("unused")
  private static final Logger LOG = Logger.getLogger(RecordService.class);

  private static final String RECORDCLASS_RESOURCE = "RecordClass with name ";

  private static final Counter TABLE_REQUEST_COUNTER = Counter.build()
      .name("wdk_table_requests")
      .help("Times individual tables are requested at the /records endpoint")
      .labelNames("table")
      .register();

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  @OutSchema("wdk.records.get")
  public Response getRecordClassList(@QueryParam("format") String format) {
    WdkModel wdkModel = getWdkModel();
    return Response.ok(
      isExpandedFormat(format, false)
        ?
          // stream expanded format, which may be large (subclasses may override to cache, etc.)
          getStreamingOutput(getExpandedRecordClassesJsonStream(wdkModel))
        :
          // build smaller JSON directly and use string as entity
          RecordClassFormatter.getRecordClassNamesJson(wdkModel.getAllRecordClasses()).toString()).build();
  }

  protected InputStream getExpandedRecordClassesJsonStream(WdkModel wdkModel) {
    JSONArray allRecordClassesJson = RecordClassFormatter.getExpandedRecordClassesJson(wdkModel.getAllRecordClasses(), wdkModel.getRecordClassQuestionMap());
    return new ByteArrayInputStream(allRecordClassesJson.toString().getBytes());
  }

  @GET
  @Path(RECORD_TYPE_PARAM_SEGMENT)
  @Produces(MediaType.APPLICATION_JSON)
  @OutSchema("wdk.records.name.get")
  public JSONObject getRecordClassInfo(
      @PathParam(RECORD_TYPE_PATH_PARAM) String recordClassName,
      @QueryParam("format") String format) {
    RecordClass rc = getRecordClassOrNotFound(recordClassName);
    return isExpandedFormat(format, true) ?
        RecordClassFormatter.getExpandedRecordClassJson(rc, getWdkModel().getRecordClassQuestionMap()) :
        RecordClassFormatter.getRecordClassJson(rc, true, true, true);
  }

  private static boolean isExpandedFormat(String format, boolean defaultValue) {
    return Optional.ofNullable(format)
        .map(f -> f.equals("expanded"))
        .orElse(defaultValue);
  }

  // TODO: replace this with a GET (using the path to encode the primary key)
  @POST
  @Path(RECORD_TYPE_PARAM_SEGMENT + "/records")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response buildResult(@PathParam(RECORD_TYPE_PATH_PARAM) String recordClassName, String body)
      throws WdkModelException, DataValidationException, RequestMisformatException {
    try {
      // get and parse request information
      RecordClass recordClass = getRecordClassOrNotFound(recordClassName);
      RecordRequest request = RecordRequest.createFromJson(recordClass, new JSONObject(body));

      // fetch a list of record instances the passed primary key maps to
      List<RecordInstance> records = RecordClass.getRecordInstances(getSessionUser(), request.getPrimaryKey());

      // if no mapping exists, return not found
      if (records.isEmpty()) {
        throw new NotFoundException(formatNotFound(RECORDCLASS_RESOURCE + recordClassName +
            " has no record with primary keys: " + request.getPrimaryKey().getValuesAsString()));
      }

      // if PK specified maps to multiple records, return Multiple Choices response with IDs from which to choose
      else if (records.size() > 1) {
        String idOptionText = records.stream()
            .map(record -> record.getPrimaryKey().getRawValues().entrySet().stream()
                .map(entry -> entry.getKey() + " = " + entry.getValue())
                .collect(Collectors.joining(", ")))
            .collect(Collectors.joining(NL));
        return Response.status(new MultipleChoicesStatusType())
            .type(MediaType.TEXT_PLAIN).entity(idOptionText).build();
      }

      // PK represents only one record; fetch, format, and return
      else {

        // update metrics; increment entries for requested tables
        request.getTableNames().stream()
            .map(tableName -> recordClass.getUrlSegment() + "." + tableName)
            .forEach(key -> TABLE_REQUEST_COUNTER.labels(key).inc());

        TwoTuple<JSONObject,List<Exception>> recordJsonResult = RecordFormatter.getRecordJson(
            records.get(0), request.getAttributeNames(), request.getTableNames(), AttributeFormat.DISPLAY);
        triggerErrorEvents(recordJsonResult.getSecond());
        return Response.ok(recordJsonResult.getFirst().toString()).build();
      }
    }
    catch (JSONException e) {
      throw new RequestMisformatException("Passed request body deemed unacceptable. " + e.getMessage(), e);
    }
    catch (WdkUserException e) {
      // due to checks above during request parsing, this should not happen
      throw new WdkModelException(e);
    }
  }
}
