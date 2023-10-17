package org.gusdb.wdk.model.report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.BuildTracking;
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
public class ReporterRef extends WdkModelBase implements ScopedField, PropertiesProvider, BuildTracking {

  private static final Logger LOG = Logger.getLogger(ReporterRef.class);

  private String _name;
  private String _displayName;
  private String _scopes = "";
  private String _description;
  private String _implementation;
  private boolean _inReportMaker = true;
  private List<WdkModelText> _propertyList = new ArrayList<>();
  private Map<String, String> _properties = new LinkedHashMap<>();

  /**
   * new build flag on what build this question is introduced.
   */
  private String _newBuild;

  /**
   * revise build flag on what build this question is revised.
   */
  private String _reviseBuild;

  @Override
  public WdkModel getWdkModel() {
    return _wdkModel;
  }

  /**
   * @return the implementation
   */
  public String getImplementation() {
    return _implementation;
  }

  /**
   * @param implementation
   *          the implementation to set
   */
  @RngOptional
  public void setImplementation(String implementation) {
    _implementation = implementation;
  }

  /**
   * @return the name
   */
  public String getName() {
    return _name;
  }

  /**
   * @param name
   *          the name to set
   */
  @RngOptional
  public void setName(String name) {
    _name = name;
  }

  /**
   * @return name by default, overridden by attribute reporter refs
   */
  public String getReferenceName() {
    return _name;
  }

  /**
   * @return the displayName
   */
  public String getDisplayName() {
    return _displayName;
  }

  /**
   * @param displayName
   *          the displayName to set
   */
  @RngOptional
  public void setDisplayName(String displayName) {
    _displayName = displayName;
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
    return _inReportMaker;
  }

  /**
   * @param inReportMaker
   *          the inReportMaker to set
   */
  @RngOptional
  public void setInReportMaker(boolean inReportMaker) {
    _inReportMaker = inReportMaker;
  }

  /**
   * @return the list of scopes
   */
  public String getScopes() {
    return _scopes;
  }

  public List<String> getScopesList() {
    return _scopes.isEmpty() ? Collections.emptyList() :
        Arrays.asList(_scopes.split("\\s*,\\s*"));
  }

  /**
   * @param scopes
   *          comma-separated list of scopes that reporter is visible
   */
  @RngOptional
  public void setScopes(String scopes) {
    _scopes = scopes;
  }

  @RngOptional
  public void setDescription(WdkModelText description) {
    _description = description.getText();
  }

  public String getDescription() {
    return _description;
  }

  @Override
  public String getNewBuild() {
    return _newBuild;
  }

  @Override
  @RngOptional
  public void setNewBuild(String newBuild) {
    _newBuild = newBuild;
  }

  @Override
  public String getReviseBuild() {
    return _reviseBuild;
  }

  @Override
  @RngOptional
  public void setReviseBuild(String reviseBuild) {
    _reviseBuild = reviseBuild;
  }

  public void addProperty(WdkModelText property) {
    _propertyList.add(property);
  }

  @Override
  public Map<String, String> getProperties() {
    return new LinkedHashMap<String, String>(_properties);
  }

  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    // exclude properties
    for (WdkModelText property : _propertyList) {
      if (property.include(projectId)) {
        property.excludeResources(projectId);
        String propName = property.getName();
        String propValue = property.getText();
        if (_properties.containsKey(propName))
          throw new WdkModelException("The property " + propName
              + " is duplicated in reporter " + _name);
        _properties.put(propName, propValue);
        LOG.trace("reporter property: [" + propName + "]='" + propValue
            + "'");
      }
    }
    _propertyList = null;
  }

  @RngUndefined
  public void setResources(WdkModel wdkModel) {
    _wdkModel = wdkModel;
  }

  @Override
  public void resolveReferences(WdkModel wodkModel) throws WdkModelException {
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
