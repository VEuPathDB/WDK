package org.gusdb.wdk.service.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.gusdb.fgputil.IoUtil;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.fix.ComparisonBean;
import org.json.JSONArray;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Path("/")
public class ComparisonService extends WdkService {
  private final String QUESTION_NAME_SERVICE_URL = "/service/questions";
  
  protected String getQaBaseUri() {
    String project = getWdkModel().getProjectId().toLowerCase();
    return getScheme() + "://qa." + project + ".org" + getContextPath().split(Pattern.quote("."))[0];
  }
  
  protected String getProdBaseUri() {
    String project = getWdkModel().getProjectId().toLowerCase();
    return getScheme() + "://www." + project + ".org" + getContextPath().split(Pattern.quote("."))[0];
  }

  /**
   * Provides a JSON object for this project detailing both new questions (those on qa not in
   * production) and invalid questions (those in production not on qa).
   * @return
   * @throws WdkModelException
   */
  @GET
  @Path("comparison/questionName")
  @Produces(MediaType.APPLICATION_JSON)
  public ComparisonBean compareQuestionNames() throws WdkModelException {
	List<String> qaList = getResults(getQaBaseUri());
	List<String> prodList = getResults(getProdBaseUri());
	
    List<String> newQuestionList = new ArrayList<>(qaList);
    newQuestionList.removeAll(prodList);
    JSONArray newQuestionJson = new JSONArray();
    newQuestionList.stream().forEach(entry -> {newQuestionJson.put(entry);});
    
    List<String> invalidQuestionList = new ArrayList<>(prodList);
    invalidQuestionList.removeAll(prodList);
    JSONArray invalidQuestionJson = new JSONArray();
    invalidQuestionList.stream().forEach(entry -> {invalidQuestionJson.put(entry);});
    ComparisonBean comparison = new ComparisonBean();
    comparison.setContext("");
    comparison.setProject(getWdkModel().getProjectId());
    comparison.setMissingFromProdName("New Questions");
    comparison.setMissingFromQaName("Invalid Questions");
    comparison.setMissingFromProd(newQuestionList);
    comparison.setMissingFromQa(invalidQuestionList);
    return comparison;
  }
  
  /**
   * Call the question service for the website specified by the url and return
   * an ordered list of question names that the site supports.
   * @param url
   * @return
   * @throws WdkModelException
   */
  protected List<String> getResults(String url) throws WdkModelException {
	String result = callQuestionService(url);
    ObjectMapper mapper = new ObjectMapper();
    TypeReference<List<String>> listType = new TypeReference<List<String>>() {};
    try {
      List<String> list = mapper.readValue(result, listType);
      list.sort(String.CASE_INSENSITIVE_ORDER);
      return list;
    }
    catch(IOException ioe) {
  	  throw new WdkModelException("Unable to parse result from question service.");
    }
  }

  /**
   * Call the question service for the website given by the url provided and return
   * the question names as a comma delimited string.
   * @param url
   * @return
   * @throws WdkModelException
   */
  protected String callQuestionService(String url) throws WdkModelException {
    Client client = ClientBuilder.newBuilder().build();
    Response response = client
	        .target(url + this.QUESTION_NAME_SERVICE_URL)
	        .request()
	        .get();
    try {
      if (response.getStatus() == 200) {
    	    InputStream resultStream = (InputStream) response.getEntity();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        IoUtil.transferStream(buffer, resultStream);
        return new String(buffer.toByteArray());
      }
      else {
        throw new WdkModelException("Failed to call question service - " + response.getStatus() + " : " + response.getStatusInfo());
      }
    }
    catch(IOException ioe) {
    	  throw new WdkModelException("Failed to parse question service response.");
    }
    finally {
      response.close();
      client.close();
    }
  } 

}
