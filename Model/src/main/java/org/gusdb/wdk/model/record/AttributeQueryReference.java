package org.gusdb.wdk.model.record;

import static org.gusdb.wdk.model.AttributeMetaQueryHandler.getDynamicallyDefinedAttributes;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.Timer;
import org.gusdb.wdk.model.AttributeMetaQueryHandler;
import org.gusdb.wdk.model.Reference;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.SqlQuery;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.ColumnAttributeField;
import org.gusdb.wdk.model.record.attribute.QueryColumnAttributeField;
import org.gusdb.wdk.model.report.DynamicAttributeReporterReference;

/**
 * <p>
 * an object representation of the @{code &lt;recordClass>/&lt;attributeQuery>}, it
 * provides a reference to an attribute {@link Query}, and
 * {@link AttributeField}s can be defined in this tag.
 * </p>
 * 
 * <p>
 * The attribute {@link Query} of a {@link RecordClass} must be a
 * {@link SqlQuery}, with no {@link Param}s. The attribute {@link Query} should
 * return all possible records a record type can have in the system, and each
 * record must be unique in the result.
 * </p>
 * 
 * <p>
 * The attribute {@link Query} must also have all the primary key
 * {@link Column}s defined, but defining a {@link ColumnAttributeField} for
 * those columns is optional.
 * </p>
 * 
 * <p>
 * At runtime, the attribute query will be used by WDK in two contexts: on
 * summary page and on record page. Please refer to the {@link Query} class for
 * how to define attribute queries.
 * </p>
 * 
 * @author Jerric
 */
public class AttributeQueryReference extends Reference {

  private static final Logger LOG = Logger.getLogger(AttributeQueryReference.class);

  private String _attributeMetaQueryRef;
  private List<AttributeField> attributeFieldList = new ArrayList<AttributeField>();
  private Map<String, AttributeField> attributeFieldMap = new LinkedHashMap<String, AttributeField>();

  public AttributeQueryReference() {}

  public void setRecordClass(RecordClass recordClass) {
    for (AttributeField field : attributeFieldList) {
      field.setContainer(recordClass);
    }
  }
  /**
   * Sets an optional reference to a dynamic columns query
   * @param attributeMetaQueryRef dynamic columns query ref of the form "set.element"
   */
  public void setAttributeMetaQueryRef(String attributeMetaQueryRef) {
    _attributeMetaQueryRef = attributeMetaQueryRef;
  }

  public void addAttributeField(AttributeField attributeField) {
    attributeFieldList.add(attributeField);
  }

  public Map<String, AttributeField> getAttributeFieldMap() {
    return new LinkedHashMap<String, AttributeField>(attributeFieldMap);
  }

  public AttributeField[] getAttributeFields() {
    AttributeField[] array = new AttributeField[attributeFieldMap.size()];
    attributeFieldMap.values().toArray(array);
    return array;
  }

  /**
   * This resolver collects database defined column attributes that are obtained from a meta column
   * query table as specified by the _attributeMetaQueryRef.
   */
  @Override
  public void resolveReferences(final WdkModel wdkModel) throws WdkModelException {
    // Continue only if an attribute meta query reference is provided.  
    if(_attributeMetaQueryRef != null) {
      Timer timer = new Timer();
      for (Map<String,Object> row : getDynamicallyDefinedAttributes(_attributeMetaQueryRef, wdkModel)) {
        AttributeField attributeField = new QueryColumnAttributeField();

        // Need to call this explicitly since this attribute field originates from the database
        attributeField.excludeResources(wdkModel.getProjectId());

        // Populate the attributeField with the attribute meta data
        AttributeMetaQueryHandler.populate(attributeField, row);

        // Populate the attribute plugin if present
        DynamicAttributeReporterReference reporter = AttributeMetaQueryHandler.populate(new DynamicAttributeReporterReference(), row);
        if (reporter.getReferenceName() != null) {
          reporter.setAttributeField(attributeField);
          reporter.setScopes("");
          // plugin specified for this attribute
          if (!reporter.hasAllDynamicFields()) {
            throw new WdkModelException("Dynamic attribute plugin '" + reporter.getName() +
                "' is missing at least one plugin field.  Configured values: " + reporter.getDynamicFieldsAsString());
          }
          attributeField.addReporterReference(reporter);
        }

        attributeFieldMap.put(attributeField.getName(), attributeField);
      }
      LOG.debug("Took " + timer.getElapsedString() + " to resolve AttributeMetaQuery: " + _attributeMetaQueryRef);
    }
  }

  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    super.excludeResources(projectId);

    // exclude attribute fields
    for (AttributeField field : attributeFieldList) {
      if (field.include(projectId)) {
        field.excludeResources(projectId);
        String fieldName = field.getName();
        if (attributeFieldMap.containsKey(fieldName))
          throw new WdkModelException("The attributeField " + fieldName
              + " is duplicated in queryRef " + this.getTwoPartName());
        attributeFieldMap.put(fieldName, field);
      }
    }
    attributeFieldList = null;
  }
}
