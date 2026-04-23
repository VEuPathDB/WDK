package org.gusdb.wdk.service.service;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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

  private static final Logger LOG = Logger.getLogger(RecordService.class);

  private static final String RECORDCLASS_RESOURCE = "RecordClass with name ";

  private static final String EXPANDED_RECORD_CLASSES_CACHE_FILE = "expanded-record-classes.json";

  private static final Counter TABLE_REQUEST_COUNTER = Counter.build()
      .name("wdk_table_requests")
      .help("Times individual tables are requested at the /records endpoint")
      .labelNames("project_id", "table", "user_registration_status")
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
    try {
      Path cacheFile = getExpandedRecordClassesCacheFile(wdkModel);

      if (Files.exists(cacheFile)) {
        LOG.debug("Serving expanded record classes from cache file: " + cacheFile);
        return new FileInputStream(cacheFile.toFile());
      } else {
        LOG.warn("Cache file does not exist at: " + cacheFile + ". Falling back to in-memory generation.");
        // Fallback to in-memory generation if cache doesn't exist
        JSONArray allRecordClassesJson = RecordClassFormatter.getExpandedRecordClassesJson(
            wdkModel.getAllRecordClasses(), wdkModel.getRecordClassQuestionMap());
        return new ByteArrayInputStream(allRecordClassesJson.toString().getBytes());
      }
    } catch (IOException e) {
      LOG.error("Failed to read cache file, falling back to in-memory generation", e);
      // Fallback to in-memory generation if cache read fails
      JSONArray allRecordClassesJson = RecordClassFormatter.getExpandedRecordClassesJson(
          wdkModel.getAllRecordClasses(), wdkModel.getRecordClassQuestionMap());
      return new ByteArrayInputStream(allRecordClassesJson.toString().getBytes());
    }
  }

  /**
   * Gets the path to the expanded record classes cache file.
   */
  public static Path getExpandedRecordClassesCacheFile(WdkModel wdkModel) {
    Path wdkTempDir = wdkModel.getModelConfig().getWdkTempDir();
    return Paths.get(wdkTempDir.toString(), EXPANDED_RECORD_CLASSES_CACHE_FILE);
  }

  /**
   * Generates the expanded record classes cache file.
   * This should be called during application initialization.
   */
  public static void generateExpandedRecordClassesCache(WdkModel wdkModel) throws IOException {
    Path cacheFile = getExpandedRecordClassesCacheFile(wdkModel);

    LOG.info("Generating expanded record classes cache file at: " + cacheFile);

    // Generate JSON
    JSONArray allRecordClassesJson = RecordClassFormatter.getExpandedRecordClassesJson(
        wdkModel.getAllRecordClasses(), wdkModel.getRecordClassQuestionMap());

    // Write to file
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(cacheFile.toFile()))) {
      writer.write(allRecordClassesJson.toString());
    }

    LOG.info("Cache file generation complete. File size: " + Files.size(cacheFile) + " bytes");
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
      List<RecordInstance> records = RecordClass.getRecordInstances(getRequestingUser(), request.getPrimaryKey());

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
            .forEach(key -> TABLE_REQUEST_COUNTER.labels(getWdkModel().getProjectId(), key, getRequestingUser().getRegistrationStatus()).inc());

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
