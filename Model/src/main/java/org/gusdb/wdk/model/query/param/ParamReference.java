package org.gusdb.wdk.model.query.param;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.gusdb.wdk.model.Group;
import org.gusdb.wdk.model.Reference;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.WdkModelText;
import org.gusdb.wdk.model.query.param.AbstractEnumParam.SelectMode;

/**
 * An object representation of a <paramRef> tag, It is used in query and
 * question tag to reference a param, and provide customization to the param.
 * 
 * @author Jerric
 * @created Feb 16, 2006
 */
public class ParamReference extends Reference {

  private static final Logger LOG = Logger.getLogger(ParamReference.class);

  public static Param resolveReference(WdkModel wdkModel,
      ParamReference paramRef, ParameterContainer container) throws WdkModelException {

    String twoPartName = paramRef.getTwoPartName();
    Param param;
    try {
      param = (Param) wdkModel.resolveReference(twoPartName);
    }
    catch (WdkModelException e) {
      throw new WdkModelException("Unable to resolve param reference '" + twoPartName +
          "' referred to by context query '" + container.getFullName() + "'.", e);
    }
    // clone the param to have different default values
    param = param.clone();
    param.setContainer(container);

    // if the param has customized default value
    String defaultValue = paramRef.getDefault();
    if (defaultValue != null)
      param.setDefault(defaultValue);

    // if the param has customized allowEmpty
    Boolean allowEmpty = paramRef.isAllowEmpty();
    if (allowEmpty != null) {
      param.setAllowEmpty(allowEmpty);

      // if the param has customized allowEmpty
      String emptyValue = paramRef.getEmptyValue();
      if (emptyValue != null)
        param.setEmptyValue(emptyValue);
    }
    Boolean noTranslation = paramRef.getNoTranslation();
    if (noTranslation != null)
      param.setNoTranslation(noTranslation);

    // if the visible is set
    Boolean visible = paramRef.getVisible();
    if (visible != null)
      param.setVisible(visible);

    // set help if exists
    String help = paramRef.getHelp();
    if (help != null)
      param.setHelp(help);

    // set visibleHelp if exists
    String visibleHelp = paramRef.getVisibleHelp();
    if (visibleHelp != null)
      param.setVisibleHelp(visibleHelp);

    // set prompt if any
    String prompt = paramRef.getPrompt();
    if (prompt != null)
      param.setPrompt(prompt);

    Boolean number = paramRef.getNumber();

    Boolean quote = paramRef.getQuote();
    Boolean multiPick = paramRef.isMultiPick();
    String displayType = paramRef.getDisplayType();
    SelectMode selectMode = paramRef.getSelectModeEnum();
    String queryRef = paramRef.getQueryRef();
    Boolean suppressNode = paramRef.getSuppressNode();
    Integer minSelectedCount = paramRef.getMinSelectedCount();
    Integer maxSelectedCount = paramRef.getMaxSelectedCount();
    Boolean countOnlyLeaves = paramRef.getCountOnlyLeaves();
    Long interval = paramRef.getInterval();
    Integer depthExpanded = paramRef.getDepthExpanded();
    Boolean exposeAsAttribute = paramRef.isExposeAsAttribute();
    String minDate = paramRef.getMinDate();
    String maxDate = paramRef.getMaxDate();
    Double min = paramRef.getMin();
    Double max = paramRef.getMax();
    
    if (param instanceof AbstractEnumParam) {
      AbstractEnumParam enumParam = (AbstractEnumParam) param;
      // check those invalid properties
      if (number != null)
        throw new WdkModelException("The 'number' property is not "
            + "allowed in param '" + twoPartName + "'");

      if (interval != null)
        throw new WdkModelException("The 'interval' property is not "
            + "allowed in param '" + twoPartName + "'");

      // if the param has customized multi pick
      if (multiPick != null) {
        if (LOG.isDebugEnabled()) {
          if (!enumParam.isMultiPick() != multiPick) {
            LOG.debug("ParamRef to '" + enumParam.getFullName() +
                "' in context query '" + container.getFullName() +
                "' is overriding multi-pick: " + enumParam.isMultiPick() +
                " -> " + multiPick + ", displayType: " + enumParam.getDisplayType() +
                " -> " + (displayType == null ? "<inherited>" : displayType));
          }
        }
        enumParam.setMultiPick(multiPick);
      }

      // if the queryRef is set for FlatVocabParam
      if (queryRef != null) {
        if (param instanceof FlatVocabParam) {
          ((FlatVocabParam) param).setQueryRef(queryRef);
        } else
          throw new WdkModelException("The paramRef to '" + twoPartName
              + "' is not a flatVocabParam. The "
              + "'queryRef' property can only be applied to "
              + "paramRefs of flatVocabParams.");
      }

      // if quote is set, it overrides the value of the param
      if (quote != null)
        enumParam.setQuote(quote);

      // if displayType is set, overrides the value in param
      if (displayType != null)
        enumParam.setDisplayType(displayType);

      if (selectMode != null)
        enumParam.setSelectMode(selectMode);

      if (suppressNode != null)
        enumParam.setSuppressNode(suppressNode);
      
      if (minSelectedCount != null)
        enumParam.setMinSelectedCount(minSelectedCount);
      
      if (maxSelectedCount != null)
        enumParam.setMaxSelectedCount(maxSelectedCount);
      
      if (countOnlyLeaves != null)
        enumParam.setCountOnlyLeaves(countOnlyLeaves);
      
      if (depthExpanded != null)
        enumParam.setDepthExpanded(depthExpanded);

    } else { // or other param types
      if (multiPick != null || quote != null || displayType != null
          || selectMode != null || queryRef != null || depthExpanded != null)
        throw new WdkModelException("The paramRef to '" + twoPartName
            + "' is not a flatVocabParam nor enumParam. The "
            + "'multiPick', 'displayType', 'quote',"
            + " 'selectMode', 'queryRef', 'depthExpanded' properties can only be "
            + "applied to paramRefs of flatVocabParams or " + "enumParams.");

      if (param instanceof StringParam) {
        // if quote is set, it overrides the value of the param
        if (number != null)
          ((StringParam) param).setNumber(number);
      } else if (number != null) {
        throw new WdkModelException("The paramRef to '" + twoPartName
            + "' is not a stringParam. The 'number' property can "
            + "only be applied to paramRefs of stringParams.");
      }

      if (param instanceof TimestampParam) {
        if (interval != null)
          ((TimestampParam)param).setInterval(interval);
      } else if (interval != null) {
        throw new WdkModelException("The paramRef to '" + twoPartName
            + "' is not a timestampParam. The 'interval' property can "
            + "only be applied to paramRefs of timestampParam.");
      }

      if (param instanceof AnswerParam) {
        if (exposeAsAttribute != null)
          ((AnswerParam)param).setExposeAsAttribute(exposeAsAttribute);
      } else if (exposeAsAttribute != null) {
        throw new WdkModelException("The paramRef to '" + twoPartName
            + "' is not a answerParam.  The 'exposeAsAttribute' property can "
            + "only be applied to paramRefs of answerParam.");
      }

      if (param instanceof DateRangeParam) {
        if (minDate != null) {
          ((DateRangeParam)param).setMinDate(minDate);
        }
        if (maxDate != null) {
          ((DateRangeParam)param).setMaxDate(maxDate);
        }
      } else if (minDate != null || maxDate != null) {
        throw new WdkModelException("The paramRef to '" + twoPartName
            + "' is not a dateRangeParam.  The 'minDate' and 'maxDate' properties can "
            + "only be applied to paramRefs of dateRangeParam.");
      }

      if (param instanceof NumberRangeParam) {
        if (min != null) {
          ((NumberRangeParam)param).setMin(min);
        }
        if (max != null) {
          ((NumberRangeParam)param).setMax(max);
        }
      } else if (min != null || max != null) {
        throw new WdkModelException("The paramRef to '" + twoPartName
            + "' is not a numberRangeParam.  The 'min' and 'max' properties can "
            + "only be applied to paramRefs of numberRangeParam.");
      }
    }

    // resolve the group reference
    String groupRef = paramRef.getGroupRef();
    if (groupRef != null) {
      Group group = (Group) wdkModel.resolveReference(groupRef);
      param.setGroup(group);
    } else if (!param.isVisible()) {
      param.setGroup(Group.Hidden());
    }
    param.setResources(wdkModel);
    return param;
  }

  // set of overridable values
  private String _defaultValue;
  private Boolean _allowEmpty;
  private Boolean _multiPick;
  private String _queryRef;
  private Boolean _quote;
  private Boolean _isNumber;
  private String _emptyValue;
  private String _displayType;
  private Boolean _isVisible;
  private SelectMode _selectMode;
  private Boolean _noTranslation;
  private Boolean _suppressNode;
  private Integer _minSelectedCount;
  private Integer _maxSelectedCount;
  private Boolean _countOnlyLeaves;
  private String _prompt;
  private Integer _depthExpanded;
  private Boolean _exposeAsAttribute;
  private String _minDate;
  private String _maxDate;
  private Double _min;
  private Double _max;

  private List<WdkModelText> _helps = new ArrayList<WdkModelText>();
  private String _help;

  private List<WdkModelText> _visibleHelps = new ArrayList<WdkModelText>();
  private String _visibleHelp;

  // this property only applies to timestamp param.
  private Long _interval;

  public ParamReference() {}

  /**
   * @param twoPartName
   */
  public ParamReference(String twoPartName) throws WdkModelException {
    super(twoPartName);
  }

  /**
   * @return Returns the defaultValue.
   */
  public String getDefault() {
    return _defaultValue;
  }

  /**
   * @param defaultValue
   *          The defaultValue to set.
   */
  public void setDefault(String defaultValue) {
    _defaultValue = defaultValue;
  }

  /**
   * @return the allowEmpty
   */
  public Boolean isAllowEmpty() {
    return _allowEmpty;
  }

  /**
   * @param allowEmpty
   *          the allowEmpty to set
   */
  public void setAllowEmpty(boolean allowEmpty) {
    _allowEmpty = allowEmpty;
  }

  /**
   * @return the multiPick
   */
  public Boolean isMultiPick() {
    return _multiPick;
  }

  /**
   * @param multiPick
   *          the multiPick to set
   */
  public void setMultiPick(boolean multiPick) {
    _multiPick = multiPick;
  }

  /**
   * @return the queryRef
   */
  public String getQueryRef() {
    return _queryRef;
  }

  /**
   * @param queryRef
   *          the queryRef to set
   */
  public void setQueryRef(String queryRef) {
    _queryRef = queryRef;
  }

  /**
   * @return the quote
   */
  public Boolean getQuote() {
    return _quote;
  }

  /**
   * @param quote
   *          the quote to set
   */
  public void setQuote(Boolean quote) {
    _quote = quote;
  }

  public String getEmptyValue() {
    return _emptyValue;
  }

  public void setEmptyValue(String emptyValue) {
    _emptyValue = emptyValue;
  }

  /**
   * @return the displayType
   */
  public String getDisplayType() {
    return _displayType;
  }

  /**
   * @param displayType
   *          the displayType to set
   */
  public void setDisplayType(String displayType) {
    _displayType = displayType;
  }

  /**
   * @return the visible
   */
  public Boolean getVisible() {
    return _isVisible;
  }

  /**
   * @param visible
   *          the visible to set
   */
  public void setVisible(Boolean visible) {
    _isVisible = visible;
  }

  /**
   * @return the selectMode
   */
  public String getSelectMode() {
    return _selectMode.name();
  }

  public SelectMode getSelectModeEnum() {
    return _selectMode;
  }

  /**
   * @param selectMode
   *          the selectMode to set
   */
  public void setSelectMode(String selectMode) {
    _selectMode = SelectMode.valueOf(selectMode.toUpperCase());
  }

  /**
   * @return the noTranslation
   */
  public Boolean getNoTranslation() {
    return _noTranslation;
  }

  /**
   * @param noTranslation
   *          the noTranslation to set
   */
  public void setNoTranslation(Boolean noTranslation) {
    _noTranslation = noTranslation;
  }

  /**
   * @return the number
   */
  public Boolean getNumber() {
    return _isNumber;
  }

  /**
   * @param number
   *          the number to set
   */
  public void setNumber(Boolean number) {
    _isNumber = number;
  }

  /**
   * @return the suppressNode
   */
  public Boolean getSuppressNode() {
    return _suppressNode;
  }

  /**
   * @param suppressNode
   *          the suppressNode to set
   */
  public void setSuppressNode(Boolean suppressNode) {
    _suppressNode = suppressNode;
  }

  public Integer getDepthExpanded() {
    return _depthExpanded;
  }

  public void setDepthExpanded(Integer depthExpanded) {
    _depthExpanded = depthExpanded;
  }

  public Boolean isExposeAsAttribute() {
    return _exposeAsAttribute;
  }

  public void setExposeAsAttribute(Boolean exposeAsAttribute) {
    _exposeAsAttribute = exposeAsAttribute;
  }

  public String getMinDate() {
    return _minDate;
  }

  public void setMinDate(String minDate) {
    _minDate = minDate;
  }

  public String getMaxDate() {
    return _maxDate;
  }

  public void setMaxDate(String maxDate) {
    _maxDate = maxDate;
  }

  public Double getMin() {
    return _min;
  }

  public void setMin(Double min) {
    _min = min;
  }

  public Double getMax() {
    return _max;
  }

  public void setMax(Double max) {
    _max = max;
  }

  public Boolean getMultiPick() {
    return _multiPick;
  }

  public void setMultiPick(Boolean multiPick) {
    _multiPick = multiPick;
  }

  public Integer getMinSelectedCount() {
    return _minSelectedCount;
  }

  public void setMinSelectedCount(Integer minSelectedCount) {
    _minSelectedCount = minSelectedCount;
  }

  public Integer getMaxSelectedCount() {
    return _maxSelectedCount;
  }

  public void setMaxSelectedCount(Integer maxSelectedCount) {
    _maxSelectedCount = maxSelectedCount;
  }

  public Boolean getCountOnlyLeaves() {
    return _countOnlyLeaves;
  }

  public void setCountOnlyLeaves(Boolean countOnlyLeaves) {
    _countOnlyLeaves = countOnlyLeaves;
  }

  public void addHelp(WdkModelText help) {
    _helps.add(help);
  }

  public String getHelp() {
    return _help;
  }

 public void addVisibleHelp(WdkModelText visibleHelp) {
    _visibleHelps.add(visibleHelp);
  }

  public String getVisibleHelp() {
    return _visibleHelp;
  }

  public void setPrompt(String prompt) {
    _prompt = prompt;
  }

  public String getPrompt() {
    return _prompt;
  }
  
  /**
   * @return the interval
   */
  public Long getInterval() {
    return _interval;
  }

  /**
   * @param interval the interval to set
   */
  public void setInterval(Long interval) {
    _interval = interval;
  }

  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    super.excludeResources(projectId);

    for (WdkModelText help : _helps) {
      if (help.include(projectId)) {
        if (_help != null)
          throw new WdkModelException("More than one <help> are "
              + "defined in the paramRef '" + this.getTwoPartName());

        help.excludeResources(projectId);
        _help = help.getText();
      }
    }
    _helps = null;

 for (WdkModelText visibleHelp : _visibleHelps) {
      if (visibleHelp.include(projectId)) {
        if (_visibleHelp != null)
          throw new WdkModelException("More than one <visibleHelp> are "
              + "defined in the paramRef '" + this.getTwoPartName());

        visibleHelp.excludeResources(projectId);
        _visibleHelp = visibleHelp.getText();
      }
    }
    _visibleHelps = null;
	 }
}
