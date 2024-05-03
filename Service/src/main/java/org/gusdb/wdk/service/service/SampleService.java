package org.gusdb.wdk.service.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import jakarta.ws.rs.core.Response.Status;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.json.JsonType;
import org.gusdb.fgputil.json.JsonType.ValueType;
import org.gusdb.wdk.core.api.JsonKeys;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/*
 * TODO: remove this file when this branch is merged to trunk
 */
@Path("/sample")
public class SampleService {

  private static final Logger LOG = Logger.getLogger(SampleService.class);

  private static AtomicLong ID_SEQUENCE;
  private static Map<Long, JsonType> STATE = new LinkedHashMap<>();

  // set initial data
  static {
    resetData();
  }

  @Context
  private UriInfo _uriInfo;

  @POST
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createElement(String body) {
    // parse request body to ensure it is JSON
    try {
      JsonType input = JsonType.parse(body);
      if (!input.getType().equals(ValueType.OBJECT) && !input.getType().equals(ValueType.ARRAY)) {
        LOG.info("Passed body '" + body + "' is neither JSON object nor array");
        return Response.notAcceptable(Collections.emptyList()).build();
      }
      long nextId = ID_SEQUENCE.getAndIncrement();
      STATE.put(nextId, input);
      JSONObject output = new JSONObject();
      output.put("id", nextId);

      return Response.created(_uriInfo.getAbsolutePathBuilder().build(nextId))
          .entity(new JSONObject().put(JsonKeys.ID, nextId)).build();
    }
    catch (JSONException | IllegalArgumentException e) {
      LOG.info("Passed request body deemed unacceptable", e);
      return Response.notAcceptable(Collections.emptyList()).build();
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
        Object obj = STATE.get(id).get();
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
        return Response.notAcceptable(Collections.emptyList()).build();
      }
    }
    catch (JSONException e) {
        LOG.info("Passed request body deemed unacceptable", e);
      return Response.notAcceptable(Collections.emptyList()).build();
    }
  }

  @DELETE
  @Path("{id}")
  public Response deleteById(@PathParam("id") long id) {
    STATE.remove(id);
    return Response.ok().build();
  }

  @GET
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
    for (int i = 1; i <= numSamples; i++) {
      var json = new JSONObject().put("value", "some value for record #" + i);
      STATE.put(ID_SEQUENCE.getAndIncrement(), new JsonType(json));
    }
  }
}
