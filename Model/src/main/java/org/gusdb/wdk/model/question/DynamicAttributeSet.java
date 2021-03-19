package org.gusdb.wdk.model.question;

import static org.gusdb.fgputil.FormatUtil.NL;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.gusdb.wdk.model.Utilities;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.gusdb.wdk.model.query.Column;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.query.param.AnswerParam;
import org.gusdb.wdk.model.record.RecordClass;
import org.gusdb.wdk.model.record.attribute.AttributeField;
import org.gusdb.wdk.model.record.attribute.ColumnAttributeField;
import org.gusdb.wdk.model.record.attribute.QueryColumnAttributeField;
import org.gusdb.wdk.model.report.AttributeReporterRef;
import org.gusdb.wdk.model.report.reporter.HistogramAttributeReporter;

/**
 * An object representation of the {@code <question>/<dynamicAttributes>}. The
 * author of the model files can introduce new {@link AttributeField}s to a
 * {@link RecordClass} that are specific to a certain {@link Question}. The
 * {@link AttributeField}s introduced in this way are called dynamic attributes,
 * and since they are specific to the defining {@link Question}, those
 * attributes won't be carried over to the boolean or transform steps.
 *
 * @author jerric
 */
public class DynamicAttributeSet extends WdkModelBase {

  // private static Logger logger =
  // Logger.getLogger(DynamicAttributeSet.class);

  private List<AttributeField> attributeFieldList = new ArrayList<>();
  private Map<String, AttributeField> attributeFieldMap = new LinkedHashMap<>();

  private Question question;

  public DynamicAttributeSet() {}

  public void addAttributeField(AttributeField attributeField) {
	// from XML parse, before excludeResources
	  if (attributeFieldList != null) attributeFieldList.add(attributeField);

	  // to support adding after excludeResources
	  else attributeFieldMap.put(attributeField.getName(), attributeField);
  }

  @Override
  public String toString() {
    StringBuilder buf = new StringBuilder();
    buf.append("  dynamicAttributes:" + NL);

    for (String attrName : attributeFieldMap.keySet()) {
      buf.append("    " + attrName + NL);
    }
    return buf.toString();
  }

  // /////////////////////////////////////////////////////////////////
  // package methods //
  // /////////////////////////////////////////////////////////////////

  public void setQuestion(Question question) {
    this.question = question;
    // question will be the container for this container's dynamic fields
    for (AttributeField field : attributeFieldList)
      field.setContainer(question);
  }

  public Question getQuestion() {
    return question;
  }

  public Map<String, AttributeField> getAttributeFieldMap() {
    return attributeFieldMap;
  }

  // /////////////////////////////////////////////////////////////////
  // private methods //
  // /////////////////////////////////////////////////////////////////

  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    // exclude the attribute fields
    for (AttributeField field : attributeFieldList) {
      if (field.include(projectId)) {
        field.excludeResources(projectId);
        String fieldName = field.getName();

        // the attribute name must be unique
        if (attributeFieldMap.containsKey(fieldName)) {
          throw new WdkModelException("DynamicAttributes contain a duplicate attribute '" + fieldName + "'");
        }

        attributeFieldMap.put(fieldName, field);
      }
    }
    attributeFieldList = null;

    // check if weight is defined, if not, create it
    if (!attributeFieldMap.containsKey(Utilities.COLUMN_WEIGHT)) {
      // always have weight as a dynamic attribute
      ColumnAttributeField attribute = new QueryColumnAttributeField();
      attribute.setName(Utilities.COLUMN_WEIGHT);
      attribute.setDisplayName("Search Weight");
      attribute.setInternal(false);
      attribute.setInReportMaker(true);
      attribute.setSortable(true);
      // attribute.setHelp("User-defined integer value; in a search strategy,
      // unions and intersects will sum the weights, giving higher scores to
      // items found in multiple searches. ");
      attribute.setHelp("The *search result weight*. This is the sum of "
          + "the weights you specified for individual searches that "
          + "found this record.   The more searches that found this "
          + "record the higher will be its weight.  To give a search "
          + "a weight, click \"Use Weights\" on the search page, and "
          + "provide the desired weight.");
      // add an histogram plugin to the weight
      attribute.addReporterReference(getHistogramAttributeReporterRef());
      attribute.excludeResources(projectId);
      attributeFieldMap.put(Utilities.COLUMN_WEIGHT, attribute);
    }

  }

  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    RecordClass recordClass = question.getRecordClass();
    Query query = question.getDynamicAttributeQuery();
    Map<String, Column> columns = query.getColumnMap();

    // check if answer params exist; if so add their values as dynamic columns
    Set<String> rcAttributeNames = question.getRecordClass().getAttributeFieldMap().keySet();
    List<AnswerParam> params = AnswerParam.getExposedParams(question.getParamMap().values());
    for (AnswerParam param : params) {
      String paramName = param.getName();
      String errorMsgPrefix = "Question " + question.getFullName() + " contains answer param with name '" +
          paramName + "' that conflicts with the name of an ";
      if (attributeFieldMap.containsKey(paramName)) {
        throw new WdkModelException(errorMsgPrefix + "existing dynamic column.");
      }
      if (rcAttributeNames.contains(paramName)) {
        throw new WdkModelException(errorMsgPrefix + "attribute in the question's recordclass.");
      }
      // param passes validation; add dynamic attribute
      QueryColumnAttributeField attribute = new QueryColumnAttributeField();
      attribute.setName(paramName);
      attribute.setDisplayName("AnswerParam: " + paramName);
      attribute.setColumn(columns.get(paramName));
      attribute.setInternal(true);
      attribute.setInReportMaker(false);
      attribute.excludeResources(wdkModel.getProjectId());
      attribute.resolveReferences(wdkModel);
      attributeFieldMap.put(paramName, attribute);
    }

    // make sure the dynamic attribute set doesn't have duplicated attributes
    Map<String, AttributeField> fields = recordClass.getAttributeFieldMap();
    for (String fieldName : attributeFieldMap.keySet()) {
      if (fields.containsKey(fieldName)) {
        throw new WdkModelException("Dynamic attribute field " + fieldName +
            " in question " + question.getFullName() +
            " already exists in recordClass " + recordClass.getFullName());
      }
      AttributeField field = attributeFieldMap.get(fieldName);
      field.setContainer(question);
      if (field instanceof QueryColumnAttributeField) {
        // need to set the column before resolving references
        Column column = columns.get(fieldName);
        if (column == null)
          throw new WdkModelException("Dynamic column attribute of "
              + "question [" + question.getFullName() + "] is "
              + "defined, but the underlying id query doesn't"
              + " have the column '" + fieldName + "'");
        ((QueryColumnAttributeField) field).setColumn(column);
      }

      field.resolveReferences(wdkModel);
    }

    // Assign data types to the contained columns.
    var types = query.resolveColumnTypes();
    types.keySet()
      .stream()
      .map(attributeFieldMap::get)
      .filter(ColumnAttributeField.class::isInstance)
      .map(ColumnAttributeField.class::cast)
      .forEach(field -> field.setDataType(types.get(field.getName())));
  }

  private AttributeReporterRef getHistogramAttributeReporterRef() {
    AttributeReporterRef reference = new AttributeReporterRef();
    WdkModelText description = new WdkModelText();
    description.setText("Display a histogram of weight distribution");
    reference.setName("histogram");
    reference.setDisplayName("Histogram");
    reference.setDescription(description);
    reference.setImplementation(HistogramAttributeReporter.class.getName());
    return reference;
  }
}
