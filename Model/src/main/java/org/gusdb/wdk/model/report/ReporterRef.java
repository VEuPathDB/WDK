package org.gusdb.wdk.model.report;

import java.util.*;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.RngAnnotations.RngOptional;
import org.gusdb.wdk.model.RngAnnotations.RngUndefined;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelBase;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.gusdb.wdk.model.record.ScopedField;

/**
 * A reference to the download Reporter class. the full class name of the
 * reporter is set into implementation, and an instance of the reporter will be
 * created to generate download results.
 *
 * @author xingao
 *
 */
public class ReporterRef extends WdkModelBase implements ScopedField {

  private static final Logger logger = Logger.getLogger(ReporterRef.class);

  public static final String WDK_SERVICE_JSON_REPORTER_RESERVED_NAME = "wdk-service-json";

  private String name;
  private String displayName;
  private String scopes;
  private String description;
  private String implementation;
  private boolean inReportMaker = true;
  private List<WdkModelText> propertyList = new ArrayList<>();
  private Map<String, String> properties = new LinkedHashMap<>();

  @Override
  public WdkModel getWdkModel() {
    return _wdkModel;
  }

  /**
   * @return the implementation
   */
  public String getImplementation() {
    return implementation;
  }

  /**
   * @param implementation
   *          the implementation to set
   */
  @RngOptional
  public void setImplementation(String implementation) {
    this.implementation = implementation;
  }

  /**
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * @param name
   *          the name to set
   */
  @RngOptional
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return name by default, overridden by attribute reporter refs
   */
  public String getReferenceName() {
    return name;
  }

  /**
   * @return the displayName
   */
  public String getDisplayName() {
    return displayName;
  }

  /**
   * @param displayName
   *          the displayName to set
   */
  @RngOptional
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  @Override
  public boolean isInternal() {
    // all reporters are non-internal
    return false;
  }

  /**
   * @return the inReportMaker
   */
  @Override
  public boolean isInReportMaker() {
    return inReportMaker;
  }

  /**
   * @param inReportMaker
   *          the inReportMaker to set
   */
  @RngOptional
  public void setInReportMaker(boolean inReportMaker) {
    this.inReportMaker = inReportMaker;
  }

  /**
   * @return the list of scopes
   */
  public String getScopes() {
    return scopes;
  }

  public List<String> getScopesList() {
    return scopes.isEmpty() ? Collections.emptyList() :
        Arrays.asList(scopes.split("\\s*,\\s*"));
  }

  /**
   * @param scopes
   *          comma-separated list of scopes that reporter is visible
   */
  @RngOptional
  public void setScopes(String scopes) {
    this.scopes = scopes;
  }

  @RngOptional
  public void setDescription(WdkModelText description) {
    this.description = description.getText();
  }

  public String getDescription() {
    return (description == null ? displayName : description);
  }

  public void addProperty(WdkModelText property) {
    this.propertyList.add(property);
  }

  public Map<String, String> getProperties() {
    return new LinkedHashMap<String, String>(this.properties);
  }

  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    // exclude properties
    for (WdkModelText property : propertyList) {
      if (property.include(projectId)) {
        property.excludeResources(projectId);
        String propName = property.getName();
        String propValue = property.getText();
        if (properties.containsKey(propName))
          throw new WdkModelException("The property " + propName
              + " is duplicated in reporter " + name);
        properties.put(propName, propValue);
        logger.trace("reporter property: [" + propName + "]='" + propValue
            + "'");
      }
    }
    propertyList = null;
  }

  @RngUndefined
  public void setResources(WdkModel wdkModel) {
    this._wdkModel = wdkModel;
  }

  @Override
  public void resolveReferences(WdkModel wodkModel) throws WdkModelException {
    // warn user if they are using a service JSON reporter reference
    if (WDK_SERVICE_JSON_REPORTER_RESERVED_NAME.equals(getName())) {
      logger.warn("You are using reporter reserved name '" + getName() + "'.  This will not affect the " +
          "WDK answer service endpoint; your reporter may not be accessible in that context.");
    }
    // try to find implementation class
    String msgStart = "Implementation class for reporter '" + getName() + "' [" + getImplementation() + "] ";
    try {
      Class<?> implClass = Class.forName(getImplementation());
      if (!Reporter.class.isAssignableFrom(implClass)) {
        throw new WdkModelException(msgStart + "must be a subclass of " + Reporter.class.getName());
      }
    }
    catch (ClassNotFoundException e) {
      throw new WdkModelException(msgStart + "cannot be found.", e);
    }
  }

}
