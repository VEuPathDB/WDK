package org.gusdb.wdk.model.implementation;

public class SqlClausePiece {

    String origSql;

    //locations relative to original sql
    int start;
    int end;

    /**
     * A piece of sql that belongs to an sql clause and that has no kid
     * clauses.  If the sql clause has no kids, then the piece is its
     * full length (exluding bounding parens)
     *
     * @param start start of this piece (non-paren)
     * @param end end of this piece (non-paren), ie, index of last char + 1
     */
    public SqlClausePiece(String origSql, int start, int end, String joinTableName) {
	this.origSql = origSql;
	this.start = start;
	this.end = end;
    }

    String getFinalPieceSql(boolean needsFromFix) {
	String finalSql = origSql.substring(start, end);
	if (needsFromFix) finalSql = addJoinTableToFrom(finalSql);
	return finalSql;
    }

    String addJoinTableToFrom(String sql) {
	return sql.replaceAll("from|FROM", "from resultTable," );
    }

    ///////////////////////////////////////////////////////////////////
    /////  private methods
    ///////////////////////////////////////////////////////////////////

    boolean containsFrom() {
	
	boolean b = origSql.substring(start, end).toLowerCase().indexOf("from") != -1;
	return b;
    }

    boolean containsPrimaryKey() {
	
	return origSql.substring(start, end).indexOf(SqlClause.PRIMARY_KEY) != -1;
    }

}

