/**
 * 
 */
package org.gusdb.wdk.model.dataset;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.record.RecordClass;

/**
 * @author jerric
 * 
 */
public class GffDatasetParser extends AbstractDatasetParser {

  private static final String PROP_RECORD_TYPES = "gff.record.types";
  private static final String PROP_ATTRIBUTES = "gff.attributes";

  private static final Logger logger = Logger.getLogger(GffDatasetParser.class);

  public GffDatasetParser() {
    setName("gff");
    setDisplay("GFF");
    setDescription("The input is GFF file format. Only the record rows will be parsed.");
  }
  
  /*
   * (non-Javadoc)
   * 
   * @see org.gusdb.wdk.model.dataset.DatasetParser#parse(java.lang.String)
   */
  @Override
  public List<String[]> parse(String content) throws WdkDatasetException {
    Set<String> types = getRecordTypes();
    Map<String, Integer> attributes = getAttributes();
    List<String[]> data = new ArrayList<>();

    logger.debug("types: " + types);
    logger.debug("attributes: " + attributes);

    BufferedReader reader = new BufferedReader(new StringReader(content));
    String line;
    try {
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (line.length() == 0 || line.startsWith("#"))
          continue;
        if (line.startsWith(">")) // reaching sequence section, stop.
          break;
        String[] columns = line.split("\t");
        if (types.isEmpty() || types.contains(columns[2].toLowerCase())) {
          String[] row = new String[attributes.size() + 2];
          row[1] = columns[1];

          // parsing attributes
            for (String tuple : columns[8].split(";")) {
              String[] pieces = tuple.split("=");
              String attr = pieces[0].toLowerCase();
              if (attr.equals("id")) 
                row[0] = pieces[1];
              if (attributes.containsKey(attr)) {
                row[attributes.get(attr) + 2] = pieces[1];
              }
            }
          data.add(row);
        }
      }
    }
    catch (IOException ex) {
      throw new WdkDatasetException(ex);
    }

    return data;
  }

  /**
   * Look up the record type to be extracted from the GFF file. It will first check the properties provided by
   * the model author, if not present, will use the recordClass from the param; if none of them available,
   * will simply get all records.
   * 
   * all the types are converted to lower case.
   * 
   * @return a set with acceptable types; if the set is empty, will accept all types.
   */
  private Set<String> getRecordTypes() {
    String types = properties.get(PROP_RECORD_TYPES);
    Set<String> recordTypes = new HashSet<>();
    if (types == null) { // type is not specified, infer the types from record class
      RecordClass recordClass = param.getRecordClass();
      if (recordClass != null) {
        String type = recordClass.getDisplayName().trim().toLowerCase();
        recordTypes.add(type);
      } // else, no type specified.
    }
    else { // user specified types
      for (String type : types.split(",")) {
        type = type.trim().toLowerCase();
        if (type.length() > 0)
          recordTypes.add(type);
      }
    }
    return recordTypes;
  }

  /**
   * get the acceptable attribute names to be parsed. if the set is empty, don't parse any attribute.
   * 
   * the attribute names are converted to lower case.
   * 
   * @return
   * @throws WdkDatasetException
   */
  private Map<String, Integer> getAttributes() throws WdkDatasetException {
    String attrs = properties.get(PROP_ATTRIBUTES);
    Map<String, Integer> attributes = new HashMap<>();
    int i = 0;
    for (String attr : attrs.split(",")) {
      attr = attr.trim().toLowerCase();
      if (attr.length() > 0)
        attributes.put(attr, i++);
    }
    int allowedSize = DatasetFactory.MAX_VALUE_COLUMNS - 2;
    if (attributes.size() >= allowedSize)
      throw new WdkDatasetException("Only " + allowedSize + " attributes are allowed, but " +
          attributes.size() + " attributes are declared: " + attrs + " in datasetParam " +
          param.getFullName());
    return attributes;
  }
}
