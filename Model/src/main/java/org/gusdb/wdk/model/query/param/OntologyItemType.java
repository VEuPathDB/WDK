package org.gusdb.wdk.model.query.param;

import static java.util.Arrays.asList;
import static org.gusdb.fgputil.functional.Functions.mapToList;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.gusdb.wdk.model.WdkModelException;

public enum OntologyItemType {

  STRING("string", "string_value", String.class),
  NUMBER("number", "number_value", Double.class),
  DATE("date", "date_value", String.class);

  private final String _dbTypeIdentifier;
  private final String _metadataQueryColumn;
  private final Class<?> _javaClass;

  private OntologyItemType(String dbTypeIdentifier, String metadataQueryColumn, Class<?> javaClass) {
    _dbTypeIdentifier = dbTypeIdentifier;
    _metadataQueryColumn = metadataQueryColumn;
    _javaClass = javaClass;
  }

  public String getMetadataQueryColumn() {
    return _metadataQueryColumn;
  }

  public Class<?> getJavaClass() {
    return _javaClass;
  }

  public static OntologyItemType getType(String typeIdentifier) throws WdkModelException {
    for (OntologyItemType type : values()) {
      if (type._dbTypeIdentifier.equalsIgnoreCase(typeIdentifier)) {
        return type;
      }
    }
    throw new WdkModelException("Unrecognized ontology item type: " + typeIdentifier);
  }

  public static String[] getMetadataQueryColumns() {
    return mapToList(asList(values()),
        type -> type._metadataQueryColumn
    ).toArray(new String[values().length]);
  }

/*  @SuppressWarnings("unchecked")
  public static <T> T resolveTypedValue(ResultSet resultSet, OntologyItem ontologyItem,
      Class<T> ontologyItemClass) throws SQLException {
    if (!ontologyItem.getType().getJavaClass().getName().equals(ontologyItemClass.getName())) {
      throw new IllegalStateException("Incoming ontologyItemClass (" + ontologyItemClass.getName() +
          ") must be the same as that configured for the ontologyItem's value Java class (" +
          ontologyItem.getType().getJavaClass().getName() + ").  See ontology item '" + ontologyItem.getOntologyId() + "'.");
    }
    Object value = null;
    switch(ontologyItem.getType()) {
      case NUMBER:
        value = resultSet.getDouble(OntologyItemType.NUMBER.getMetadataQueryColumn());
      case STRING:
        value = resultSet.getString(OntologyItemType.STRING.getMetadataQueryColumn());
      case DATE:
        value = resultSet.getString(OntologyItemType.DATE.getMetadataQueryColumn());
    }
    if (resultSet.wasNull()) {
      value = null;
    }
    return (T) value;
  }*/
}
