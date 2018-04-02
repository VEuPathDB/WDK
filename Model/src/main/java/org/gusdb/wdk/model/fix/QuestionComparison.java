package org.gusdb.wdk.model.fix;

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.glassfish.jersey.client.ClientConfig;
import org.gusdb.wdk.model.WdkModelException;

public class QuestionComparison {

  public static void main(String[] args) {
    if (args.length != 1) {
      System.err.println("Usage: questionComparison <urls\nComma separated list of urls.");
      System.exit(1);
    }
    try {
        new QuestionComparison().execute(args[0]);
    }
    catch (Exception e) {
      e.printStackTrace();
      System.exit(1);
    }
  }
	
  protected void execute(String urlList) throws Exception {
    String[] urls = urlList.split(",");
    List<ComparisonBean> comparisons = new ArrayList<>();
    for(String url : urls) {
      comparisons.add(callComparisonService(url + "/service/comparison/questionName"));
    }
    System.out.println("Result of Question Comparison:");
    //System.out.println(comparisons.toString());
    comparisons.stream().forEach(comparison -> comparison.display());
  }

  protected ComparisonBean callComparisonService(String serviceUrl) throws WdkModelException {
    Client client = ClientBuilder.newClient(new ClientConfig().register( ComparisonBean.class ));
    Response response = client
    		.target(serviceUrl)
    		.request(MediaType.APPLICATION_JSON)
    		.get();
    try {
      if (response.getStatus() == 200) {
    	    ComparisonBean comparison = response.readEntity(ComparisonBean.class);
    	    return comparison;
      }
      else {
        throw new WdkModelException("Failed to call comparison service - " + response.getStatus() + " : " + response.getStatusInfo());
      }
    }
    finally {
      response.close();
      client.close();
    }
  } 

}
