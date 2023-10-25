package org.gusdb.wdk.model.dbms;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.gusdb.fgputil.iterator.IteratorUtil;
import org.gusdb.fgputil.runtime.GusHome;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.query.SqlQuery;
import org.json.JSONObject;

public class TableUtilization {

  public static void main(String[] args) throws Exception {

    // some minimal arg validation
    if (args.length != 2) {
      System.err.println("USAGE: TableUtilization <projectId> <tableFile>\n");
      System.err.println("  projectId: projectId used to find config files");
      System.err.println("  tableFile: location of file containing newline delimited list of tables to grep queries for\n");
      System.exit(1);
    }

    // pull out args
    String projectId = args[0];
    Path dbTableFile = Paths.get(args[1]);

    // read table file, initializing recording map to empty lists
    List<String> tableNames = readTablesFile(dbTableFile);
    System.out.println("\nGathered " + tableNames.size() + " table names from tables file.\n");

    // initialize data structures outsize the try so we can close the model before dumping output
    Map<String, List<String>> queryToTablesMap = new LinkedHashMap<>(); // ordered query map (query -> tableName[])
    Map<String, String> queryToJoinedStringMap = new LinkedHashMap<>(); // another ordered query map (query -> join(tableName[])
    //Map<String, List<String>> joinedStringToQueriesMap = new LinkedHashMap<>();

    // build a model to get all queries
    try (WdkModel model = WdkModel.construct(projectId, GusHome.getGusHome())) {

      // gather sql queries into a stream
      Stream<SqlQuery> queries = Arrays.stream(model.getAllQuerySets())
          .flatMap(set -> Arrays.stream(set.getQueries()))
          .filter(query -> query instanceof SqlQuery)
          .map(query -> (SqlQuery)query);

      // check each query against each table in the tables file to find matches
      int numSqlQueries = 0;
      for (SqlQuery query : IteratorUtil.toIterable(queries.iterator())) {
        numSqlQueries++;
        String sql = query.getSql().toLowerCase();
        List<String> queryTables = new ArrayList<>();
        for (String tableName : tableNames) {
          if (sql.contains(tableName)) {
            queryTables.add(tableName);
          }
        }
        queryToTablesMap.put(query.getFullName(), queryTables);
        queryToJoinedStringMap.put(query.getFullName(), String.join("|", queryTables));
      }

      System.out.println("\nProcessed " + numSqlQueries + " SQL queries in model for " + projectId + "\n)");
    }

    // dump out the map from queryName -> tableName[]
    System.out.println("Map from query name to tables it uses: " + new JSONObject(queryToTablesMap).toString(2));

    // make a list of unique table combinations; index will be the "ID" for that combo
    List<String> joinedStringList = new ArrayList<>(new HashSet<>(queryToJoinedStringMap.values()));
    joinedStringList.sort((a,b) -> a.compareTo(b));

    // dump unique table combinations
    System.out.println("Unique table combinations by index:");
    for (int i = 0; i < joinedStringList.size(); i++) {
      // find how many queries use each combination
      int count = 0;
      for (String joinedTableString : queryToJoinedStringMap.values()) {
        if (joinedStringList.get(i).equals(joinedTableString)) {
          count++;
        }
      }
      System.out.println(i + " (" + count + "): " + joinedStringList.get(i));
    }

    // desired output:
    //   list of table collections found in queries, so
    //   1. start with map of query -> table[]
    //   2. use to build ID -> table[] (unique collection of tables)
    //   3. produce map of query -> ID, sort by IDs

  }

  private static List<String> readTablesFile(Path dbTableFile) throws FileNotFoundException, IOException {
    try (BufferedReader in = new BufferedReader(new FileReader(dbTableFile.toFile()))) {
      List<String> tableNames = new ArrayList<>();
      while (in.ready()) {
        tableNames.add(in.readLine().trim().toLowerCase());
      }
      return tableNames;
    }
  }
}
