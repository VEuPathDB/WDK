package org.gusdb.wdk.model.fix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ComparisonBean {
  private static String NL = System.getProperty("line.separator");
  private String context;	
  private String project;
  private String missingFromQaName;
  private String missingFromProdName;
  private List<String> missingFromQa = new ArrayList<>();
  private List<String> missingFromProd = new ArrayList<>();
  
  public String getContext() {
	return context;
  }
  
  public void setContext(String context) {
	this.context = context;
  }
  
  public String getProject() {
	return project;
  }
  
  public void setProject(String project) {
	this.project = project;
  }
  
  public String getMissingFromQaName() {
	return missingFromQaName;
  }
  
  public void setMissingFromQaName(String missingFromQaName) {
	this.missingFromQaName = missingFromQaName;
  }
  
  public String getMissingFromProdName() {
	return missingFromProdName;
  }
  
  public void setMissingFromProdName(String missingFromProdName) {
	this.missingFromProdName = missingFromProdName;
  }
  
  public List<String> getMissingFromQa() {
	return missingFromQa;
  }
  
  public void setMissingFromQa(List<String> missingFromQa) {
	this.missingFromQa = missingFromQa;
  }
  
  public List<String> getMissingFromProd() {
	return missingFromProd;
  }
  
  public void setMissingFromProd(List<String> missingFromProd) {
	this.missingFromProd = missingFromProd;
  }
  
  public String toString() {
    return NL + getProject() + ":" + NL +
     getMissingFromQaName() + "-" +
     Arrays.toString(getMissingFromQa().toArray()) + NL +
    	 getMissingFromProdName() + " - " +
    	 Arrays.toString(getMissingFromProd().toArray()) + NL;
  }

}
