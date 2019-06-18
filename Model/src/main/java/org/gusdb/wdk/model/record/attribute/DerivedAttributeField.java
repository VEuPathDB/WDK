package org.gusdb.wdk.model.record.attribute;

import org.apache.log4j.Logger;
import org.gusdb.fgputil.FormatUtil;
import org.gusdb.fgputil.Named;
import org.gusdb.fgputil.SortDirection;
import org.gusdb.fgputil.functional.Result;
import org.gusdb.wdk.model.*;
import org.gusdb.wdk.model.answer.AnswerValue;
import org.gusdb.wdk.model.toolbundle.ColumnFilter;
import org.gusdb.wdk.model.toolbundle.ColumnToolConfig;

import java.io.PrintWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A derived attribute field is one whose value depends on the values of other
 * attribute fields
 *
 * @author rdoherty
 */
public abstract class DerivedAttributeField extends AttributeField {

  private static final Logger LOG = Logger.getLogger(DerivedAttributeField.class);

  public static final Pattern MACRO_PATTERN = Pattern.compile("\\$\\$([^$]+?)\\$\\$", Pattern.MULTILINE);

  private boolean checkedFilterDependency;
  private QueryColumnAttributeField filterDependency;

  /**
   * The dependent fields are the ones that are embedded in the current field.
   */
  protected abstract Collection<AttributeField> getDependencies() throws WdkModelException;

  /**
   * @param container
   *   the container to set
   */
  @Override
  public void setContainer(AttributeFieldContainer container) {
    _container = container;
  }

  @Override
  public Map<String, ColumnAttributeField> getColumnAttributeFields() throws WdkModelException {
    Map<String, ColumnAttributeField> leaves = new LinkedHashMap<>();

    for (AttributeField dependent : getDependencies())
      leaves.putAll(dependent.getColumnAttributeFields());

    return leaves;
  }

  protected String excludeModelText(List<WdkModelText> texts, String projectId,
      String textTag, boolean isRequired) throws WdkModelException {
    String source = "The " + getClass().getSimpleName() + " " + _container.getNameForLogging() + "." + getName();
    String selectedText = null;
    boolean hasText = false;
    for (WdkModelText text : texts) {
      if (text.include(projectId)) {
        if (hasText) {
          throw new WdkModelException(source + " has more than one " + textTag + " tag for project " + projectId);
        }
        else {
          selectedText = text.getText();
          hasText = true;
        }
      }
    }
    // check if all texts are excluded
    if (selectedText == null && isRequired) {
      throw new WdkModelException(source + " does not have a " + textTag + " tag for project " + projectId);
    }
    texts.clear();
    return selectedText;
  }

  /**
   * Several kinds of fields can embed other fields in their properties. This method
   * parses out the embedded fields from the text.
   *
   * @param text to parse for attribute name macros
   * @return map of attribute fields this field depends on
   */
  protected Map<String, AttributeField> parseFields(String text) throws WdkModelException {
    Map<String, AttributeField> children = new LinkedHashMap<>();
    Map<String, AttributeField> fields = _container.getAttributeFieldMap();

    for (String fieldName : parseFieldNames(text)) {
      if (!fields.containsKey(fieldName)) {
        throw new WdkModelException("Invalid field macro in attribute" + " [" + _name + "]: " + fieldName +
            "; the following macros are available: " + FormatUtil.arrayToString(fields.keySet().toArray()));
      }
      AttributeField field = fields.get(fieldName);
      children.put(fieldName, field);
      if (!children.containsKey(fieldName))
        children.put(fieldName, field);
    }
    return children;
  }

  public static Set<String> parseFieldNames(String text) {
    Matcher matcher = MACRO_PATTERN.matcher(text);
    Set<String> fieldNames = new LinkedHashSet<>();
    while (matcher.find()) {
      fieldNames.add(matcher.group(1));
    }
    return fieldNames;
  }

  @Override
  public void resolveReferences(WdkModel wdkModel) throws WdkModelException {
    super.resolveReferences(wdkModel);
    // check for dependency loops
    traverseDependency(this, new Stack<>());
  }

  /**
   * Make sure the attribute doesn't embed other attributes that may cause
   * cross-dependency.
   *
   * @param attribute
   *   the attribute to be checked
   * @param path
   *   the path from root to the attribute (attribute is not included)
   */
  private static void traverseDependency(DerivedAttributeField attribute, Stack<String> path)
      throws WdkModelException {
    checkBranch(path, attribute);
    // add this attribute and traverse branches
    path.push(attribute.getName());
    for (AttributeField dependency : attribute.getDependencies()) {
      if (dependency instanceof ColumnAttributeField) {
        checkBranch(path, dependency);
      }
      else if (dependency instanceof DerivedAttributeField) {
        traverseDependency((DerivedAttributeField)dependency, path);
      }
      else {
        throw new WdkModelException("A derived attribute can only depend on a column or another derived " +
            "attribute.  Derived attribute [" + attribute.getName() + "] depends on [" + dependency + "].");
      }
    }
    path.pop();
  }

  private static void checkBranch(Stack<String> path, AttributeField attribute) throws WdkModelException {
    if (path.contains(attribute.getName())) {
      // NOTE: if you got this exception on a LinkAttribute, make sure you are
      //   not self-referencing the LinkAttribute.  LinkAttributes need to only
      //   reference existing ColumnAttributes (i.e. values pulled from SQL)
      throw new WdkModelException("Attribute '" + attribute.getName() +
          "' has circular reference: " +  FormatUtil.NL + "   Previous = [ " +
          FormatUtil.join(path.toArray(), ", ") + " ]");
    }
  }

  @Override
  protected void printDependencyContent(PrintWriter writer, String indent) throws WdkModelException {
    List<AttributeField> dependencies = new ArrayList<>(getDependencies());
    Named.sortByName(dependencies, SortDirection.ASC, true);
    writer.println(indent + "<dependOn count=\"" + dependencies.size() + "\">");
    for (AttributeField dependency : dependencies) {
      dependency.printDependency(writer, indent + WdkModel.INDENT);
    }
    writer.println(indent + "</dependOn>");
  }

  public static String replaceMacrosWithAttributeValues(String text, AttributeValueContainer container, String label)
      throws WdkModelException, WdkUserException {
    Map<String, Object> values = new LinkedHashMap<>();
    Map<String, AttributeField> fields = container.getAttributeFieldMap();

    Matcher matcher = MACRO_PATTERN.matcher(text);
    while (matcher.find()) {
      String fieldName = matcher.group(1);

      if (!values.containsKey(fieldName)) {

        if (!fields.containsKey(fieldName)) {
          LOG.warn("Invalid field macro in " + label + ": " + fieldName);
          continue;
        }

        AttributeValue value = container.getAttributeValue(fieldName);
        values.put(fieldName, value.toString());
      }
    }
    return Utilities.replaceMacros(text, values);
  }

  /**
   * @return whether or not this field has a filterable dependency field.
   *
   * @see #getFilterDependencyField()
   */
  @Override
  public boolean isFilterable() {
    return getFilterDependencyField().isPresent();
  }

  /**
   * Delegates to the {@code getFilter} method of this field's filterable
   * dependency (if present)
   *
   * @see #getFilterDependencyField()
   * @see AttributeField#getFilter(String)
   */
  @Override
  public Optional<ColumnFilter> getFilter(final String name) {
    return getFilterDependencyField().flatMap(af -> af.getFilter(name));
  }

  /**
   * Delegates to the {@code prepareFilter} method of this field's filterable
   * dependency (if present)
   *
   * @see #getFilterDependencyField()
   * @see AttributeField#prepareFilter(String, AnswerValue, ColumnToolConfig)
   */
  @Override
  public Optional<ColumnFilter> prepareFilter(
    final String name,
    final AnswerValue val,
    final ColumnToolConfig config
  ) throws WdkModelException {
    var field = getFilterDependencyField();
    if (field.isEmpty())
      return Optional.empty();

    return field.get().prepareFilter(name, val, config);
  }

  /**
   * Delegates to the {@code getColumnFilterNames} method of this field's
   * filterable dependency (if present)
   *
   * @see #getFilterDependencyField()
   * @see AttributeField#getColumnFilterNames()
   */
  @Override
  public Collection<String> getColumnFilterNames() {
    return getFilterDependencyField()
      .map(QueryColumnAttributeField::getColumnFilterNames)
      .orElse(Collections.emptyList());
  }

  /**
   * Returns an option of this {@code DerivedAttributeField}'s filterable
   * dependency field.
   * <p>
   * The filter dependency field is the actual backing field that will be used
   * by column filters if the client chooses to apply a column filter to this
   * {@code DerivedAttributeField}.
   * <p>
   * This {@code DerivedAttributeField} and it's filterable dependency must meet
   * the following criteria:
   * <p>
   * <ul>
   * <li>This field has exactly 1 dependency
   * <li>This field's 1 dependency is an instance of
   * {@link QueryColumnAttributeField}
   * <li>this field's 1 dependency is itself filterable
   * </ul>
   *
   * @return an option containing either the filterable field if such a field
   *   exists and satisfies the above criteria, or none if the above rules are
   *   not met.
   */
  public Optional<QueryColumnAttributeField> getFilterDependencyField() {
    if (checkedFilterDependency)
      return Optional.ofNullable(filterDependency);

    checkedFilterDependency = true;

    var deps = Result.of(this::getDependencies)
      .value()
      .orElse(Collections.emptyList());

    if (deps.size() != 1)
      return Optional.empty();

    var dep = deps.iterator().next();

    if (!(dep instanceof QueryColumnAttributeField) || !dep.isFilterable())
      return Optional.empty();

    filterDependency = (QueryColumnAttributeField) dep;

    return Optional.of(filterDependency);
  }
}
