package org.gusdb.wdk.model.dataset;

import java.util.List;

public class FastaDatasetParser extends AbstractDatasetParser {
  
  private static final int SEQUENCE_LENGTH = 1999;
  private static final String PROP_ATTRIBUTES = "fasta.attributes";

  public FastaDatasetParser() {
    setName("fasta");
    setDisplay("FASTA");
    setDescription("The input is FASTA format. Only sequence of " + SEQUENCE_LENGTH + " bps or fewer will be parsed");
  }

  @Override
  public List<String[]> parse(String content) throws WdkDatasetException {
    // TODO Auto-generated method stub
    return null;
  }

  
}
