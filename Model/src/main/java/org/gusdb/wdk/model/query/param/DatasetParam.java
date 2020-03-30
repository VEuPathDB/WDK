package org.gusdb.wdk.model.query.param;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkUserException;
import org.gusdb.wdk.model.dataset.Dataset;
import org.gusdb.wdk.model.dataset.DatasetParser;
import org.gusdb.wdk.model.dataset.DatasetParserReference;
import org.gusdb.wdk.model.dataset.ListDatasetParser;
import org.gusdb.wdk.model.query.spec.PartiallyValidatedStableValues;
import org.gusdb.wdk.model.query.spec.PartiallyValidatedStableValues.ParamValidity;
import org.gusdb.wdk.model.record.RecordClass;

/**
 * Dataset param represents a list of user input ids. The list is readonly, and stored in persistence
 * instance, along with other user related data.
 *
 * A dataset param is typed, and the author has to define a recordClass as the type of the input IDs. This
 * type is required as a function for getting a snapshot of user basket and make a step from it.
 *
 * @author xingao
 *
 *         raw value: Dataset object;
 *
 *         stable value: dataset id;
 *
 *         signature: content checksum;
 *
 *         internal data: A SQL that represents the dataset values.
 *
 */
public class DatasetParam extends Param {

  public static final String TYPE_DATA = "data";
  public static final String TYPE_FILE = "file";
  public static final String TYPE_BASKET = "basket";
  public static final String TYPE_STRATEGY = "strategy";

  private String recordClassRef;
  private RecordClass recordClass;

  /**
   * Only used by datasetParam, determines what input type to be selected as default.
   */
  private String defaultType;

  private List<DatasetParserReference> parserReferences = new ArrayList<>();

  private Map<String, DatasetParser> parsers;

  public DatasetParam() {
    setHandler(new DatasetParamHandler());
  }

  public DatasetParam(DatasetParam param) {
    super(param);
    this.recordClass = param.recordClass;
    this.recordClassRef = param.recordClassRef;
    this.defaultType = param.defaultType;
    if (param.parserReferences != null)
      this.parserReferences = new ArrayList<>(param.parserReferences);
    if (param.parsers != null)
      this.parsers = new LinkedHashMap<>(param.parsers);
  }

  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    super.excludeResources(projectId);

    // exclude parser references
    Map<String, DatasetParserReference> references = new LinkedHashMap<>();
    for (DatasetParserReference reference : parserReferences) {
      if (reference.include(projectId)) {
        String refName = reference.getName();
        if (references.containsKey(refName))
          throw new WdkModelException("parser '" + refName + "' is duplicated in datasetParam " + getFullName());
        reference.excludeResources(projectId);
        references.put(refName, reference);
      }
    }
    parserReferences = new ArrayList<>(references.values());
  }

  @Override
  public void resolveReferences(WdkModel model) throws WdkModelException {
    super.resolveReferences(model);

    // optional recordClass Ref
    if (recordClassRef != null)
      recordClass = (RecordClass) _wdkModel.resolveReference(recordClassRef);

    // get parsers
    parsers = new LinkedHashMap<>();
    // add the default parser into it first, so that it could be overridden if needed
    DatasetParser parser = new ListDatasetParser();
    parsers.put(parser.getName(), parser);
    if (parserReferences != null) {
      for (DatasetParserReference reference : parserReferences) {
        reference.resolveReferences(model);
        parser = reference.getParser();
        parsers.put(parser.getName(), parser);
      }
      parserReferences = null;
    }
    for(DatasetParser p : parsers.values()) {
      p.setParam(this);
    }
  }

  @Override
  public Param clone() {
    return new DatasetParam(this);
  }

  @Override
  protected ParamValidity validateValue(PartiallyValidatedStableValues ctxParamVals, ValidationLevel level) {

    final String name = getName();
    final String stableValue = ctxParamVals.get(name);

    // value must be a positive integer (representing a dataset ID)
    if (!FormatUtil.isInteger(stableValue)) {
      return ctxParamVals.setInvalid(name, level, "'" + stableValue + "' must be a positive integer (Dataset ID).");
    }

    // that's all the validation we perform if level is syntactic
    if (level.isLessThanOrEqualTo(ValidationLevel.SYNTACTIC)) {
      return ctxParamVals.setValid(name, level);
    }

    // otherwise, make sure the dataset exists
    try {
      _wdkModel.getDatasetFactory().getDatasetWithOwner(
          Long.parseLong(stableValue), ctxParamVals.getUser().getUserId());
    }
    catch (WdkModelException | WdkUserException | NumberFormatException e) {
      return ctxParamVals.setInvalid(name, level, e.getMessage());
    }

    return ctxParamVals.setValid(name, level);
  }

  /**
   * @return the recordClass
   */
  public Optional<RecordClass> getRecordClass() {
    return Optional.ofNullable(recordClass);
  }

  /**
   * @param recordClassRef
   *          the recordClassRef to set
   */
  public void setRecordClassRef(String recordClassRef) {
    this.recordClassRef = recordClassRef;
  }

  public void setRecordClass(RecordClass recordClass) {
    this.recordClass = recordClass;
  }

  public String getDefaultType() {
    return (defaultType != null) ? defaultType : TYPE_DATA;
  }

  public void setDefaultType(String defaultType) {
    this.defaultType = defaultType;
  }

  @Override
  protected void applySuggestion(ParamSuggestion suggest) {
    defaultType = ((DatasetParamSuggestion) suggest).getDefaultType();
  }

  public Collection<DatasetParser> getParsers() {
    return parsers.values();
  }

  public DatasetParser getParser(String parserName) throws WdkModelException {
    DatasetParser parser = parsers.get(parserName);
    if (parser == null)
      throw new WdkModelException("The DatasetParser '" + parserName + "' doesn't exist in datasetParam " +
          getFullName());
    return parser;
  }

  @Override
  public String getBriefRawValue(Object rawValue, int truncateLength) throws WdkModelException {
    return ((Dataset) rawValue).getContent()
      .truncate(truncateLength);
  }

  public void addParserReference(DatasetParserReference reference) {
    this.parserReferences.add(reference);
  }

  @Override
  protected String getDefault(PartiallyValidatedStableValues stableValues) {
    // default stable value for DatasetParam is always an empty string;
    // XML default value is used to display a default set of IDs (i.e. raw value) in the user interface
    return "";
  }
}
