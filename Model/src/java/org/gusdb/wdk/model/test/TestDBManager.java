package org.gusdb.wdk.model.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.dbms.DBPlatform;
import org.gusdb.wdk.model.dbms.SqlUtils;
import org.gusdb.wdk.model.query.ColumnType;

public class TestDBManager {

    public static void main(String[] args) throws Exception {
        // process args
        Options options = declareOptions();
        String cmdName = System.getProperty("cmdName");
        CommandLine cmdLine = parseOptions(cmdName, options, args);

        String gusHome = System.getProperty(Utilities.SYSTEM_PROPERTY_GUS_HOME);
        String modelName = cmdLine.getOptionValue("model");

        WdkModel wdkModel = WdkModel.construct(modelName, gusHome);
        DBPlatform platform = wdkModel.getQueryPlatform();

        boolean drop = cmdLine.hasOption("drop");
        boolean create = cmdLine.hasOption("create");

        String tableDir;
        if (cmdLine.hasOption("tableDir")) {
            tableDir = cmdLine.getOptionValue("tableDir");
        } else {
            tableDir = "data/testTables";
        }
        String[] tables = getTableNames(gusHome + "/" + tableDir);

        if (drop == false && create == false) {// invalid option
            System.err.println("TestDBManager: user has not specified any "
                    + "database management operations");
            usage(cmdName, options);
        }

        for (int t = 0; t < tables.length; t++) {
            File nextTable = new File(tables[t]);
            if ("CVS".equals(nextTable.getName())) {
                continue;
            }

            BufferedReader reader = new BufferedReader(
                    new FileReader(nextTable));
            String firstLine = reader.readLine();
            String tableName = nextTable.getName();

            if (drop) {
                try {
                    System.err.println("Dropping table " + tableName);
                    dropTable(tableName, platform.getDataSource());
                } catch (SQLException ex) {
                    System.err.println("Dropping table '" + tableName
                            + "' failed.");
                    ex.printStackTrace();
                }
            }
            if (create) {
                try {
                    Map<String, String> columnTypes = createTable(tableName,
                            firstLine, platform);
                    Map<Integer, String> columnIds = new LinkedHashMap<Integer, String>();

                    // prepare the statement
                    StringBuffer sql = new StringBuffer("INSERT INTO ");
                    sql.append(tableName).append(" (");
                    StringBuffer sqlPiece = new StringBuffer();
                    int columnId = 0;
                    for (String column : columnTypes.keySet()) {
                        if (sqlPiece.length() > 0) {
                            sql.append(", ");
                            sqlPiece.append(", ");
                        }
                        sql.append(column);
                        sqlPiece.append("?");

                        columnIds.put(columnId++, column);
                    }
                    sql.append(") VALUES (").append(sqlPiece).append(")");
                    PreparedStatement ps = SqlUtils.getPreparedStatement(
                            platform.getDataSource(), sql.toString());

                    System.err.println("Loading table " + tableName
                            + " to database from file\n");
                    String nextLine;
                    while ((nextLine = reader.readLine()) != null) {
                        String[] parts = nextLine.split("\t", columnIds.size());
                        for (int i = 0; i < parts.length; i++) {
                            String nextValue = parts[i];
                            ColumnType type = ColumnType.parse(columnIds.get(i));

                            if (nextValue.trim().equals("")
                                    && !type.isText())
                                nextValue = "0";

                            if (!type.isText()) {
                                ps.setObject(i + 1, Integer.parseInt(nextValue));
                            } else {
                                ps.setObject(i + 1, nextValue);
                            }
                        }
                        ps.executeUpdate();
                    }
                    SqlUtils.closeStatement(ps);
                } catch (SQLException ex) {
                    System.err.println("Create table " + tableName + " failed");
                    ex.printStackTrace();
                }
            }
        }
    }

    private static String[] getTableNames(String tableDir) {
        File dir = new File(tableDir);
        File[] files = dir.listFiles();
        String[] tables = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            tables[i] = files[i].getAbsolutePath();
        }
        return tables;
    }

    private static Map<String, String> createTable(String tableName,
            String firstLine, DBPlatform platform) throws Exception {
        DataSource dataSource = platform.getDataSource();

        StringBuffer sql = new StringBuffer("CREATE TABLE ");
        sql.append(tableName).append(" (");

        // parse the first line, which holds the column definition
        String[] columns = firstLine.split(",");
        Map<String, String> columnTypes = new LinkedHashMap<String, String>();
        boolean firstColumn = true;
        for (String column : columns) {
            if (firstColumn) firstColumn = false;
            else sql.append(", ");

            String[] pieces = column.trim().split("\\s+");
            sql.append(pieces[0]).append(" "); // column name
            String type = pieces[1].trim().toLowerCase();
            if (type.startsWith("varchar") || type.startsWith("char")) {
                int quoteStart = type.indexOf('(');
                int quoteEnd = type.indexOf(')');
                int length = Integer.parseInt(type.substring(quoteStart + 1,
                        quoteEnd));
                sql.append(platform.getStringDataType(length));
            } else if (type.startsWith("number")) {
                int quoteStart = type.indexOf('(');
                int quoteEnd = type.indexOf(')');
                int length = Integer.parseInt(type.substring(quoteStart + 1,
                        quoteEnd));
                sql.append(platform.getNumberDataType(length));
            } else if (type.startsWith("clob")) {
                sql.append(platform.getClobDataType());
            } else {
                sql.append(type);
            }
            for (int i = 2; i < pieces.length; i++) {
                sql.append(" ").append(pieces[i]);
            }

            // decide the type
            if (type.startsWith("number")) {
                columnTypes.put(pieces[0], ColumnType.NUMBER.getType());
            } else {
                columnTypes.put(pieces[0], ColumnType.STRING.getType());
            }
        }
        sql.append(")");

        // System.err.println("creating test table with sql " + createTable);

        SqlUtils.executeUpdate(dataSource, sql.toString());

        return columnTypes;
    }

    private static void dropTable(String tableName, DataSource dataSource)
            throws Exception {

        String dropTable = "drop table " + tableName;
        SqlUtils.executeUpdate(dataSource, dropTable);
    }

    private static void addOption(Options options, String argName,
            boolean hasValue, boolean required, String desc) {

        Option option = new Option(argName, hasValue, desc);
        option.setRequired(required);
        option.setArgName(argName);

        options.addOption(option);
    }

    static Options declareOptions() {
        Options options = new Options();

        // model name
        addOption(options, "model", true, true, "the name of the model.  This "
                + "is used to find the config file "
                + "($GUS_HOME/config/model_name-config.xml)");

        // tableDir
        addOption(options, "tableDir", true, false, "the path to a directory "
                + "that contains the data files to be created at tables in the"
                + " database. The path can be absolute path (starts with '/'),"
                + " or relative path from $GUS_HOME (not starting with '/').");

        addOption(options, "drop", false, false, "Drop existing test database");

        addOption(options, "create", false, false, "Create new test database");

        return options;
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

    /**
     * As it currently stands, TestDBManager is called from the command line
     * with wdkTestDb. That file has its own command line arguments (different
     * from these) so this usage() method will not be called.
     */

    static void usage(String cmdName, Options options) {

        String newline = System.getProperty("line.separator");
        String cmdlineSyntax = cmdName + " -model model_name"
                + " tableDir <table_dir> [-create | -drop] ";

        String header = newline + "Parse flat files representing database "
                + "tables and insert into database." + newline + newline
                + "Options:";

        String footer = "";

        // PrintWriter stderr = new PrintWriter(System.err);
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp(70, cmdlineSyntax, header, options, footer);
        System.exit(1);
    }

}
