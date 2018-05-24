package org.gusdb.wdk.model.fix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang.StringUtils;

/**
 * Simple generic comparison object for capturing comparison information for later display in a tabular report.
 * @author crisl-adm
 *
 */
public class ComparisonBean {
  public static String NL = System.getProperty("line.separator");
  private String _context;	
  private String _missingFromQaName;
  private String _missingFromProdName;
  private List<String> _missingFromQa = new ArrayList<>();
  private List<String> _missingFromProd = new ArrayList<>();
  
  public ComparisonBean() {}
  
  public ComparisonBean(String context, String missingFromProdName, String missingFromQaName, List<String> missingFromProd, List<String> missingFromQa) {
    _context = context;
    _missingFromProdName = missingFromProdName;
    _missingFromQaName = missingFromQaName;
    _missingFromProd = missingFromProd;
    _missingFromQa = missingFromQa;
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
 
  @Override
  public String toString() {
    return NL + getContext() + ":" + NL +
     getMissingFromQaName() + "-" +
     Arrays.toString(getMissingFromQa().toArray()) + NL +
     getMissingFromProdName() + " - " +
     Arrays.toString(getMissingFromProd().toArray()) + NL;
  }
  
  /**
   * Method to allow presentation of generic comparison data in a tabular format.
   * @param displayOption - invalid, new or other.  If invalid show only invalid questions
   * (i.e., missing from qa) and if new show only new questions (i.e., missing from prod).
   * Otherwise show both side by side.
   */
  public void display(String displayOption) {

	switch(displayOption) {
	
	  case "invalid":
		display(getMissingFromQaName(), getMissingFromQa());
		break;
		
	  case "new":
		display(getMissingFromProdName(), getMissingFromProd());
		break;
		
	  default:
			
		// If there are no differences display nothing for this comparison.  
		if(getMissingFromQa().isEmpty() && getMissingFromProd().isEmpty()) return;
	
		// Announce the context in which the comparison is being made.
		System.out.println(NL + getContext());
	
		// Calculate the proper table length
		Optional<String> missingFromQaLongestString = getMissingFromQa().stream().max(Comparator.comparingInt(String::length));
		int missingFromQaLength = missingFromQaLongestString.isPresent() ? Math.max(getMissingFromQaName().length(), missingFromQaLongestString.get().length()) : getMissingFromQaName().length();
		Optional<String> missingFromProdLongestString = getMissingFromProd().stream().max(Comparator.comparingInt(String::length));
		int missingFromProdLength = missingFromProdLongestString.isPresent() ? Math.max(getMissingFromProdName().length(), missingFromProdLongestString.get().length()) : getMissingFromProdName().length();

		// Create the table header
		String line = new String(new char[5 + missingFromQaLength + missingFromProdLength]).replace('\0', '-');
		System.out.println(line);
		System.out.printf("%s|%s|%n",
	            StringUtils.center(getMissingFromQaName(), missingFromQaLength + 2),
	            StringUtils.center(getMissingFromProdName(), missingFromProdLength + 2));
		System.out.println(line);
    
		// Setup format for table body
		String missingFromQaFormat = " %1$" + missingFromQaLength + "s |";
		String missingFromProdFormat = " %2$" + missingFromProdLength + "s |%n";
		String format = missingFromQaFormat.concat(missingFromProdFormat);
		int maxRows = Math.max(getMissingFromQa().size(),getMissingFromProd().size());
    
		// Fill in table rows
		for(int i = 0; i < maxRows; i++) {
		  String missingFromQaItem = getMissingFromQa().size() < i + 1 ? "" : getMissingFromQa().get(i);
		  String missingFromProdItem = getMissingFromProd().size() < i + 1 ? "" : getMissingFromProd().get(i);
		  System.out.printf(format, missingFromQaItem, missingFromProdItem);
		  System.out.println(line + NL);  
	    }
	}
  }
  
  /**
   * Tabular display of only one side of the comparison data
   * @param name - title for items to display
   * @param items - comparison data to display
   */
  public void display(String name, List<String> items) {
	  
	// If there are no differences display nothing for this comparison.  
	if(items.isEmpty()) return;
	
	// Announce the context in which the comparison is being made.
	System.out.println(NL + getContext());
	
	// Calculate the proper table length
	Optional<String> itemsLongestString = items.stream().max(Comparator.comparingInt(String::length));
	int columnLength = itemsLongestString.isPresent() ? Math.max(name.length(), itemsLongestString.get().length()) : name.length();

	// Create the table header
	String line = new String(new char[2 + columnLength]).replace('\0', '-');
	System.out.println(line);
	System.out.printf("%s|%n", StringUtils.center(name, columnLength + 2));
    System.out.println(line);
    
    // Setup format for table body
    String format = " %1$" + columnLength + "s |%n";
   
    // Fill in table rows
    for(int i = 0; i < items.size(); i++) {
      String item = items.size() < i + 1 ? "" : items.get(i);
      System.out.printf(format, item);
      System.out.println(line + NL);  
    }
  }  
}
