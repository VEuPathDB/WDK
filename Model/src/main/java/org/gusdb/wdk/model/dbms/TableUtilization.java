package org.gusdb.wdk.model.dbms;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import org.gusdb.fgputil.iterator.IteratorUtil;
import org.gusdb.fgputil.runtime.GusHome;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.query.SqlQuery;

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
    Map<String, List<String>> tables = readTablesFile(dbTableFile);
    System.out.println("Read " + tables.size() + " table names from tables file.");

    // build a model to get all queries
    try (WdkModel model = WdkModel.construct(projectId, GusHome.getGusHome())) {

      // gather sql queries into a stream
      Stream<SqlQuery> queries = Arrays.stream(model.getAllQuerySets())
          .flatMap(set -> Arrays.stream(set.getQueries()))
          .filter(query -> query instanceof SqlQuery)
          .map(query -> (SqlQuery)query);

      // check each query against each table in the tables file and record instances
      int numSqlQueries = 0;
      for (SqlQuery query : IteratorUtil.toIterable(queries.iterator())) {
        numSqlQueries++;
        String sql = query.getSql().toLowerCase();
        for (String table : tables.keySet()) {
          if (sql.contains(table)) {
            tables.get(table).add(query.getFullName());
          }
        }
      }

      // once complete, dump out the map
      System.out.println("Processed " + numSqlQueries + " SQL queries in model for " + projectId);
      for (Entry<String, List<String>> entry : tables.entrySet()) {
        System.out.println(entry.getKey() + " : [ " + String.join(", ", entry.getValue()) + " ]");
      }
    }
  }

  private static Map<String, List<String>> readTablesFile(Path dbTableFile) throws FileNotFoundException, IOException {
    try (BufferedReader in = new BufferedReader(new FileReader(dbTableFile.toFile()))) {
      Map<String, List<String>> tables = new LinkedHashMap<>();
      while (in.ready()) {
        tables.put(in.readLine().trim().toLowerCase(), new ArrayList<>());
      }
      return tables;
    }
  }
}
