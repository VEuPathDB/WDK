package org.gusdb.wdk.model.toolbundle;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

import java.util.*;

import static java.lang.String.format;
import static org.gusdb.fgputil.json.JsonUtil.Jackson;

/**
 * Collection of attribute field tool bundles indexed by their unique names.
 * <p>
 * If more than one ColumnToolBundle is found with the same name an exception
 * will be thrown at model parse time.
 */
public class ColumnToolBundles {
  private static final String ERR_DUPLICATE = "More than one columnToolBundle "
    + "is defined with the name \"%s\".";

  private final Collection<ColumnToolBundleBuilder> builders;

  private final Map<String, ColumnToolBundle> bundles;

  public ColumnToolBundles() {
    builders = new ArrayList<>();
    bundles = new HashMap<>();
  }

  /**
   * Adds a new unique tool bundle to this tool bundle collection.
   *
   * @param bundle
   *   New tool bundle to to this tool bundle set.
   */
  @SuppressWarnings("unused") // Referenced by name in ModelXmlParser
  public void addBundle(final ColumnToolBundleBuilder bundle) {
    builders.add(bundle);
  }

  /**
   * Returns an optional tool bundle retrieved by name.
   *
   * @param name
   *   Name of the tool bundle to retrieve
   *
   * @return an option of {@link ColumnToolBundle} which will be empty if no
   * tool bundle was found with the given name.
   */
  public Optional<ColumnToolBundle> getBundle(final String name) {
    return Optional.ofNullable(bundles.get(name));
  }

  /**
   * Resolve any references set from the model XML to their backing
   * implementations.
   *
   * @param wdk
   *   WdkModel, can be used to retrieve additional information or
   *   implementations of named references.
   *
   * @throws WdkModelException
   *   thrown when a tool bundle is added that has a name conflicting with
   *   another existing tool bundle or if any error is encountered while
   *   attempting to resolve model references.
   */
  public void resolveReferences(final WdkModel wdk) throws WdkModelException {
    for (final ColumnToolBundleBuilder builder : builders) {
      final ColumnToolBundle tmp = builder.build(wdk);
      if (bundles.containsKey(tmp.getName()))
        throw new WdkModelException(format(ERR_DUPLICATE, tmp.getName()));
      bundles.put(tmp.getName(), tmp);
    }
    builders.clear();
  }

  @Override
  public String toString() {
    return Jackson.createObjectNode()
      .set(
        getClass().getSimpleName(), Jackson.createObjectNode()
          .putPOJO("builders", builders)
          .putPOJO("bundles", bundles)
      )
      .toString();
  }
}
