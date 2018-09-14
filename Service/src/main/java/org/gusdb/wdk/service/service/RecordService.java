package org.gusdb.wdk.service.service;

import static org.gusdb.fgputil.FormatUtil.NL;
import static org.gusdb.fgputil.FormatUtil.join;
import static org.gusdb.fgputil.functional.Functions.mapToList;

import java.util.List;
import java.util.Map;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
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
import org.gusdb.wdk.model.record.DynamicRecordInstance;
import org.gusdb.wdk.model.record.FieldScope;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.RecordNotFoundException;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.service.formatter.AttributeFieldFormatter;
import org.gusdb.wdk.service.formatter.JsonKeys;
import org.gusdb.wdk.service.formatter.RecordClassFormatter;
import org.gusdb.wdk.service.formatter.RecordFormatter;
import org.gusdb.wdk.service.formatter.TableFieldFormatter;
import org.gusdb.wdk.service.request.RecordRequest;
import org.gusdb.wdk.service.request.exception.DataValidationException;
import org.gusdb.wdk.service.request.exception.RequestMisformatException;
import org.gusdb.wdk.service.statustype.MultipleChoicesStatusType;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/records")
public class RecordService extends AbstractWdkService {

  private static final Logger LOG = Logger.getLogger(RecordService.class);

  private static final String RECORDCLASS_RESOURCE = "RecordClass with name ";
  private static final String TABLE_RESOURCE = "Table with name ";

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getRecordClassList(
      @QueryParam("expandRecordClasses") Boolean expandRecordClasses,
      @QueryParam("expandAttributes") Boolean expandAttributes,
      @QueryParam("expandTables") Boolean expandTables,
      @QueryParam("expandTableAttributes") Boolean expandTableAttributes) {
    return Response.ok(
        RecordClassFormatter.getRecordClassesJson(
            getWdkModel().getAllRecordClassSets(), getFlag(expandRecordClasses),
            getFlag(expandAttributes), getFlag(expandTables), getFlag(expandTableAttributes)).toString()
    ).build();
  }

  @GET
  @Path("{recordClassName}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getRecordClassInfo(
      @PathParam("recordClassName") String recordClassName,
      @QueryParam("expandAttributes") Boolean expandAttributes,
      @QueryParam("expandTables") Boolean expandTables,
      @QueryParam("expandTableAttributes") Boolean expandTableAttributes) {
    return Response.ok(
        RecordClassFormatter.getRecordClassJson(
            getRecordClassOrNotFound(recordClassName, getWdkModel()), getFlag(expandAttributes),
            getFlag(expandTables), getFlag(expandTableAttributes)).toString()
    ).build();
  }

  @GET
  @Path("{recordClassName}/attributes")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAttributesInfo(
      @PathParam("recordClassName") String recordClassName,
      @QueryParam("expandAttributes") Boolean expandAttributes) {
    return Response.ok(
        AttributeFieldFormatter.getAttributesJson(
            getRecordClassOrNotFound(recordClassName, getWdkModel()).getAttributeFieldMap().values(),
            FieldScope.ALL, getFlag(expandAttributes)).toString()
    ).build();
  }

  @GET
  @Path("{recordClassName}/tables")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getTablesInfo(
      @PathParam("recordClassName") String recordClassName,
      @QueryParam("expandTables") Boolean expandTables,
      @QueryParam("expandTableAttributes") Boolean expandTableAttributes) {
    return Response.ok(
        TableFieldFormatter.getTablesJson(
            getRecordClassOrNotFound(recordClassName, getWdkModel()).getTableFieldMap().values(),
            FieldScope.ALL, getFlag(expandTables), getFlag(expandTableAttributes)).toString()
    ).build();
  }

  @GET
  @Path("{recordClassName}/tables/{tableName}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getTableInfo(
      @PathParam("recordClassName") String recordClassName,
      @PathParam("tableName") String tableName,
      @QueryParam("expandTableAttributes") Boolean expandTableAttributes) {
    return getTableResponse(recordClassName, tableName, expandTableAttributes, false);
  }

  @GET
  @Path("{recordClassName}/tables/{tableName}/attributes")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getTableAttributesInfo(
      @PathParam("recordClassName") String recordClassName,
      @PathParam("tableName") String tableName,
      @QueryParam("expandTableAttributes") Boolean expandTableAttributes) {
    return getTableResponse(recordClassName, tableName, expandTableAttributes, true);
  }

  @GET
  @Path("{recordClassName}/answer-format")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAnswerFormats(@PathParam("recordClassName") String recordClassName) {
    return Response.ok(
        RecordClassFormatter.getAnswerFormatsJson(
            getRecordClassOrNotFound(recordClassName, getWdkModel()).getReporterMap().values(),
            FieldScope.ALL).toString()
    ).build();
  }

  @GET
  @Path("{recordClassName}/count")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getRecordCount(@PathParam("recordClassName") String recordClassName) throws WdkModelException {
    RecordClass recordClass = getRecordClassOrNotFound(recordClassName, getWdkModel());
    if (!recordClass.hasAllRecordsQuery()) {
      throw new NotFoundException(formatNotFound(RECORDCLASS_RESOURCE + recordClassName + "/count"));
    }
    long count = recordClass.getAllRecordsCount(getSessionUser());
    JSONObject json = new JSONObject().put(JsonKeys.TOTAL_COUNT, count);
    return Response.ok(json.toString()).build();
  }

  @POST
  @Path("{recordClassName}/instance")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response buildResult(@PathParam("recordClassName") String recordClassName, String body)
      throws WdkModelException, DataValidationException {
    RecordInstance recordInstance = null;
    try {
      // get and parse request information
      RecordClass recordClass = getRecordClassOrNotFound(recordClassName, getWdkModel());
      JSONObject requestJson = new JSONObject(body);
      RecordRequest request = RecordRequest.createFromJson(recordClass, requestJson);
      
      // check to see if PKs specified map to multiple records
      try {
        List<Map<String,Object>> ids = recordClass.lookupPrimaryKeys(getSessionUser(), request.getPrimaryKey().getRawValues());
        if (ids.size() > 1) {
          // more than one record found; return multi-choice status and give user IDs from which to choose
          String idOptionText = join(mapToList(ids, pkValueMap -> join(mapToList(pkValueMap.entrySet(),
              entry -> entry.getKey() + " = " + entry.getValue()), ", ")), NL);
          return Response.status(
              new MultipleChoicesStatusType()).type(MediaType.TEXT_PLAIN).entity(idOptionText).build();
        }
      }
      catch(RecordNotFoundException rnfe) {
        throw new NotFoundException(rnfe);
      }
      catch(WdkUserException e) {
        throw new BadRequestException(e);
      }

      // PKs represent only one record; fetch, format, and return
      recordInstance = new DynamicRecordInstance(getSessionUser(), request.getRecordClass(), request.getPrimaryKey().getRawValues());
      TwoTuple<JSONObject,List<Exception>> recordJsonResult = RecordFormatter.getRecordJson(
          recordInstance, request.getAttributeNames(), request.getTableNames());
      triggerErrorEvents(recordJsonResult.getSecond());
      return Response.ok(recordJsonResult.getFirst().toString()).build();
    }
    catch (JSONException | RequestMisformatException e) {
      LOG.warn("Passed request body deemed unacceptable", e);
      throw new BadRequestException(e);
    }
    catch (WdkUserException | RecordNotFoundException e) {
      // these may be thrown when the PK values either don't exist or map to >1 record
      // OR the ID exists but the attribute query returns nothing
      String primaryKeys = (recordInstance == null ? "<unknown>" : recordInstance.getPrimaryKey().getValuesAsString());
      throw new NotFoundException(AbstractWdkService.formatNotFound(RECORDCLASS_RESOURCE +
          recordClassName + ", " + String.format("with primary key [%s]", primaryKeys)) ,e);
    }
  }

  private Response getTableResponse(String recordClassName, String tableName,
      Boolean expandTableAttributes, boolean attributesOnly) {
    RecordClass rc = getRecordClassOrNotFound(recordClassName, getWdkModel());
    TableField table = rc.getTableFieldMap().get(tableName);
    boolean expandAttributes = getFlag(expandTableAttributes);
    if (table == null) {
      throw new NotFoundException(formatNotFound(RECORDCLASS_RESOURCE + recordClassName + ", " + TABLE_RESOURCE + tableName));
    }
    return Response.ok((attributesOnly ?
        AttributeFieldFormatter.getAttributesJson(
            table.getAttributeFieldMap(FieldScope.ALL).values(), FieldScope.ALL, expandAttributes) :
        TableFieldFormatter.getTableJson(table, expandAttributes)
    ).toString()).build();
  }

  public static RecordClass getRecordClassOrNotFound(String recordClassName, WdkModel model) {
    RecordClass rc = model.getRecordClassByUrlSegment(recordClassName);
    if (rc == null) {
      throw new NotFoundException(formatNotFound(RECORDCLASS_RESOURCE + recordClassName));
    }
    return rc;
  }
}
