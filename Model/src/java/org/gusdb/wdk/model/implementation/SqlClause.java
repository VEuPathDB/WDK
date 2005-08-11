package org.gusdb.wdk.model.implementation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Stack;

import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.RecordClass;

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
 *          - a WHERE statement that includes PRIMARY_KEY_NAME as a value
 *      - if such a pair is found, use the helper class SqlClausePiece to add:
 *          - the join table's row index to the SELECT statement
 *          - the JOIN_TABLE to the FROM statement
 *          - the page constraints to the WHERE statement
 *          - the ORDER BY statement to order by the join table's row index
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
    private SqlClausePiece selectPiece = null;
    private SqlClausePiece primaryKeyPiece = null;

    private ArrayList kids = new ArrayList();
    private ArrayList pieces = new ArrayList();

    private static final String OPEN = "(";
    private static final String CLOSE = ")";

    // used for the constraints for WHERE statement
    private int pageStartIndex;
    private int pageEndIndex;

    // public constructor 
    public SqlClause (String origSql, String joinTableName, int pageStartIndex, int pageEndIndex) throws WdkModelException {
	this(origSql, 0, joinTableName, pageStartIndex, pageEndIndex);
    }

    // stitch together piece, clause, ..., piece
    // also modify Sql if needed:
    //  - RESULT_TABLE_INDEX added to select
    //  - join table added to its FROM statement
    //  - page constraints and order by page index added to where statement
    public String getModifiedSql() throws WdkModelException {

	if (pieces.size() - kids.size() != 1) {
	    throwException("Invalid sql. There are " + pieces.size() + 
			   " pieces and " + kids.size() + " kids ", 
			   getClauseSql());   
	}
	
	// initial pass to check pieces for From, PrimaryKey pair, and validate
	Iterator piecesIter = pieces.iterator();
	while (piecesIter.hasNext()) {
	    SqlClausePiece piece = (SqlClausePiece)piecesIter.next();
	    checkForSelect(piece);
	    checkForFrom(piece);
	    checkForPrimaryKey(piece);
	}

	piecesIter = pieces.iterator();
	Iterator kidsIter = kids.iterator();
	StringBuffer finalSql = new StringBuffer();
	while (piecesIter.hasNext()) {
	    SqlClausePiece piece = (SqlClausePiece)piecesIter.next();
	    boolean needsSelectFix = 
		selectPiece == piece && primaryKeyPiece != null;
	    boolean needsFromFix = 
		fromPiece == piece && primaryKeyPiece != null;
	    boolean needsWhereFix = primaryKeyPiece == piece;

	    finalSql.append(piece.getFinalPieceSql(needsSelectFix,
						   needsFromFix,
						   needsWhereFix,
						   pageStartIndex,
						   pageEndIndex));

	    if (kidsIter.hasNext()) {
		SqlClause kid = (SqlClause)kidsIter.next();
		finalSql.append(kid.getModifiedSql());
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
     * constructor (close unknown) 
     * recursively constructs kid clauses and the pieces that surround them
     * @param open index of clause open paren
     */ 
    private SqlClause (String origSql, int open, String joinTableName, int pageStartIndex, int pageEndIndex) throws WdkModelException {
	this.origSql = origSql;
	this.open = open;
	this.joinTableName = joinTableName;
	this.pageStartIndex = pageStartIndex;
	this.pageEndIndex = pageEndIndex;
	if (open == 0) validateParenStructure();
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
	
	return new SqlClause(origSql, nextOpen, joinTableName, pageStartIndex,
			     pageEndIndex); // start next kid to find
    }

    private void validateParenStructure() throws WdkModelException {
	
	String newline = System.getProperty("line.separator");
	String errMsg = "Sql has invalid parentheses structure";

	char[] chars = origSql.toCharArray();
	Stack parenStack = new Stack();
	Object o = new Object();

	if (chars[0] != '(') hadOuterParens = false;

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

    private void checkForSelect(SqlClausePiece piece) throws WdkModelException {
	if (piece.containsSelect()) {
	    if (selectPiece != null) 
		throwException("Sql clause has too many SELECTs",getClauseSql());
	    selectPiece = piece;
	}
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
		throwException("Sql clause has too many " + 
			       RecordClass.PRIMARY_KEY_NAME + "s",
			       getClauseSql());
	    if (fromPiece == null || selectPiece == null) 
		throwException("Sql clause has a " + 
			       RecordClass.PRIMARY_KEY_NAME + 
			       " but no FROM or no SELECT",
			       getClauseSql());
	    primaryKeyPiece = piece;
	}
    }

    private void throwException(String msg, String sql) throws WdkModelException {
	String newline = System.getProperty("line.separator");
	throw new WdkModelException(msg + newline + "SQL: " + newline +
				    sql + newline);
    }

    private static String[] testCases = 
    {
	"SELECT A, 'select ''from''' FROM B WHERE X = 'from' and B = '$$primaryKey$$'",

	"SELECT A, count(X), 'select ''from''' FROM (SELECT B FROM C WHERE D), E WHERE X = 'from' and F = '$$primaryKey$$'",

	"SELECT A FROM B WHERE X = 2 and B = $$primaryKey$$",
       
	"SELECT A FROM (SELECT B FROM C WHERE $$primaryKey$$ = D)",

	"(SELECT A FROM (SELECT B FROM C WHERE $$primaryKey$$ = D))",

	"SELECT A FROM (SELECT B FROM C WHERE D) WHERE $$primaryKey$$ = E",

	"SELECT A FROM B WHERE C IN (SELECT D FROM E) AND $$primaryKey$$ = E",

	"(SELECT A FROM B WHERE C IN (SELECT D FROM E) AND $$primaryKey$$ = E)",

	"(select A from B where C = $$primaryKey$$) union (select A from D where E = $$primaryKey$$)",

	"SELECT SUBSTR(g.source_id, 1, 1) FROM dots.genefeature g WHERE  g.source_id = '$$primaryKey$$'"    
    }; 
	
    public static void main(String[] args) {
	if (args.length == 1) {
	    test(args[0]);
	} else {
	    for (int i=0; i<testCases.length; i++) {
		System.out.println("====================================================");
		test(testCases[i]);
	    }
	}
	
    }

    private static void test(String sql) {
	try {
	    System.out.println("Testing: ");
	    System.out.println(sql);
	    System.out.println("");
	    System.out.println("Result: ");
	    SqlClause clause = new SqlClause(sql, "RESULT_TABLE", 1, 20);
	    System.out.println(clause.getModifiedSql());
	    System.out.println("");
	} catch (WdkModelException e) {
	    e.printStackTrace();
	}
    }
}

