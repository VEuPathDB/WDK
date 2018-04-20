package org.gusdb.wdk.model.fix;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.gusdb.fgputil.IoUtil;
import org.gusdb.fgputil.json.JsonType;
import org.gusdb.wdk.model.WdkModelException;
import org.json.JSONArray;
import org.json.JSONObject;

public class QuestionComparison {
  private final String QUESTION_SERVICE_URL = "/service/questions";
  private List<String> commonQuestionList;

  public static void main(String[] args) {
    if (args.length != 2) {
      System.err.println("Usage: questionComparison <qa url> <prod url>");
      System.exit(1);
    }
    try {
      new QuestionComparison().execute(args[0], args[1]);
    }
    catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
	
  protected void execute(String qaUrl, String prodUrl) throws Exception {
    validateParameters(qaUrl, prodUrl);
    
    try {
      ComparisonBean questionNameComparison = compareQuestionNames(qaUrl, prodUrl);
      System.out.println("Result of Question Comparisons between " + qaUrl + " and " + prodUrl);
      questionNameComparison.display();
      List<ComparisonBean> parameterNameComparisons = compareParameterNames(qaUrl, prodUrl);
      parameterNameComparisons.stream().forEach(comparison -> comparison.display());
    }
    catch(WdkModelException wme) {
      throw new RuntimeException(wme);
    }
  }
  
  protected void validateParameters(String qaUrl, String prodUrl) {
    String pattern = "^(http|https)://.*\\.(org|net)/.*";
    if(!Pattern.matches(pattern, qaUrl) || !Pattern.matches(pattern, prodUrl)) {
	  throw new RuntimeException("One or both of the urls provided is not recognized as such.");
	}
  }
  
  protected ComparisonBean compareQuestionNames(String qaUrl, String prodUrl) throws WdkModelException {
    JSONArray qaData = new JSONArray(callQuestionService(qaUrl + QUESTION_SERVICE_URL));
    List<String> qaQuestionNames = new ArrayList<>();
    for(int i = 0; i < qaData.length(); i++) {
  	  qaQuestionNames.add(qaData.getString(i));
    }
    
    JSONArray prodData = new JSONArray(callQuestionService(prodUrl + QUESTION_SERVICE_URL));
    List<String> prodQuestionNames = new ArrayList<>();
    for(int i = 0; i < prodData.length(); i++) {
   	  prodQuestionNames.add(prodData.getString(i));
    }
    
    List<String> newQuestionList = new ArrayList<>(qaQuestionNames);
    newQuestionList.removeAll(prodQuestionNames);
  
    List<String> invalidQuestionList = new ArrayList<>(prodQuestionNames);
    invalidQuestionList.removeAll(qaQuestionNames);
    
    commonQuestionList = new ArrayList<>(qaQuestionNames);
    commonQuestionList.retainAll(prodQuestionNames);
   
    ComparisonBean comparison = new ComparisonBean();
    comparison.setContext("");
    comparison.setMissingFromProdName("New Questions");
    comparison.setMissingFromQaName("Invalid Questions");
    comparison.setMissingFromProd(newQuestionList);
    comparison.setMissingFromQa(invalidQuestionList);
    return comparison;
  }
  
  protected List<ComparisonBean> compareParameterNames(String qaUrl, String prodUrl) throws WdkModelException {
	List<ComparisonBean> comparisons = new ArrayList<>();
    for(String question : commonQuestionList) {
    	
      String qaQuestionPath = Paths.get(QUESTION_SERVICE_URL, question).toString();
      JSONObject qaData = new JSONObject(callQuestionService(qaUrl + qaQuestionPath));
      JSONArray qaParameters = qaData.getJSONArray("parameters");
      List<String> qaParameterNames = new ArrayList<>();
      for(int i = 0; i < qaParameters.length(); i++) {
    	qaParameterNames.add(qaParameters.getString(i));
      }
      
      String prodQuestionPath = Paths.get(QUESTION_SERVICE_URL, question).toString();
      JSONObject prodData = new JSONObject(callQuestionService(prodUrl + prodQuestionPath));
      JSONArray prodParameters = prodData.getJSONArray("parameters");
      List<String> prodParameterNames = new ArrayList<>();
      for(int i = 0; i < prodParameters.length(); i++) {
    	prodParameterNames.add(prodParameters.getString(i));
      }
      
      List<String> newParameterList = new ArrayList<>(qaParameterNames);
      newParameterList.removeAll(prodParameterNames);
      
      List<String> invalidParameterList = new ArrayList<>(prodParameterNames);
      invalidParameterList.removeAll(qaParameterNames);
   
      ComparisonBean comparison = new ComparisonBean();
      comparison.setContext(question);
      comparison.setMissingFromProdName("New Parameters");
      comparison.setMissingFromQaName("Invalid Parameters");
      comparison.setMissingFromProd(newParameterList);
      comparison.setMissingFromQa(invalidParameterList);
      comparisons.add(comparison);
    }
    return comparisons;
  }  
  
  protected String callQuestionService(String serviceUrl) throws WdkModelException {
    if(!serviceUrl.matches(".*\\s.*")) {
      Client client = ClientBuilder.newBuilder().build();
      Response response = client
      	.target(serviceUrl)
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
	return null;
  }

}
