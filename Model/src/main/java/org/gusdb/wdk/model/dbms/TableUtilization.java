package org.gusdb.wdk.model.dbms;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.iterator.IteratorUtil;
import org.gusdb.fgputil.runtime.GusHome;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.query.SqlQuery;
import org.json.JSONArray;
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

    // initialize data structures outside the try so we can close the model before dumping output
    // query -> tableName[]
    Map<String, List<String>> queryToTablesMap = new LinkedHashMap<>();
    // joinedTableString -> { tables, queries }
    Map<String, TwoTuple<List<String>, List<String>>> uniqueTableComboMap = new LinkedHashMap<>();

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
        String queryName = query.getFullName();
        String sql = query.getSql().toLowerCase();
        List<String> queryTables = new ArrayList<>();
        for (String tableName : tableNames) {
          if (sql.contains(tableName)) {
            queryTables.add(tableName);
          }
        }
        queryToTablesMap.put(queryName, queryTables);

        String joinedString = String.join("|", queryTables);
        uniqueTableComboMap.computeIfAbsent(joinedString, s -> new TwoTuple<>(queryTables, new ArrayList<>()));
        uniqueTableComboMap.get(joinedString).getSecond().add(queryName);
      }

      System.out.println("\nProcessed " + numSqlQueries + " SQL queries in model for " + projectId + "\n");
    }

    // dump out the map from queryName -> tableName[]
    System.out.println("Dumping map from query name to tables it uses.");
    dumpOutputFile(Paths.get("tableUsageMap.json"), new JSONObject(queryToTablesMap).toString(2));

    // other output file should be: { /id/: { tables: string[], queries: string[] }

    // make a list of unique table combinations; index will be the "ID" for that combo
    List<String> joinedStringList = new ArrayList<>(uniqueTableComboMap.keySet());
    joinedStringList.sort((a,b) -> a.compareTo(b));

    JSONArray uniqueTableCombosArray = new JSONArray();
    for (int i = 0; i < joinedStringList.size(); i++) {
      TwoTuple<List<String>,List<String>> data = uniqueTableComboMap.get(joinedStringList.get(i));
      uniqueTableCombosArray.put(new JSONObject()
          .put("id",  i)
          .put("tables", data.getFirst())
          .put("queries", data.getSecond()));
    }
    dumpOutputFile(Paths.get("uniqueTableGroups.json"), uniqueTableCombosArray.toString(2));
  }

  private static void dumpOutputFile(Path file, String content) throws IOException {
    try (BufferedWriter out = new BufferedWriter(new FileWriter(file.toFile()))) {
      out.write(content);
    }
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
