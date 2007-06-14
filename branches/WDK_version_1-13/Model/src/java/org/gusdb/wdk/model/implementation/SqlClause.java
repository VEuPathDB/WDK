package org.gusdb.wdk.model.implementation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.gusdb.wdk.model.RDBMSPlatformI;
import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.ResultFactory;
import org.gusdb.wdk.model.WdkModelException;

/**
 * An sql clause: a section of sql bounded by a parenthesis pair that may
 * contain other such sections (kids). The sections of sql within the clause
 * that are not within kid clauses are "pieces." A clause has K + 1 pieces where
 * K (>=0) is the number of kid clauses.
 * 
 * For example, the (contrived) sql: (select author from (select * from books)
 * where title = "war and peace")
 * 
 * Has two pieces: 'select author from ' ' where title = "war and peace' and one
 * kid: (select * from books)
 * 
 * The objective of this class is to: - recursively construct clauses found in
 * the sql - for each clause: - virtually assemble its pieces into a super-piece -
 * see if the super-piece contains both: - a FROM statement - a WHERE statement
 * that includes PRIMARY_KEY_NAME as a value - if such a pair is found, use the
 * helper class SqlClausePiece to add: - the join table's row index to the
 * SELECT statement - the JOIN_TABLE to the FROM statement - the page
 * constraints to the WHERE statement - the join table's row index a GROUP BY
 * statement, if present - if any of its kid clauses get the SELECT statement
 * modification apply that modification as well to the parent (propagating up) -
 * if the clause is the outermost one, wrap it in a SELECT * FROM (... ) ORDER
 * BY the join table's row index
 * 
 * Because literals never contain information we need for this analysis, and
 * because they can confuse things by containing "select" or "from", we excise
 * them at the beginning, and replace them at the end
 * 
 */
public class SqlClause {

    private String origSql;

    // locations relative to original sql
    private int open; // clause open paren
    private int close; // clause close paren

    private String joinTableName;
    private String projectColumn;
    private String primarykeyColumn;
    private boolean hadOuterParens = true;

    private SqlClausePiece fromPiece = null;
    private SqlClausePiece selectPiece = null;
    private SqlClausePiece groupByPiece = null;
    private SqlClausePiece primaryKeyPiece = null;
    private boolean hasPrimaryKey = false;

    private List<SqlClause> kids = new ArrayList<SqlClause>();
    private List<SqlClausePiece> pieces = new ArrayList<SqlClausePiece>();

    private static final String OPEN = "(";
    private static final String CLOSE = ")";
    private static final String WASLITERAL = "XX_WASLITERAL_XX";

    // used for the constraints for WHERE statement
    private int pageStartIndex;
    private int pageEndIndex;

    private String[] splitByQuote;

    private RDBMSPlatformI platform;

    private int sortingIndex;

    // public constructor
    public SqlClause(String origSql, String joinTableName,
            String projectColumn, String primaryKeyColumn, int sortingIndex,
            int pageStartIndex, int pageEndIndex, RDBMSPlatformI platform)
            throws WdkModelException {
        this(origSql, 0, joinTableName, projectColumn, primaryKeyColumn,
                sortingIndex, pageStartIndex, pageEndIndex, platform);
    }

    /**
     * @return the improved sql
     */
    // this method wraps getModifiedSqlSub to add the order by to the
    // outermost clause
    public String getModifiedSql() throws WdkModelException {
        hadOuterParens = false; // we put them in manually in this method

        String finalSql = getModifiedSqlSub();

        String newline = System.getProperty("line.separator");

        String resultTableIndex = ResultFactory.RESULT_TABLE_I;

        finalSql = "SELECT * FROM (" + newline + finalSql.trim() + newline
                + ") " + platform.getTableAliasAs() + "auto_wrapped_ "
                + newline + "ORDER BY " + resultTableIndex;

        return finalSql;
    }

    // /////////////////////////////////////////////////////////////////
    // /// private methods
    // /////////////////////////////////////////////////////////////////

    /**
     * recursively constructs kid clauses and the pieces that surround them
     * 
     * @param open index of clause open paren
     */
    private SqlClause(String origSql, int open, String joinTableName,
            String projectColumn, String primaryKeyColumn, int sortingIndex,
            int pageStartIndex, int pageEndIndex, RDBMSPlatformI platform)
            throws WdkModelException {
        if (open == 0) {
            origSql = exciseLiterals(origSql);
            origSql = validateParenStructure(origSql);
        }
        this.origSql = origSql;
        this.open = open;
        this.pageStartIndex = pageStartIndex;
        this.pageEndIndex = pageEndIndex;
        this.platform = platform;
        this.joinTableName = joinTableName;
        this.projectColumn = projectColumn;
        this.primarykeyColumn = primaryKeyColumn;
        this.sortingIndex = sortingIndex;
        findKidsAndPieces();
    }

    private int getOpen() {
        return open;
    }

    private int getClose() {
        return close;
    }

    private boolean getHasPrimaryKey() {
        return hasPrimaryKey;
    }

    // return an sql string in which all literals are replaced
    // by WASLITERAL. store the original sql, split by single quote
    // in splitByQuote
    private String exciseLiterals(String sql) throws WdkModelException {
        String newline = System.getProperty("line.separator");

        // strip any quotes surrounding the primary key macro
        String regex = "'" + RecordClass.PRIMARY_KEY_MACRO + "'";
        String newSql = sql.replaceAll(regex, RecordClass.PRIMARY_KEY_MACRO);

        // strip any quotes surrounding the project ID macro
        regex = "'" + RecordClass.PROJECT_ID_MACRO + "'";
        newSql = newSql.replaceAll(regex, RecordClass.PROJECT_ID_MACRO);

        if (newSql.indexOf("'") != -1) {
            boolean endsInQuote = newSql.lastIndexOf("'") == (newSql.length() - 1);
            if (endsInQuote) {
                newSql += " ";
            }
            splitByQuote = newSql.split("'");

            if (splitByQuote.length % 2 != 1) {
                throw new WdkModelException("Hello Odd number of quotes in: "
                        + newline + newSql);
            }
            StringBuffer buf = new StringBuffer(splitByQuote[0]);
            for (int i = 2; i < splitByQuote.length; i += 2) {
                buf.append(WASLITERAL + splitByQuote[i]);
            }
            newSql = buf.toString();
        }
        return newSql;
    }

    // restore literals to the sql that was fixed
    private String restoreLiterals(String sql) {
        String newSql = sql;

        if (newSql.indexOf(WASLITERAL) != -1) {
            String[] splitByWasLiteral = sql.split(WASLITERAL); // no literals
            StringBuffer buf = new StringBuffer();

            // iterate through original split by quotes
            // replace even elements with those in splitByWasLiteral
            // which contains only the non-literal sections
            int j = 0;
            for (int i = 0; i < splitByQuote.length; i += 1) {
                if (i % 2 == 0) buf.append(splitByWasLiteral[j++]);
                else buf.append("'" + splitByQuote[i] + "'");
            }
            newSql = buf.toString();
        }
        return newSql;
    }

    private String getClauseSql() {
        return origSql.substring(open, close + 1);
    }

    // on entry cursor points to open paren for this clause
    private void findKidsAndPieces() throws WdkModelException {

        int cursor = open;
        SqlClause kid;
        while ((kid = constructNextKid(cursor)) != null) {

            pieces.add(new SqlClausePiece(origSql, cursor + 1,
                    kid.getOpen() - 1, joinTableName, projectColumn,
                    primarykeyColumn, sortingIndex));
            kids.add(kid);
            hasPrimaryKey |= kid.getHasPrimaryKey();
            cursor = kid.getClose();
        }
        close = origSql.indexOf(CLOSE, cursor + 1);
        pieces.add(new SqlClausePiece(origSql, cursor + 1, close - 1,
                joinTableName, projectColumn, primarykeyColumn, sortingIndex));

        if (pieces.size() - kids.size() != 1) {
            throwException("Invalid sql. There are " + pieces.size()
                    + " pieces and " + kids.size() + " kids ", getClauseSql());
        }

        // initial pass to check pieces for From, PrimaryKey pair, and validate
        for (SqlClausePiece piece : pieces) {
            checkForSelect(piece);
            checkForFrom(piece);
            checkForPrimaryKey(piece);
            checkForGroupBy(piece);
        }
    }

    // cursor points to either this clause's open paren or prev kid close
    private SqlClause constructNextKid(int cursor) throws WdkModelException {

        int nextClose = origSql.indexOf(CLOSE, cursor + 1);
        int nextOpen = origSql.indexOf(OPEN, cursor + 1);

        if (nextOpen == -1 || nextOpen > nextClose) return null;// no more kids

        // start next kid to find
        return new SqlClause(origSql, nextOpen, joinTableName, projectColumn,
                primarykeyColumn, sortingIndex, pageStartIndex, pageEndIndex,
                platform);
    }

    private String validateParenStructure(String sql) throws WdkModelException {

        // never used
        // String newline = System.getProperty("line.separator");
        String errMsg = "Sql has invalid parentheses structure";

        char[] chars = sql.toCharArray();
        Stack<Object> parenStack = new Stack<Object>();
        Object o = new Object();

        if (chars[0] != '(') hadOuterParens = false;

        for (int i = 0; i < chars.length; i++) {
            if (chars[i] == '(') parenStack.push(o);
            if (chars[i] == ')') {
                if (parenStack.empty()) {
                    throwException(errMsg, sql);
                } else {
                    parenStack.pop();
                    if (parenStack.empty() && i < chars.length - 1)
                        hadOuterParens = false; // orig needs bounding parens
                }
            }
        }
        if (!parenStack.empty()) {
            throwException(errMsg, sql);
        }

        if (!hadOuterParens) sql = "(" + sql + ")";
        return sql;
    }

    private void checkForSelect(SqlClausePiece piece) throws WdkModelException {
        if (piece.containsSelect()) {
            if (selectPiece != null)
                throwException("Sql clause has too many SELECTs",
                        getClauseSql());
            selectPiece = piece;
        }
    }

    private void checkForFrom(SqlClausePiece piece) throws WdkModelException {
        if (piece.containsFrom()) {
            if (fromPiece != null)
                throwException("Sql clause has too many FROMs", getClauseSql());
            fromPiece = piece;
        }
    }

    private void checkForPrimaryKey(SqlClausePiece piece)
            throws WdkModelException {
        if (piece.containsPrimaryKey()) {
            if (primaryKeyPiece != null)
                throwException("Sql clause has too many "
                        + RecordClass.PRIMARY_KEY_NAME + "s", getClauseSql());
            if (fromPiece == null || selectPiece == null)
                throwException("Sql clause has a "
                        + RecordClass.PRIMARY_KEY_NAME
                        + " but no FROM or no SELECT", getClauseSql());
            primaryKeyPiece = piece;
            hasPrimaryKey = true;
        }
    }

    private void checkForGroupBy(SqlClausePiece piece) throws WdkModelException {
        if (piece.containsGroupBy()) {
            if (groupByPiece != null)
                throwException("Sql clause has too many GROUP BYs",
                        getClauseSql());
            groupByPiece = piece;
        }
    }

    // stitch together piece, clause, ..., piece
    // also modify Sql if needed:
    // - RESULT_TABLE_INDEX added to select
    // - join table added to its FROM statement
    // - page constraints and order by page index added to where statement
    private String getModifiedSqlSub() throws WdkModelException {

        Iterator<SqlClause> kidsIter = kids.iterator();
        StringBuffer buf = new StringBuffer();
        for (SqlClausePiece piece : pieces) {
            boolean needsSelectFix = selectPiece == piece && hasPrimaryKey;
            boolean needsFromFix = fromPiece == piece
                    && primaryKeyPiece != null;
            boolean needsWhereFix = primaryKeyPiece == piece;
            boolean needsGroupByFix = groupByPiece == piece && hasPrimaryKey;

            buf.append(piece.getFinalPieceSql(needsSelectFix, needsFromFix,
                    needsWhereFix, needsGroupByFix, pageStartIndex,
                    pageEndIndex));

            if (kidsIter.hasNext()) {
                SqlClause kid = kidsIter.next();
                buf.append(kid.getModifiedSqlSub());
            }
        }

        String finalSql = buf.toString();
        if (splitByQuote != null) finalSql = restoreLiterals(finalSql);

        finalSql = hadOuterParens ? "(" + finalSql + ")" : finalSql;

        return finalSql;
    }

    private void throwException(String msg, String sql)
            throws WdkModelException {
        String newline = System.getProperty("line.separator");
        throw new WdkModelException(msg + newline + "SQL: " + newline + sql
                + newline);
    }

    // /////////////////////////////////////////////////////////////////////////
    // static methods for testing
    // /////////////////////////////////////////////////////////////////////////

    public static void main(String[] args) {
        String cmdName = System.getProperties().getProperty("cmdName");
        String gusHome = System.getProperties().getProperty("gusHome");
        Options options = declareOptions();
        CommandLine cmdLine = parseOptions(cmdName, options, args);

        String sqlFileName = cmdLine.getOptionValue("sqlfile");

        if (sqlFileName == null)
            sqlFileName = gusHome + "/data/WDK/Model/sqlMungeTest.sql";

        File sqlFile = new File(sqlFileName);
        try {
            if (!sqlFile.exists() || !sqlFile.canRead()) {
                throw new WdkModelException("Can't open file '" + sqlFileName
                        + "' for reading");
            }

            ArrayList<String> testCases = parseSqlFile(sqlFile);
            for (String testCase : testCases) {
                System.out.println("====================================================");
                test(testCase);
            }

        } catch (WdkModelException e) {
            e.printStackTrace();
        }
    }

    /**
     * @return Array of sql queries to test
     */
    private static ArrayList<String> parseSqlFile(File sqlFile)
            throws WdkModelException {
        try {
            BufferedReader r = new BufferedReader(new FileReader(sqlFile));

            String newline = System.getProperty("line.separator");
            ArrayList<String> queriesArray = new ArrayList<String>();
            boolean withinComment = false;
            StringBuffer stringBuf = null;
            String line;
            while ((line = r.readLine()) != null) {
                validateLine(line);

                // dodge comment lines (w/in ^/* and ^*/, or starting with //)
                if (line.matches("/\\*.*")) withinComment = true;
                if (withinComment) {
                    withinComment &= !line.matches("\\*/.*");
                    continue;
                }
                if (line.matches("^//.*")) continue;

                // white space line delimits an sql
                if (line.matches("\\s*")) {
                    if (stringBuf != null)
                        queriesArray.add(stringBuf.toString());
                    stringBuf = null;
                } else {
                    if (stringBuf == null) stringBuf = new StringBuffer();
                    stringBuf.append(line + newline);
                }
            }
            if (stringBuf != null) queriesArray.add(stringBuf.toString());
            return queriesArray;
        } catch (IOException e) {
            throw new WdkModelException(e);
        }
    }

    private static void validateLine(String line) throws WdkModelException {
        if (line.matches(".+/\\*.*") || line.matches(".*/\\*.+")
                || line.matches(".+\\*/.*") || line.matches(".*\\*/.+"))
            throw new WdkModelException("/* and */ must be on their own line");
        if (line.matches(".+//.*"))
            throw new WdkModelException("// must be at the start of a line");
    }

    private static void test(String sql) throws WdkModelException {
        System.out.println("Testing: ");
        System.out.println(sql);
        System.out.println("");
        System.out.println("Result: ");

        RDBMSPlatformI platform = new PostgreSQL();
        int sortingIndex = 1;
        SqlClause clause = new SqlClause(sql, "Result_Table", null,
                "source_id", 1, 1, 20, platform);
        System.out.println(clause.getModifiedSql());
        System.out.println("");
    }

    // a number of the following methods should be factored somehow. they are
    // duplicated across many of the main() methods found in the Model
    static Options declareOptions() {
        Options options = new Options();

        addOption(
                options,
                "sqlfile",
                "The file containing the SQL to test. The default is '$GUS_HOME/data/WDK/Model/sqlMungeTest.sql', which contains the 'built-in' tests.  Please see that file for allowed syntax.  If you have an important test case, please add it to that file (in $PROJECT_HOME) and check it in.");
        return options;
    }

    private static void addOption(Options options, String argName, String desc) {

        Option option = new Option(argName, true, desc);
        option.setOptionalArg(true);
        option.setArgName(argName);

        options.addOption(option);
    }

    static CommandLine parseOptions(String cmdName, Options options,
            String[] args) {

        CommandLineParser parser = new BasicParser();
        CommandLine cmdLine = null;
        try {
            // parse the command line arguments
            cmdLine = parser.parse(options, args);
        } catch (ParseException exp) {
            // oops, something went wrong
            System.err.println("");
            System.err.println("Parsing failed.  Reason: " + exp.getMessage());
            System.err.println("");
            usage(cmdName, options);
        }

        return cmdLine;
    }

    static void usage(String cmdName, Options options) {

        String newline = System.getProperty("line.separator");
        String cmdlineSyntax = cmdName + " -sqlfile [fileName]";

        String header = newline
                + "Test the munging of a set of SQL Attributes Queries. "
                + newline + newline + "Options:";

        String footer = "";

        // PrintWriter stderr = new PrintWriter(System.err);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(75, cmdlineSyntax, header, options, footer);
        System.exit(1);
    }
}
