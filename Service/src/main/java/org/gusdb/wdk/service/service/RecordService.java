package org.gusdb.wdk.service.service;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.RecordClassSet;
import org.json.JSONObject;

@Path("/record")
public class RecordService extends WdkService {

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getRecordClassList() {
    JSONObject json = new JSONObject();
    for (RecordClassSet rcSet : getWdkModel().getAllRecordClassSets()) {
      for (RecordClass rc : rcSet.getRecordClasses()) {
        json.append("rcList", rc.getFullName());
      }
    }
    return Response.ok(json.getJSONArray("rcList").toString()).build();
  }

  @GET
  @Path("{recordClassName}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getRecordClassInfo(@PathParam("recordClassName") String recordClassName) {
    try {
      RecordClass rc = getWdkModel().getRecordClass(recordClassName);
      return Response.ok(getRecordClassJson(rc).toString()).build();
    }
    catch (WdkModelException e) {
      return Response.status(Status.NOT_FOUND).build();
    }
  }

  private JSONObject getRecordClassJson(RecordClass recordClass) {
    JSONObject json = new JSONObject();
    json.put("fullName", recordClass.getFullName());
    json.put("displayName", recordClass.getDisplayName());
    json.put("displayNamePlural", recordClass.getDisplayNamePlural());
    return json;
  }
}
