package org.gusdb.wdk.model.fix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;

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
  
  public void display() {
	Optional<String> missingFromQaLongestString = getMissingFromQa().stream().max(Comparator.comparingInt(String::length));
	int missingFromQaLength = missingFromQaLongestString.isPresent() ? missingFromQaLongestString.get().length() : getMissingFromQaName().length();
	Optional<String> missingFromProdLongestString = getMissingFromProd().stream().max(Comparator.comparingInt(String::length));
	int missingFromProdLength = missingFromProdLongestString.isPresent() ? missingFromProdLongestString.get().length() : getMissingFromProdName().length();
	String line = new String(new char[23 + missingFromQaLength + missingFromProdLength]).replace('\0', '-');
	System.out.println(line);
    System.out.printf("%n%s|%s|%s|%n",
	            StringUtils.center("Project", 17),
	            StringUtils.center(getMissingFromQaName(), missingFromQaLength + 2),
	            StringUtils.center(getMissingFromProdName(), missingFromProdLength + 2));
    System.out.println(line);
    String projectFormat = " %1$-15s |";
    String missingFromQaFormat = " %2$" + missingFromQaLength + "s |";
    String missingFromProdFormat = " %3$" + missingFromProdLength + "s |%n";
    String format = projectFormat.concat(missingFromQaFormat).concat(missingFromProdFormat);
    int maxRows = Math.max(getMissingFromQa().size(),getMissingFromProd().size());
    if(maxRows == 0) {
    	  System.out.printf(format, project, "", "");
  	  System.out.println(line);
    }
    else {
      for(int i = 0; i < maxRows; i++) {
    	    String project = i == 0 ? getProject() : "";
    	    String missingFromQaItem = getMissingFromQa().size() < i + 1 ? "" : getMissingFromQa().get(i);
    	    String missingFromProdItem = getMissingFromProd().size() < i + 1 ? "" : getMissingFromProd().get(i);
    	    System.out.printf(format, project, missingFromQaItem, missingFromProdItem);
    	    System.out.println(line);
      }  
    }
  }  
}
