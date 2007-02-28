package org.gusdb.wdk.model.implementation;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.gusdb.wdk.model.RecordClass;
import org.gusdb.wdk.model.ResultFactory;
import org.gusdb.wdk.model.WdkModelException;

public class SqlClausePiece {

    String origSql;
    String joinTableName;
    // locations relative to original sql
    int start;
    int end;

    private int sortingIndex;
    private String projectColumn;
    private String primaryKeyColumn;

    /**
     * A piece of sql that belongs to an sql clause and that has no kid clauses.
     * If the sql clause has no kids, then the piece is its full length
     * (exluding bounding parens)
     * 
     * @param start index of the start of this piece (non-paren)
     * @param end index of end of this piece (non-paren), ie, index of last char
     */
    public SqlClausePiece(String origSql, int start, int end,
            String joinTableName, String projectColumn,
            String primaryKeyColumn, int sortingIndex) {
        this.origSql = origSql;
        this.start = start;
        this.end = end;
        this.joinTableName = joinTableName;
        this.sortingIndex = sortingIndex;
        this.primaryKeyColumn = primaryKeyColumn;
        this.projectColumn = projectColumn;
    }

    String getFinalPieceSql(boolean needsSelectFix, boolean needsFromFix,
            boolean needsWhereFix, boolean needsGroupByFix, int pageStartIndex,
            int pageEndIndex) throws WdkModelException {

        String finalSql = origSql.substring(start, end + 1);
        if (needsSelectFix) finalSql = addJoinTableIndexToSelect(finalSql);
        if (needsFromFix) finalSql = addJoinTableToFrom(finalSql);
        if (needsWhereFix)
            finalSql = addConstraintsToWhere(finalSql, pageStartIndex,
                    pageEndIndex);
        if (needsGroupByFix) finalSql = addJoinTableIndexToGroupBy(finalSql);

        return finalSql;
    }

    String getSql() {
        return origSql.substring(start, end + 1);
    }

    String addJoinTableIndexToSelect(String sql) {
        String regex = "\\b(select)\\b";
        int flag = Pattern.CASE_INSENSITIVE;
        String cacheTable = (sortingIndex == 0)
                ? joinTableName
                : "sorting_cache";
        String replace = "$1 " + cacheTable + "."
                + ResultFactory.RESULT_TABLE_I + ",";

        return Pattern.compile(regex, flag).matcher(sql).replaceAll(replace);
    }

    String addJoinTableToFrom(String sql) {
        String regex = "\\b(from)\\b";
        int flag = Pattern.CASE_INSENSITIVE;
        String replace = " ";
        if (sortingIndex != 0) // add secondary sorting cache
            replace += joinTableName + " sorting_cache, ";
        replace += joinTableName;

        // return Pattern.compile(regex, flag).matcher(sql).replaceAll(replace);
        Matcher matcher = Pattern.compile(regex, flag).matcher(sql);
        int prev = 0;
        StringBuffer result = new StringBuffer();
        while (matcher.find()) {
            String before = sql.substring(prev, matcher.start());
            String remain = sql.substring(matcher.end());
            remain = remain.trim().toLowerCase();
            remain = remain.split("\\s+")[0];
            result.append(before);
            result.append(" FROM ");
            result.append(replace);
            if (!remain.equals("where") && !remain.equals("order") && !remain.equals("group")) {
                result.append(",");
            }
            result.append(" ");
            prev = matcher.end();
        }
        result.append(sql.substring(prev));
        return result.toString();
    }

    String addConstraintsToWhere(String sql, int pageStartIndex,
            int pageEndIndex) throws WdkModelException {

        String macro = RecordClass.PRIMARY_KEY_MACRO; // shorter var. name

        // add AND clauses for page constraints
        String newline = System.getProperty("line.separator");
        String resultTableIndex = ResultFactory.RESULT_TABLE_I;
        String sortingIndexColumn = ResultFactory.COLUMN_SORTING_INDEX;

        // append paging and sorting index
        StringBuffer sbAnd = new StringBuffer();
        if (sortingIndex != 0) { // use secondary sorting cache
            sbAnd.append(newline + "AND sorting_cache." + resultTableIndex
                    + " >= " + pageStartIndex);
            sbAnd.append(newline + "AND sorting_cache." + resultTableIndex
                    + " <= " + pageEndIndex);
            sbAnd.append(newline + "AND sorting_cache." + sortingIndexColumn
                    + " = " + sortingIndex);
            if (projectColumn != null)
                sbAnd.append(newline + "AND sorting_cache." + projectColumn
                        + " = " + joinTableName + "." + projectColumn);
            sbAnd.append(newline + "AND sorting_cache." + primaryKeyColumn
                    + " = " + joinTableName + "." + primaryKeyColumn);
            sbAnd.append(newline + "AND " + joinTableName + "."
                    + sortingIndexColumn + " = 0" + newline);
        } else {
            sbAnd.append(newline + "AND " + joinTableName + "."
                    + resultTableIndex + " >= " + pageStartIndex);
            sbAnd.append(newline + "AND " + joinTableName + "."
                    + resultTableIndex + " <= " + pageEndIndex);
            sbAnd.append(newline + "AND " + joinTableName + "."
                    + sortingIndexColumn + " = " + sortingIndex + newline);
        }
        String andClause = sbAnd.toString();

        String newSql = sql;
        int flag = Pattern.DOTALL;
        // case 1: "blah = $$primaryKey$$"
        if (Pattern.compile(".*=\\s*" + macro + ".*", flag).matcher(newSql).matches()) {
            newSql = newSql.replaceAll("(" + macro + ")", "$1" + andClause);

            // case 2: "$$primaryKey$$ = blah"
        } else if (Pattern.compile(".*" + macro + "\\s*=.*", flag).matcher(
                newSql).matches()) {
            newSql = newSql.replaceAll("(" + macro + "\\s*=\\s*\\S+)", "$1"
                    + andClause);
        } else {
            throw new WdkModelException("Invalid use of primary key macro in:"
                    + newline + sql);
        }

        return newSql;
    }

    String addJoinTableIndexToGroupBy(String sql) {
        String regex = "\\b(group\\s+by)\\b";
        int flag = Pattern.CASE_INSENSITIVE;
        String cacheTable = (sortingIndex == 0)
                ? joinTableName
                : "sorting_cache";
        String replace = "$1 " + cacheTable + "."
                + ResultFactory.RESULT_TABLE_I + ",";

        return Pattern.compile(regex, flag).matcher(sql).replaceAll(replace);
    }

    boolean containsSelect() {
        return contains(".*\\bselect\\b.*", Pattern.CASE_INSENSITIVE);
    }

    boolean containsFrom() {
        return contains(".*\\bfrom\\b.*", Pattern.CASE_INSENSITIVE);
    }

    boolean containsPrimaryKey() {
        String regex = ".*" + RecordClass.PRIMARY_KEY_MACRO + ".*";
        return contains(regex, 0);
    }

    boolean containsGroupBy() {
        return contains(".*\\bgroup\\s+by\\b.*", Pattern.CASE_INSENSITIVE);
    }

    boolean containsOrderBy() {
        return contains(".*\\border\\s+by\\b.*", Pattern.CASE_INSENSITIVE);
    }

    // ////////////////////////////////////////////////////////////////
    // private methods
    // ////////////////////////////////////////////////////////////////

    private boolean contains(String regex, int caseFlag) {
        int flag = Pattern.DOTALL | caseFlag;
        return Pattern.compile(regex, flag).matcher(
                origSql.substring(start, end + 1)).matches();
    }
}
