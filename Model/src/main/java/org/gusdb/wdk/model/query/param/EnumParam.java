package org.gusdb.wdk.model.query.param;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.query.Query;
import org.gusdb.wdk.model.user.User;

/**
 * The enumParam represents a list of param values declared in the model that user can choose from.
 *
 * @author jerric
 *
 */
public class EnumParam extends AbstractEnumParam {

  private static final Logger LOG = Logger.getLogger(EnumParam.class);

  private List<EnumItemList> _enumItemLists;
  private EnumItemList _enumItemList;

  public EnumParam() {
    _enumItemLists = new ArrayList<>();
  }

  public EnumParam(EnumParam param) {
    super(param);
    _enumItemList = param._enumItemList;
  }

  // ///////////////////////////////////////////////////////////////////
  // /////////// Public properties ////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////

  public void addEnumItemList(EnumItemList enumItemList) {
    _enumItemLists.add(enumItemList);
  }

  // ///////////////////////////////////////////////////////////////////
  // /////////// Protected properties ////////////////////////////////////
  // ///////////////////////////////////////////////////////////////////

  @Override
  public EnumParamVocabInstance getVocabInstance(User user, Map<String, String> dependedParamValues)
      throws WdkModelException {
    LOG.trace("Entering createEnumParamCache(" + FormatUtil.prettyPrint(dependedParamValues) + ")");
    Set<Param> dependedParams = getDependedParams();
    EnumParamVocabInstance cache = new EnumParamVocabInstance(dependedParamValues);
    EnumItem[] enumItems = _enumItemList.getEnumItems();
    for (EnumItem item : enumItems) {
      String term = item.getTerm();
      String display = item.getDisplay();
      String parentTerm = item.getParentTerm();
      boolean skip = false;

      // escape the term & parentTerm
      // term = term.replaceAll("[,]", "_");
      // if (parentTerm != null)
      // parentTerm = parentTerm.replaceAll("[,]", "_");
      if (term.indexOf(',') >= 0 && dependedParams != null)
        throw new WdkModelException(this.getFullName() + ": The term cannot contain comma: '" + term + "'");
      if (parentTerm != null && parentTerm.indexOf(',') >= 0)
        throw new WdkModelException(this.getFullName() + ": The parent term cannot contain" + "comma: '" +
            parentTerm + "'");

      if (isDependentParam()) {
        // if this is a dependent param, only include items that are
        // valid for the current depended value
        skip = !item.isValidFor(dependedParamValues);
      }

      if (!skip) {
        cache.addTermValues(term, item.getInternal(), display, parentTerm);
      }
    }
    // check if the result is empty
    if (cache.isEmpty())
      throw new WdkEmptyEnumListException("The EnumParam [" + getFullName() + "] doesn't have any values.");

    initTreeMap(cache);
    LOG.trace("Leaving createEnumParamCache(" + FormatUtil.prettyPrint(dependedParamValues) + ")");
    return cache;
  }

  @Override
  public void excludeResources(String projectId) throws WdkModelException {
    super.excludeResources(projectId);

    // exclude enum items
    boolean hasEnumList = false;
    for (EnumItemList itemList : _enumItemLists) {
      if (itemList.include(projectId)) {
        if (hasEnumList) {
          throw new WdkModelException("enumParam " + getFullName() +
              " has more than one <enumList> for project " + projectId);
        }
        else {
          EnumItem[] enumItems = itemList.getEnumItems();
          if (enumItems.length == 0)
            throw new WdkModelException("enumParam '" + _name + "' has zero items");

          itemList.setParam(this);
          itemList.excludeResources(projectId);
          _enumItemList = itemList;

          hasEnumList = true;
        }
      }
    }
    _enumItemLists = null;
    if (_enumItemList == null || _enumItemList.getEnumItems().length == 0)
      throw new WdkModelException("No enum items available in enumParam " + getFullName());
  }

  @Override
  public void resolveReferences(WdkModel model) throws WdkModelException {
    super.resolveReferences(model);

    _enumItemList.resolveReferences(model);

    StringBuffer sb = new StringBuffer();
    EnumItem[] enumItems = _enumItemList.getEnumItems();
    for (EnumItem item : enumItems) {
      if (item.isDefault()) {
        if (sb.length() > 0) {
          // single pick default should be singular value
          if (!_multiPick)
            break;
          sb.append(",");
        }
        sb.append(item.getTerm());
      }
    }
    if (sb.length() > 0) {
      setDefault(sb.toString());
    }
  }

  @Override
  public Param clone() {
    return new EnumParam(this);
  }

  @Override
  public Set<String> getContainedQueryFullNames() {
    Set<String> names = new HashSet<>();
    return names;
  }

  @Override
  public List<Query> getQueries() {
    return new ArrayList<>();
  }

}
