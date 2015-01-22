package org.gusdb.wdk.service.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.TableField;
import org.gusdb.wdk.service.formatter.RecordFormatter;

@Path("/record")
public class RecordService extends WdkService {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getRecordClassList(
      @QueryParam("expandRecordClasses") Boolean expandRecordClasses,
      @QueryParam("expandAttributes") Boolean expandAttributes,
      @QueryParam("expandTables") Boolean expandTables,
      @QueryParam("expandTableAttributes") Boolean expandTableAttributes) {
    return Response.ok(
        RecordFormatter.getRecordClassesJson(getWdkModel().getAllRecordClassSets(),
            getFlag(expandRecordClasses), getFlag(expandAttributes),
            getFlag(expandTables), getFlag(expandTableAttributes))
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
          RecordFormatter.getRecordClassJson(rc, getFlag(expandAttributes),
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
          RecordFormatter.getAttributesJson(rc, getFlag(expandAttributes)).toString()
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
          RecordFormatter.getTablesJson(rc, getFlag(expandTables),
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
  

  private Response getTableResponse(String recordClassName, String tableName,
      Boolean expandTableAttributes, boolean attributesOnly) {
    try {
      RecordClass rc = getWdkModel().getRecordClass(recordClassName);
      TableField table = rc.getTableFieldMap().get(tableName);
      boolean expandAttributes = getFlag(expandTableAttributes);
      if (table == null) throw new WdkModelException ("Table '" + tableName +
          "' not found for RecordClass '" + recordClassName + "'");
      return Response.ok((attributesOnly ?
          RecordFormatter.getAttributesJson(table, expandAttributes) :
          RecordFormatter.getTableJson(table, expandAttributes)
      ).toString()).build();
    }
    catch (WdkModelException e) {
      return Response.status(Status.NOT_FOUND).build();
    }
  }
}
