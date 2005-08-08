package org.gusdb.wdk.model.implementation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

import org.gusdb.wdk.model.WdkModelException;

/**
 * An sql clause: a section of sql bounded by a parenthesis pair that 
 * may contain other such sections (kids).  The sections of sql within the
 * clause that are not within kid clauses are "pieces."   A clause has
 * K + 1 pieces where K (>=0) is the number of kid clauses.  
 *
 * For example, the (contrived) sql:
 *   (select author from (select * from books) where title = "war and peace")
 *
 * Has two pieces:
 *  'select author from '
 *  ' where title = "war and peace'
 * and one kid:
 *  (select * from books)
 *
 * The objective of this class is to:
 *  - recursively construct clauses found in the sql
 *  - for each clause:
 *      - virtually assemble its pieces into a super-piece
 *      - see if the super-piece contains both:
 *          - a FROM statement
 *          - a WHERE statement that includes PRIMARY_KEY as a value
 *      - if such a pair is found add the JOIN_TABLE to the FROM statement
 *
 */
public class SqlClause {

    private String origSql;

    //locations relative to original sql
    private int open;   // clause open paren
    private int close;     // clause close paren

    private String joinTableName;
    private boolean hadOuterParens = true;

    private SqlClausePiece fromPiece = null;
    private SqlClausePiece primaryKeyPiece = null;

    private ArrayList kids = new ArrayList();
    private ArrayList pieces = new ArrayList();

    private static final String OPEN = "(";
    private static final String CLOSE = ")";

    static final String PRIMARY_KEY = "primaryKey";

    // public constructor 
    public SqlClause (String origSql, String joinTableName) throws WdkModelException {
	this.origSql = origSql;
	this.open = 0;
	this.joinTableName = joinTableName;

	validateParenStructure();
	findKidsAndPieces();
    }

    // stitch together piece, clause, ..., piece
    // also fix piece if it needs the join table added to its FROM statement
    public String getFinalClauseSql() throws WdkModelException {

	if (pieces.size() - kids.size() != 1) {
	    throwException("Invalid sql. There are " + pieces.size() + 
			   " pieces and " + kids.size() + " kids ", 
			   getClauseSql());   
	}
	
	// initial pass to check pieces for From, PrimaryKey pair, and validate
	Iterator piecesIter = pieces.iterator();
	while (piecesIter.hasNext()) {
	    SqlClausePiece piece = (SqlClausePiece)piecesIter.next();
	    checkForFrom(piece);
	    checkForPrimaryKey(piece);
	}

	piecesIter = pieces.iterator();
	Iterator kidsIter = kids.iterator();
	StringBuffer finalSql = new StringBuffer();
	while (piecesIter.hasNext()) {
	    SqlClausePiece piece = (SqlClausePiece)piecesIter.next();
	    boolean needsFromFix = 
		fromPiece == piece && primaryKeyPiece != null;

	    finalSql.append(piece.getFinalPieceSql(needsFromFix));

	    if (kidsIter.hasNext()) {
		SqlClause kid = (SqlClause)kidsIter.next();
		finalSql.append(kid.getFinalClauseSql());
	    }
	}

	return hadOuterParens? 
	    "(" + finalSql.toString() + ")":
	    finalSql.toString();
    }

    ///////////////////////////////////////////////////////////////////
    /////  private methods
    ///////////////////////////////////////////////////////////////////

    private int getOpen() { return open; }
    private int getClose() { return close; }
    private String getClauseSql() { return origSql.substring(open, close+1); }
	
    /**
     * constructor if close known, which means this clause is a leaf 
     * (ie, it has no kid clauses)
     * @param open index of clause open paren
     * @param close index of clause close paren
     */ 
    private SqlClause(String origSql, int open, int close) {
	this.origSql = origSql;
	this.open = open;
	this.close = close;
	pieces.add(new SqlClausePiece(origSql, open+1, close-1,joinTableName));
    }

    /**
     * constructor if close unknown (because there is one or more kid clauses).
     * recursively constructs kid clauses and the pieces that surround them
     * @param open index of clause open paren
     */ 
    private SqlClause (String origSql, int open) throws WdkModelException {

	this.origSql = origSql;
	this.open = open;
	findKidsAndPieces();
    }

    // on entry cursor points to open paren for this clause
    private void findKidsAndPieces() throws WdkModelException {

	int cursor = open;
	SqlClause kid;
	while ((kid = constructNextKid(cursor)) != null) {

	    pieces.add(new SqlClausePiece(origSql, cursor+1, kid.getOpen()-1,
					  joinTableName));
	    kids.add(kid);
	    cursor = kid.getClose();
	}
	close = origSql.indexOf(CLOSE, cursor+1); 
	pieces.add(new SqlClausePiece(origSql,cursor+1,close-1,joinTableName));
    }

    // cursor points to either this clause's open paren or prev kid close
    private SqlClause constructNextKid(int cursor) throws WdkModelException {
	
	int nextClose = origSql.indexOf(CLOSE, cursor+1);
	int nextOpen = origSql.indexOf(OPEN, cursor+1);

	if (nextOpen == -1 || nextOpen > nextClose) return null;// no more kids
	
	return new SqlClause(origSql, nextOpen); // start next kid to find
    }

    private void validateParenStructure() throws WdkModelException {
	
	String newline = System.getProperty("line.separator");
	String errMsg = "Sql has invalid parentheses structure";

	char[] chars = origSql.toCharArray();
	Stack parenStack = new Stack();
	Object o = new Object();
	for (int i =0; i<chars.length; i++) {
	    if (chars[i] == '(') parenStack.push(o);
	    if (chars[i] == ')') {
		if (parenStack.empty()) {
		    throwException(errMsg, origSql);
		} else {
		    parenStack.pop();
		    if (parenStack.empty() && i < chars.length-1)
			hadOuterParens = false;  // orig needs bounding parens
		}
	    }
	}
	if (!parenStack.empty()) {
	    throwException(errMsg, origSql);
	}

	if (!hadOuterParens) origSql = "(" + origSql + ")";
    }

    private void checkForFrom(SqlClausePiece piece) throws WdkModelException {
	if (piece.containsFrom()) {
	    if (fromPiece != null) 
		throwException("Sql clause has too many FROMs",getClauseSql());
	    fromPiece = piece;
	}
    }

    private void checkForPrimaryKey(SqlClausePiece piece) throws WdkModelException {
	if (piece.containsPrimaryKey()) {
	    if (primaryKeyPiece != null) 
		throwException("Sql clause has too many " + PRIMARY_KEY + "s",
			       getClauseSql());
	    if (fromPiece == null) 
		throwException("Sql clause has a "+PRIMARY_KEY+" but no FROM",
			       getClauseSql());
	    primaryKeyPiece = piece;
	}
    }

    private void throwException(String msg, String sql) throws WdkModelException {
	String newline = System.getProperty("line.separator");
	throw new WdkModelException(msg + newline + "SQL: " + newline +
				    sql + newline);
    }

    public static void main(String[] args) {

	try {
	    SqlClause clause = new SqlClause(args[0], args[1]);
	    System.out.println(clause.getFinalClauseSql());
	} catch (WdkModelException e) {
	    e.printStackTrace();
	}

    }
}

