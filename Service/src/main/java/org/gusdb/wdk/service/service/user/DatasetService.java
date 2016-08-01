package org.gusdb.wdk.service.service.user;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.functional.FunctionalInterfaces.Function;
import org.gusdb.fgputil.functional.Functions;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dataset.AbstractDatasetParser;
import org.gusdb.wdk.model.dataset.Dataset;
import org.gusdb.wdk.model.dataset.DatasetParser;
import org.gusdb.wdk.model.dataset.WdkDatasetException;
import org.gusdb.wdk.service.UserBundle;
import org.gusdb.wdk.service.formatter.Keys;
import org.gusdb.wdk.service.request.RequestMisformatException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DatasetService extends UserService {

  public DatasetService(@PathParam(USER_ID_PATH_PARAM) String uid) {
    super(uid);
  }

  /**
   * Input JSON should be:
   * {
   *   "displayName": String,
   *   "description": String,
   *   "ids": Array<String>
   * }
   * 
   * @param body request body (JSON)
   * @return HTTP response for this request
   */
  @POST
  @Path("dataset")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response addDatasetFromJson(String body) throws WdkModelException {
    try {
      UserBundle userBundle = getUserBundle(Access.PRIVATE);
      JSONObject input = new JSONObject(body);
      JSONArray jsonIds = input.getJSONArray("ids");
      if (jsonIds.length() == 0)
        throw new RequestMisformatException("At least 1 ID must be submitted");
      final List<String> ids = new ArrayList<String>();
      for (int i = 0; i < jsonIds.length(); i++) {
        ids.add(jsonIds.getString(i));
      }
      // FIXME: this is a total hack to comply with the dataset factory API
      //   We are closing over the JSON array we already parsed and will return
      //   a List<String> version of that array
      DatasetParser parser = new AbstractDatasetParser() {
        @Override
        public List<String[]> parse(String content) throws WdkDatasetException {
          return Functions.mapToList(ids, new Function<String, String[]>() {
            @Override public String[] apply(String str) { return new String[]{ str }; }
          });
        }
        @Override
        public String getName() {
          return "anonymous";
        }
      };
      Dataset dataset = getWdkModel().getDatasetFactory().createOrGetDataset(
          userBundle.getTargetUser(), parser, FormatUtil.join(ids.toArray(), " "), "");
      JSONObject datasetMetadata = new JSONObject();
      datasetMetadata.put(Keys.ID, dataset.getDatasetId());
      return Response.ok(datasetMetadata.toString()).build();
    }
    catch (RequestMisformatException | JSONException | WdkUserException e) {
      throw new BadRequestException(e);
    }
  }

  @POST
  @Path("dataset")
  @Consumes(MediaType.MULTIPART_FORM_DATA)
  @Produces(MediaType.APPLICATION_JSON)
  public Response addDatasetFromFile() throws WdkModelException
      //@FormParam("file") InputStream fileInputStream,
      //@FormParam("file") FormDataContentDisposition contentDispositionHeader)
  {
    UserBundle userBundle = getUserBundle(Access.PRIVATE);
    return Response.ok("{ }").build();

  }
}
