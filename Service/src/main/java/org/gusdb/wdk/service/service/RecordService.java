package org.gusdb.wdk.service.service;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordInstance;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.model.user.User;
import org.gusdb.wdk.service.formatter.RecordClassFormatter;
import org.gusdb.wdk.service.request.RecordRequest;
import org.gusdb.wdk.service.request.RequestMisformatException;
import org.gusdb.wdk.service.stream.RecordStreamer;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/record")
public class RecordService extends WdkService {

  private static final Logger LOG = Logger.getLogger(RecordService.class);

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getRecordClassList(
      @QueryParam("expandRecordClasses") Boolean expandRecordClasses,
      @QueryParam("expandAttributes") Boolean expandAttributes,
      @QueryParam("expandTables") Boolean expandTables,
      @QueryParam("expandTableAttributes") Boolean expandTableAttributes) {
    return Response.ok(
        RecordClassFormatter.getRecordClassesJson(getWdkModel().getAllRecordClassSets(),
            getFlag(expandRecordClasses), getFlag(expandAttributes),
            getFlag(expandTables), getFlag(expandTableAttributes)).toString()
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
    try {
      RecordClass rc = getWdkModel().getRecordClass(recordClassName);
      return Response.ok(
          RecordClassFormatter.getRecordClassJson(rc, getFlag(expandAttributes),
              getFlag(expandTables), getFlag(expandTableAttributes)).toString()
      ).build();
    }
    catch (WdkModelException e) {
      return Response.status(Status.NOT_FOUND).build();
    }
  }

  @GET
  @Path("{recordClassName}/attribute")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getAttributesInfo(
      @PathParam("recordClassName") String recordClassName,
      @QueryParam("expandAttributes") Boolean expandAttributes) {
    try {
      RecordClass rc = getWdkModel().getRecordClass(recordClassName);
      return Response.ok(
          RecordClassFormatter.getAttributesJson(rc, getFlag(expandAttributes)).toString()
      ).build();
    }
    catch (WdkModelException e) {
      return Response.status(Status.NOT_FOUND).build();
    }
  }

  @GET
  @Path("{recordClassName}/table")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getTablesInfo(
      @PathParam("recordClassName") String recordClassName,
      @QueryParam("expandTables") Boolean expandTables,
      @QueryParam("expandTableAttributes") Boolean expandTableAttributes) {
    try {
      RecordClass rc = getWdkModel().getRecordClass(recordClassName);
      return Response.ok(
          RecordClassFormatter.getTablesJson(rc, getFlag(expandTables),
              getFlag(expandTableAttributes)).toString()
      ).build();
    }
    catch (WdkModelException e) {
      return Response.status(Status.NOT_FOUND).build();
    }
  }

  @GET
  @Path("{recordClassName}/table/{tableName}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getTableInfo(
      @PathParam("recordClassName") String recordClassName,
      @PathParam("tableName") String tableName,
      @QueryParam("expandTableAttributes") Boolean expandTableAttributes) {
    return getTableResponse(recordClassName, tableName, expandTableAttributes, false);
  }

  @GET
  @Path("{recordClassName}/table/{tableName}/attribute")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getTableAttributesInfo(
      @PathParam("recordClassName") String recordClassName,
      @PathParam("tableName") String tableName,
      @QueryParam("expandTableAttributes") Boolean expandTableAttributes) {
    return getTableResponse(recordClassName, tableName, expandTableAttributes, true);
  }
  
  @POST
  @Path("{recordClassName}/get")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response buildResult(@PathParam("recordClassName") String recordClassName, String body) throws WdkModelException, WdkUserException {
    try {
      JSONObject json = new JSONObject(body);

      RecordRequest request = RecordRequest.createFromJson(
          getCurrentUser(), json.getJSONObject("recordInstanceSpecification"), recordClassName, getWdkModelBean());
      
      RecordInstance recordInstance = getRecordInstance(getCurrentUser(), request);

      return Response.ok(RecordStreamer.getRecordAsStream(recordInstance, request.getAttributeNames(), request.getTableNames())).build();
    }
    catch (JSONException | RequestMisformatException e) {
      LOG.warn("Passed request body deemed unacceptable", e);
      return getBadRequestResponse(e.getMessage());
    }
  }
  
  private static RecordInstance getRecordInstance(User user, RecordRequest recordRequest) throws WdkModelException, WdkUserException {
    RecordClass recordClass = recordRequest.getRecordClass();
    return new RecordInstance(user, recordClass, recordRequest.getPrimaryKey());
  }
  
  private Response getTableResponse(String recordClassName, String tableName,
      Boolean expandTableAttributes, boolean attributesOnly) {
    try {
      RecordClass rc = getWdkModel().getRecordClass(recordClassName);
      TableField table = rc.getTableFieldMap().get(tableName);
      boolean expandAttributes = getFlag(expandTableAttributes);
      if (table == null) throw new WdkModelException ("Table '" + tableName +
          "' not found for RecordClass '" + recordClassName + "'");
      return Response.ok((attributesOnly ?
          RecordClassFormatter.getAttributesJson(table, expandAttributes) :
          RecordClassFormatter.getTableJson(table, expandAttributes)
      ).toString()).build();
    }
    catch (WdkModelException e) {
      return Response.status(Status.NOT_FOUND).build();
    }
  }
}
