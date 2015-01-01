package org.gusdb.wdk.service.service;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Variant;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.JsonType;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

@Path("/sample")
public class SampleService extends WdkService {

  private static final Logger LOG = Logger.getLogger(SampleService.class);

  private static AtomicLong ID_SEQUENCE;
  private static Map<Long, JsonType> STATE = new LinkedHashMap<>();

  // set initial data
  static {
    resetData();
  }

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createElement(String body) {
    // parse request body to ensure it is JSON
    try {
      JsonType input = new JsonType(body);
      long nextId = ID_SEQUENCE.getAndIncrement();
      STATE.put(nextId, input);
      String newUri = getUriInfo().getAbsolutePath() + "/" + nextId;
      JSONObject output = new JSONObject();
      output.put("id", nextId);
      return Response.created(URI.create(newUri)).entity(output.toString()).build();
    }
    catch (JSONException e) {
      LOG.info("Passed request body deemed unacceptable", e);
      return Response.notAcceptable(Collections.<Variant>emptyList()).build();
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public Response getIds(
      @QueryParam("offset") Long offset,
      @QueryParam("numRecords") Long numRecords,
      @QueryParam("expandRecords") Boolean expandRecords) {
    if (expandRecords == null) expandRecords = false;
    List<Long> ids = getSubList(new ArrayList<>(STATE.keySet()), offset, numRecords);
    if (expandRecords) {
      JSONObject json = new JSONObject();
      for (Long id : ids) {
        Object obj = STATE.get(id).getNativeObject();
        LOG.info("Adding object of type " + obj.getClass().getName() + ": " + obj.toString());
        json.put(String.valueOf(id), obj);
      }
      return Response.ok(json.toString()).build();
    }
    else {
      return Response.ok(new JSONArray(ids).toString()).build();
    }
  }

  private List<Long> getSubList(List<Long> list, Long offset, Long numRecords) {
    if (offset == null || offset < 0) offset = 0L;
    long maxNumRecords = list.size() - offset;
    numRecords = (numRecords == null ? maxNumRecords :
      numRecords < 0 ? 0 : Math.min(numRecords, maxNumRecords));
    return list.subList(offset.intValue(), Long.valueOf(offset + numRecords).intValue());
  }

  @GET
  @Path("{id}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getById(@PathParam("id") long id) {
    JsonType obj = STATE.get(id);
    return (obj == null ?
        Response.status(Status.NOT_FOUND).build() :
        Response.ok(obj.toString()).build()
    );
  }

  @PUT
  @Path("{id}")
  @Consumes(MediaType.APPLICATION_JSON)
  public Response setById(@PathParam("id") long id, String body) {
    // parse request body to ensure it is JSON
    try {
      if (STATE.containsKey(id)) {
        JsonType json = new JsonType(body);
        STATE.put(id, json);
        return Response.ok().build();
      }
      else {
        LOG.warn("Attempted update of non-existing resource");
        return Response.notAcceptable(Collections.<Variant>emptyList()).build();
      }
    }
    catch (JSONException e) {
      LOG.info("Passed request body deemed unacceptable", e);
      return Response.notAcceptable(Collections.<Variant>emptyList()).build();
    }
  }
  
  @DELETE
  @Path("{id}")
  public Response deleteById(@PathParam("id") long id) {
    STATE.remove(id);
    return Response.ok().build();
  }

  @POST
  @Path("reset")
  @Produces(MediaType.APPLICATION_JSON)
  public Response resetData(
      @QueryParam("offset") Long offset,
      @QueryParam("numRecords") Long numRecords,
      @QueryParam("expandRecords") Boolean expandRecords) {
    resetData();
    return getIds(offset, numRecords, expandRecords);
  }

  private static void resetData() {
    ID_SEQUENCE = new AtomicLong(1);
    STATE.clear();
    int numSamples = 5;
    JSONObject json;
    for (int i = 1; i <= numSamples; i++) {
     json = new JSONObject();
     json.put("value", "some value for record #" + i);
     STATE.put(ID_SEQUENCE.getAndIncrement(), new JsonType(json));
    }
  }
}
