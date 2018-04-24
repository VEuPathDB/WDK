package org.gusdb.wdk.model.fix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;

public class ComparisonBean {
  public static String NL = System.getProperty("line.separator");
  private String _context;	
  private String _missingFromQaName;
  private String _missingFromProdName;
  private List<String> _missingFromQa = new ArrayList<>();
  private List<String> _missingFromProd = new ArrayList<>();
  
  public ComparisonBean() {}
  
  public ComparisonBean(String context, String missingFromQaName, String missingFromProdName, List<String> missingFromQa, List<String> missingFromProd) {
    _context = context;
    _missingFromQaName = missingFromQaName;
    _missingFromProdName = missingFromProdName;
    _missingFromQa = missingFromQa;
    _missingFromProd = missingFromProd;
  }
  
  public String getContext() {
	return _context;
  }
  
  public String getMissingFromQaName() {
	return _missingFromQaName;
  }
  
  public String getMissingFromProdName() {
	return _missingFromProdName;
  }
  
  public List<String> getMissingFromQa() {
	return _missingFromQa;
  }
  
  public List<String> getMissingFromProd() {
	return _missingFromProd;
  }
 
  public String toString() {
    return NL + getContext() + ":" + NL +
     getMissingFromQaName() + "-" +
     Arrays.toString(getMissingFromQa().toArray()) + NL +
     getMissingFromProdName() + " - " +
     Arrays.toString(getMissingFromProd().toArray()) + NL;
  }
  
  public void display() {
	if(getMissingFromQa().isEmpty() && getMissingFromProd().isEmpty()) return;
	System.out.println(NL + getContext());
	Optional<String> missingFromQaLongestString = getMissingFromQa().stream().max(Comparator.comparingInt(String::length));
	int missingFromQaLength = missingFromQaLongestString.isPresent() ? Math.max(getMissingFromQaName().length(), missingFromQaLongestString.get().length()) : getMissingFromQaName().length();
	Optional<String> missingFromProdLongestString = getMissingFromProd().stream().max(Comparator.comparingInt(String::length));
	int missingFromProdLength = missingFromProdLongestString.isPresent() ? Math.max(getMissingFromProdName().length(), missingFromProdLongestString.get().length()) : getMissingFromProdName().length();
	String line = new String(new char[5 + missingFromQaLength + missingFromProdLength]).replace('\0', '-');
	System.out.println(line);
    System.out.printf("%s|%s|%n",
	            StringUtils.center(getMissingFromQaName(), missingFromQaLength + 2),
	            StringUtils.center(getMissingFromProdName(), missingFromProdLength + 2));
    System.out.println(line);
    String missingFromQaFormat = " %1$" + missingFromQaLength + "s |";
    String missingFromProdFormat = " %2$" + missingFromProdLength + "s |%n";
    String format = missingFromQaFormat.concat(missingFromProdFormat);
    int maxRows = Math.max(getMissingFromQa().size(),getMissingFromProd().size());
    if(maxRows == 0) {
      System.out.printf(format, "", "");
  	  System.out.println(line);
    }
    else {
      for(int i = 0; i < maxRows; i++) {
    	    String missingFromQaItem = getMissingFromQa().size() < i + 1 ? "" : getMissingFromQa().get(i);
    	    String missingFromProdItem = getMissingFromProd().size() < i + 1 ? "" : getMissingFromProd().get(i);
    	    System.out.printf(format, missingFromQaItem, missingFromProdItem);
    	    System.out.println(line + NL);
      }  
    }
  }  
}
