package org.gusdb.wdk.model.query.param;

import static java.util.Arrays.asList;
import static org.gusdb.fgputil.functional.Functions.filter;
import static org.gusdb.fgputil.functional.Functions.mapToList;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.dbms.ResultList;

public enum OntologyItemType {

  // special type to indicate an ontology branch node; no data is present in branch nodes
  BRANCH (null, null, BranchNode.class),

  // special type to indicated this node should be a multiFilter
  MULTIFILTER   ("multiFilter",   null,   MultiFilter.class),

  // data types of ontology leaf nodes
  // (ontology_type_declaration_string, metadata_query_column_name, java_parser_class)
  STRING ("string", "string_value", String.class),
  NUMBER ("number", "number_value", Double.class),
  DATE   ("date",   "date_value",   String.class);
 

  private static class BranchNode{}
  private static class MultiFilter{}

  private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat(FormatUtil.STANDARD_DATE_FORMAT_DASH);

  private final String _identifier;
  private final String _metadataQueryColumn;
  private final Class<?> _javaClass;

  private OntologyItemType(String identifier, String metadataQueryColumn, Class<?> javaClass) {
    _identifier = identifier;
    _metadataQueryColumn = metadataQueryColumn;
    _javaClass = javaClass;
  }

  public String getIdentifier() {
    return _identifier;
  }

  public String getMetadataQueryColumn() {
    return _metadataQueryColumn;
  }

  public Class<?> getJavaClass() {
    return _javaClass;
  }

  public static OntologyItemType getType(String typeIdentifier) throws WdkModelException {
    if (typeIdentifier == null) {
      return BRANCH;
    }
    for (OntologyItemType type : normalValues()) {
      if (type._identifier.equalsIgnoreCase(typeIdentifier)) {
        return type;
      }
    }
    throw new WdkModelException("Unrecognized ontology item type: " + typeIdentifier);
  }

  private static List<OntologyItemType> normalValues() {
    return filter(asList(values()), value -> !value.equals(BRANCH));
  }

  public static List<String> getTypedValueColumnNames() {
    List<OntologyItemType> values = normalValues();
    values.remove(MULTIFILTER); // has no associated column
    return mapToList(values, type -> type._metadataQueryColumn);
  }

  @SuppressWarnings("unchecked")
  public static <T> T resolveTypedValue(ResultSet resultSet, OntologyItem ontologyItem,
      Class<T> ontologyItemClass) throws SQLException {
    
    if (!ontologyItem.getType().getJavaClass().getName().equals(ontologyItemClass.getName())) {
      throw new IllegalStateException("Incoming ontologyItemClass (" + ontologyItemClass.getName() +
          ") must be the same as that configured for the ontologyItem's value Java class (" +
          ontologyItem.getType().getJavaClass().getName() + ").  See ontology item '" + ontologyItem.getOntologyId() + "'.");
    }
    Object value = null;
    switch(ontologyItem.getType()) {
      case BRANCH:
        throw new IllegalStateException("Error trying to get a metadata value for ontology term '" + ontologyItem.getOntologyId() + "'." + 
      " That term is either a branch in the ontology, or is a leaf but with a null type");
      case NUMBER:
        value = resultSet.getDouble(OntologyItemType.NUMBER.getMetadataQueryColumn()); break;
      case STRING:
        value = resultSet.getString(OntologyItemType.STRING.getMetadataQueryColumn()); break;
      case DATE:
        Date dateValue = resultSet.getDate(OntologyItemType.DATE.getMetadataQueryColumn());
        value = formatDate(dateValue);
        break;
      case MULTIFILTER:
        break;
      default:
        break;
    }
    if (resultSet.wasNull()) {
      value = null;
    }
    return (T) value;
  }
  
  public static String getStringValue(ResultList resultList, OntologyItem ontologyItem) throws SQLException, WdkModelException {
    
    Object value = null;
    switch(ontologyItem.getType()) {
      case BRANCH:
        throw new IllegalStateException("Error trying to get a metadata value for ontology term '" + ontologyItem.getOntologyId() + "'." + 
      " That term is either a branch in the ontology, or is a leaf but with a null type");
      case NUMBER:
        value = resultList.get(OntologyItemType.NUMBER.getMetadataQueryColumn()); break;
      case STRING:
        value = resultList.get(OntologyItemType.STRING.getMetadataQueryColumn()); break;
      case DATE:
        Date dateValue = (Date)resultList.get(OntologyItemType.DATE.getMetadataQueryColumn());
        value = formatDate(dateValue);
        break;
      case MULTIFILTER:
        break;
      default:
        break;
    }
    return value == null? null : value.toString();
  }
  
  private static String formatDate(Date dateValue) {
    if (dateValue == null) return null;
    synchronized (DATE_FORMATTER) { return DATE_FORMATTER.format(dateValue); }
  }

}
