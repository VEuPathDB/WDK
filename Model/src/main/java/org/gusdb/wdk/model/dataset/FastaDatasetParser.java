package org.gusdb.wdk.model.dataset;

import org.gusdb.wdk.model.WdkUserException;

import java.util.regex.Pattern;

@SuppressWarnings("unused") // used reflectively
public class FastaDatasetParser extends AbstractDatasetParser {

  private static final int SEQUENCE_LENGTH = 1999;

  public FastaDatasetParser() {
    setName("fasta");
    setDisplay("FASTA");
    setDescription("The input is FASTA format. Only sequence of "
      + SEQUENCE_LENGTH + " bps or fewer will be parsed");
  }

  @Override
  public DatasetIterator iterator(DatasetContents contents) {
    return new Iterator(contents);
  }


  private static class Iterator extends AbstractDatasetIterator {
    private final static Pattern COL_SPLIT_PAT = Pattern.compile("\\|");

    public Iterator(DatasetContents contents) {
      super(contents, ">");
    }

    @Override
    protected String[] parseRow(final String row) {
      var tmp = splitHeader(row);
      return processRow(tmp[0], tmp[1]);
    }

    // Silently skips sequences without headers as per previous implementation.
    @Override
    protected boolean rowFilter(final String row) throws WdkUserException {
      if (!super.rowFilter(row))
        return false;

      return splitHeader(row)[0] != null;
    }

    private String[] processRow(final String defline, final String sequence) {
      var columns = COL_SPLIT_PAT.split(defline);

      int length = Math.min(DatasetFactory.MAX_VALUE_COLUMNS, columns.length + 1);
      var row = new String[length];
      // the first column is usually id column
      row[0] = columns[0].trim();
      // store the sequence in the second field
      if (sequence.length() < SEQUENCE_LENGTH) {
        row[1] = sequence;
      }
      // put the rest of the columns in
      for (int i = 2, j = 1; j < row.length; i++, j++) {
        row[i] = columns[j].trim();
      }
      return row;
    }

    private String[] splitHeader(final String row) {
      var out = new String[2];
      for (var i = 0; i < row.length(); i++) {
        var c = row.charAt(i);
        switch (c) {
          case '\n':
            out[0] = row.substring(0, i).trim();
            out[1] = row.substring(i+1).trim();
            return out;
          case '\r':
            out[0] = row.substring(0, i).trim();
            out[1] = (i + 1 < row.length() && row.charAt(i + 1) == '\n')
              ? row.substring(i + 2).trim()
              : row.substring(i + 1).trim();
            return out;
        }
      }
      return out;
    }

  }
}
