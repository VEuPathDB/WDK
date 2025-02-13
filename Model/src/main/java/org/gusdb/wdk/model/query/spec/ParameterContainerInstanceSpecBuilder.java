package org.gusdb.wdk.model.query.spec;

import static org.gusdb.fgputil.FormatUtil.NL;
import static org.gusdb.fgputil.FormatUtil.getCurrentStackTrace;
import static org.gusdb.wdk.model.Utilities.PARAM_USER_ID;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.FormatUtil.Style;
import org.gusdb.fgputil.Tuples.TwoTuple;
import org.gusdb.fgputil.collection.ReadOnlyHashMap;
import org.gusdb.fgputil.validation.ValidationBundle;
import org.gusdb.fgputil.validation.ValidationBundle.ValidationBundleBuilder;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.param.Param;
import org.gusdb.wdk.model.query.param.ParameterContainer;
import org.gusdb.wdk.model.user.StepContainer;
import org.gusdb.wdk.model.user.User;

public class ParameterContainerInstanceSpecBuilder<T extends ParameterContainerInstanceSpecBuilder<T>>
    extends ReadOnlyHashMap.Builder<String,String> {

  private static final Logger LOG = Logger.getLogger(ParameterContainerInstanceSpecBuilder.class);

  private static final boolean INCLUDE_STACK_TRACE_IN_VALIDATION_INIT_LOG = false;

  public enum FillStrategy {
    NO_FILL(false, false),
    FILL_PARAM_IF_MISSING(true, false),
    FILL_PARAM_IF_MISSING_OR_INVALID(true, true);

    private final boolean _fillWhenMissing;
    private final boolean _fillWhenInvalid;

    FillStrategy(boolean fillWhenMissing, boolean fillWhenInvalid) {
      _fillWhenMissing = fillWhenMissing;
      _fillWhenInvalid = fillWhenInvalid;
    }

    public boolean shouldFillWhenMissing() {
      return _fillWhenMissing;
    }

    public boolean shouldFillWhenInvalid() {
      return _fillWhenInvalid;
    }
  }

  protected ParameterContainerInstanceSpecBuilder() {
    super(new LinkedHashMap<>());
  }

  protected ParameterContainerInstanceSpecBuilder(ParameterContainerInstanceSpec<?> spec) {
    super(new LinkedHashMap<>(spec.toMap()));
  }

  @Override
  @SuppressWarnings("unchecked")
  public T put(String key, String value) {
    return (T)super.put(key, value);
  }

  @Override
  @SuppressWarnings("unchecked")
  public T putAll(Map<String,String> values) {
    return (T)super.putAll(values);
  }

  protected void standardizeStableValues(ParameterContainer container) {
    for (Param param : container.getParams()) {
      if (containsKey(param.getName())) {
        String value = get(param.getName());
        // parent class supports null values, but null values should be treated
        //   as not-set, so remove any entries containing null values
        if (value == null ) {
          remove(param.getName());
        }
        else {
          // replace with standardized value if present, or if not present but not
          //   expected (because invisible to client), fill with standard value
          put(param.getName(), param.getStandardizedStableValue(value));
        }
      }
    }
  }

  protected TwoTuple<PartiallyValidatedStableValues, ValidationBundleBuilder>
    validateParams(ParameterContainer paramContainer, StepContainer stepContainer, User requestingUser,
      ValidationLevel validationLevel, FillStrategy fillStrategy) throws WdkModelException {

    // create a copy of the values in this builder which will be modified before passing to constructor
    var tmpValues = new HashMap<>(toMap());
    var reqParams = paramContainer.getRequiredParams();

    // trim off any values supplied that don't apply to this container
    for (var name : tmpValues.keySet().toArray(new String[0]))
      if (!reqParams.containsKey(name))
        tmpValues.remove(name);

    // add user_id to the param values if needed
    if (reqParams.containsKey(PARAM_USER_ID)) {
      // fill current user's ID, always overriding an existing value;
      // this is a security precaution in case the caller submits a value for user_id
      tmpValues.put(PARAM_USER_ID, Long.toString(requestingUser.getUserId()));
    }

    var stableValues = new PartiallyValidatedStableValues(requestingUser, tmpValues, stepContainer);
    var validation = ValidationBundle.builder(validationLevel);

    if (LOG.isEnabledFor(Param.VALIDATION_LOG_PRIORITY)) {
      LOG.log(Param.VALIDATION_LOG_PRIORITY, "Beginning param validation for " +
          "instance of container: " + paramContainer.getFullName() +
          " with validation level " + validationLevel + " and fill strategy " +
          fillStrategy + ". It requires the following params [ " +
          String.join(", ", reqParams.keySet()) + " ].  Passed params: " +
          FormatUtil.prettyPrint(tmpValues, Style.MULTI_LINE) +
          (INCLUDE_STACK_TRACE_IN_VALIDATION_INIT_LOG ? (NL + getCurrentStackTrace()) : ""));
    }

    for (var param : reqParams.values()) {
      var result = param.validate(stableValues, validationLevel, fillStrategy);
      if (!result.isValid())
        validation.addError(param.getName(), result.getMessage());
    }

    return new TwoTuple<>(stableValues, validation);
  }
}
