package org.gusdb.wdk.model.query.spec;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.gusdb.fgputil.MapBuilder;
import org.gusdb.fgputil.collection.ReadOnlyHashMap;
import org.gusdb.fgputil.validation.Validateable;
import org.gusdb.fgputil.validation.ValidationBundle;
import org.gusdb.fgputil.validation.ValidationLevel;
import org.gusdb.wdk.model.query.param.ParameterContainer;
import org.gusdb.wdk.model.user.StepContainer;
import org.gusdb.wdk.model.user.User;

public class ParameterContainerInstanceSpec<T extends ParameterContainerInstanceSpec<T>>
    extends ReadOnlyHashMap<String,String>
    implements Validateable<T> {

  final User _requestingUser;
  final ParameterContainer _parameterContainer;
  private final StepContainer _stepContainer;
  private final ValidationBundle _validationBundle;

  ParameterContainerInstanceSpec(User requestingUser, ParameterContainer parameterContainer, Map<String, String> paramValues,
      ValidationBundle validationBundle, StepContainer stepContainer) {
    super(paramValues);
    _requestingUser = requestingUser;
    _parameterContainer = parameterContainer;
    _stepContainer = stepContainer;
    _validationBundle = validationBundle;
  }

  protected ParameterContainerInstanceSpec(User requestingUser, Map<String, String> paramValues) {
    super(paramValues);
    _requestingUser = requestingUser;
    _parameterContainer = null;
    _stepContainer = StepContainer.emptyContainer();
    _validationBundle = ValidationBundle.builder(ValidationLevel.NONE)
        .addError("No parameter container present to validate params.").build();
  }

  public Optional<ParameterContainer> getParameterContainer() {
    return Optional.ofNullable(_parameterContainer);
  }

  public User getRequestingUser() {
    return _requestingUser;
  }

  public StepContainer getStepContainer() {
    return _stepContainer;
  }

  @Override
  public ValidationBundle getValidationBundle() {
    return _validationBundle;
  }

  public Map<String, String> toMap() {
    // use linked hashmap since sometimes param ordering matters
    return new MapBuilder<String,String>(new LinkedHashMap<>()).putAll(_map).toMap();
  }

  @Override
  public String toString() {
    return "ParameterContainerInstanceSpec {\n"
      + "  _requestingUser: "     + _requestingUser     + ",\n"
      + "  _parameterContainer: " + _parameterContainer + ",\n"
      + "  _stepContainer: "      + _stepContainer      + ",\n"
      + "  _validationBundle: "   + _validationBundle   + ",\n"
      + "  _map: "                + _map                + "\n"
      + '}';
  }
}
