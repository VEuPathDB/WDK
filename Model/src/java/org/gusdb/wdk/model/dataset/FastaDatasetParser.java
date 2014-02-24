package org.gusdb.wdk.model.dataset;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class FastaDatasetParser extends AbstractDatasetParser {

  private static final int SEQUENCE_LENGTH = 1999;

  public FastaDatasetParser() {
    setName("fasta");
    setDisplay("FASTA");
    setDescription("The input is FASTA format. Only sequence of " + SEQUENCE_LENGTH +
        " bps or fewer will be parsed");
  }

  @Override
  public List<String[]> parse(String content) throws WdkDatasetException {
    List<String[]> data = new ArrayList<>();

    BufferedReader reader = new BufferedReader(new StringReader(content));
    String line;
    try {
      StringBuilder defline = new StringBuilder();
      StringBuilder sequence = new StringBuilder();
      while ((line = reader.readLine()) != null) {
        if (line.startsWith(">")) { // start of a new record
          String[] row = processRow(defline.toString(), sequence.toString());
          if (row != null)
            data.add(row);
          defline = new StringBuilder(line);
          sequence = new StringBuilder();
        }
        else if (line.matches("^[\\S]+.*$")) { // a sequence line
          sequence.append(line);
        }
        else { // in defline
          defline.append(" ").append(line);
        }
      }
      String[] row = processRow(defline.toString(), sequence.toString());
      if (row != null)
        data.add(row);
    }
    catch (IOException ex) {
      throw new WdkDatasetException(ex);
    }
    return data;
  }

  private String[] processRow(String defline, String sequence) {
    if (defline.length() == 0) return null;
    
    defline = defline.substring(1); // remove leading '>'
    String[] columns = defline.split("\\|");
    
    int length = Math.min(DatasetFactory.MAX_VALUE_COLUMNS, columns.length + 1);
    String[] row = new String[length];
    // the first column is usually id column
    row[0] = columns[0].trim();
    // store the sequence in the second field
    if (sequence.length() < SEQUENCE_LENGTH) {
      row[1] = sequence;
    }
    // put the rest of the columns in
    for (int i = 2; i < row.length; i++) {
      row[i] = columns[i - 1].trim();
    }
    return row;
  }
}
