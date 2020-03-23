package org.gusdb.wdk.model.dataset;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.record.RecordClass;

import java.util.*;
import java.util.regex.Pattern;

/**
 * @author jerric
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

  @Override
  public DatasetIterator iterator(final DatasetContents contents) {
    return new Iterator(contents);
  }

  private class Iterator extends AbstractDatasetIterator {
    private final Pattern COL_DIV_PAT = Pattern.compile("\t");

    Iterator(DatasetContents contents) {
      super(contents, "\r\n?|\n");
    }

    @Override
    protected boolean rowFilter(final String row) {
      if (row.isEmpty() || row.charAt(0) != '#')
        return false;

      var types = getRecordTypes();

      if (types.isEmpty())
        return true;

      return types.contains(COL_DIV_PAT.split(row)[2].toLowerCase());
    }

    @Override
    protected boolean atEndOfInput(final String row) {
      return row.charAt(0) == '>';
    }

    @Override
    protected String[] parseRow(final String row) throws WdkUserException {
      var types      = getRecordTypes();
      var attributes = getAttributes();

      logger.debug("types: " + types);
      logger.debug("attributes: " + attributes);

      var columns = COL_DIV_PAT.split(row);
      var out     = new String[attributes.size() + 2];
      out[1] = columns[1];
      for (var tuple : columns[8].split(";")) {
        var pieces = tuple.split("=");
        var attr = pieces[0].toLowerCase();

        if (attr.equals("id"))
          out[0] = pieces[1];

        if (attributes.containsKey(attr))
          out[attributes.get(attr) + 2] = pieces[1];
      }

      return out;
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
      Optional<RecordClass> recordClass = param.getRecordClass();
      if (recordClass.isPresent()) {
        String type = recordClass.get().getDisplayName().trim().toLowerCase();
        recordTypes.add(type);
      } // else, no type specified.
    }
    else { // user specified types
      for (String type : types.split(",")) {
        type = type.trim().toLowerCase();
        if (!type.isEmpty())
          recordTypes.add(type);
      }
    }
    return recordTypes;
  }

  /**
   * get the acceptable attribute names to be parsed.
   *
   * if the set is empty, don't parse any attribute.
   *
   * the attribute names are converted to lower case.
   */
  private Map<String, Integer> getAttributes() throws WdkUserException {
    String attrs = properties.get(PROP_ATTRIBUTES);
    Map<String, Integer> attributes = new HashMap<>();
    int i = 0;
    for (String attr : attrs.split(",")) {
      attr = attr.trim().toLowerCase();
      if (!attr.isEmpty())
        attributes.put(attr, i++);
    }
    int allowedSize = DatasetFactory.MAX_VALUE_COLUMNS - 2;
    if (attributes.size() >= allowedSize)
      throw new WdkUserException(
        "Only " + allowedSize + " attributes are allowed, but "
          + attributes.size() + " attributes are declared: " + attrs
          + " in datasetParam " + param.getFullName()
      );
    return attributes;
  }
  }
}
