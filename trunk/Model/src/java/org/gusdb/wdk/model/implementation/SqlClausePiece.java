package org.gusdb.wdk.model.implementation;

import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.ResultFactory;

import java.util.regex.Pattern;

public class SqlClausePiece {

    String origSql;
    String joinTableName;
    //locations relative to original sql
    int start;
    int end;

    /**
     * A piece of sql that belongs to an sql clause and that has no kid
     * clauses.  If the sql clause has no kids, then the piece is its
     * full length (exluding bounding parens)
     *
     * @param start index of the start of this piece (non-paren)
     * @param end index of end of this piece (non-paren), ie, index of last char
     */
    public SqlClausePiece(String origSql, int start, int end, String joinTableName) {
	this.origSql = origSql;
	this.start = start;
	this.end = end;
	this.joinTableName = joinTableName;
    }

    String getFinalPieceSql(boolean needsSelectFix, 
			    boolean needsFromFix,
			    boolean needsWhereFix,
			    int pageStartIndex,
			    int pageEndIndex) throws WdkModelException {
	String finalSql = origSql.substring(start, end+1);
	if (needsSelectFix) finalSql = addJoinTableIndexToSelect(finalSql);
	if (needsFromFix) finalSql = addJoinTableToFrom(finalSql);
	if (needsWhereFix) finalSql = addConstraintsToWhere(finalSql,
							    pageStartIndex,
							    pageEndIndex);
	return finalSql;
    }

    String addJoinTableIndexToSelect(String sql) {
	// a real select must be to the left of all literals
	String[] split = sql.split("'", 2); 
	split[0] = split[0].replaceAll("select|SELECT", 
				       "SELECT " + joinTableName + "." +
				       ResultFactory.RESULT_TABLE_I + "," );
	return split.length == 1?
	    split[0] :
	    split[0] + "'" + split[1];
    }

    String addJoinTableToFrom(String sql) {
	// because quotes always come in pairs (even escaped ones)
	// we know that all non-literal sections must be in the 
	// odd elements of the array (where the first is odd)
	String[] split = sql.split("'"); 
	StringBuffer buf = new StringBuffer();
	for (int i = 0; i<split.length; i++) {
	    if (i%2 == 0) 
		split[i] = split[i].replaceAll("from|FROM", "FROM " + 
					       joinTableName + ",");
	    buf.append(split[i]);
	    if (i<split.length-2) buf.append("'");
	}
	//	System.err.println("\nfrom: " + sql + "\n" + buf.toString());
	return buf.toString();
    }

    String addConstraintsToWhere(String sql, int pageStartIndex, 
				 int pageEndIndex) throws WdkModelException {

	String macro = RecordClass.PRIMARY_KEY_MACRO; // shorter var. name

	// strip any quotes surrounding the primary key macro
	String regex = "\\S*(" + macro + ")\\S*";
	String newSql = sql.replaceAll(regex, "$1");
  
	// add AND clauses for page constraints
	String newline = System.getProperty("line.separator");
	String resultTableIndex = 
	    joinTableName + "." + ResultFactory.RESULT_TABLE_I;
	String andClause = 
	    newline + "AND " + resultTableIndex + " >= " + pageStartIndex +
	    newline + "AND " + resultTableIndex + " <= " + pageEndIndex;
	
	// case 1:  "blah = $$primaryKey$$"
	if (newSql.matches(".*=\\s*" + macro + ".*")) {
	    newSql = newSql.replaceAll("(" + macro + ")", 
				       "$1" + andClause );	    
	    
	// case 2:  "$$primaryKey$$ = blah"
	} else if (newSql.matches(".*" + macro + "\\s*=.*")) {
	    newSql = newSql.replaceAll("(" + macro + "\\s*=\\s*\\S+)", 
				       "$1" + andClause );	    
	    
	} else {
	    throw new WdkModelException("Invalid use of primary key macro in:"
					+ newline + sql);
	}

	// add order by at the end
	newSql = newSql + "\nORDER BY " + resultTableIndex;

	return newSql;
    }

    boolean containsSelect() {	
	// dodge literals by only considering the section to their left
	String[] split = origSql.substring(start, end+1).split("'", 2); 
	String regex = ".*select\\s+.*";
	return split[0].toLowerCase().matches(regex);

    }

    boolean containsFrom() {
	// dodge literals by considering only the odd pieces of the split
	String regex = ".*\\s+from\\s+.*";
	String[] split = origSql.substring(start, end+1).split("'"); 
	for (int i = 0; i<split.length; i+=2) 
	    if (split[i].toLowerCase().matches(regex)) return true;
	return false;
    }

    boolean containsPrimaryKey() {
	String regex = ".*" + RecordClass.PRIMARY_KEY_MACRO + ".*";
	return origSql.substring(start, end+1).matches(regex);
    }

    ///////////////////////////////////////////////////////////////////
    /////  private methods
    ///////////////////////////////////////////////////////////////////

    private boolean contains(String regex) {
	return origSql.substring(start, end+1).matches(regex);
    }

}

