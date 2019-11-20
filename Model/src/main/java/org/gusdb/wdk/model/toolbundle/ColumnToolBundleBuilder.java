package org.gusdb.wdk.model.toolbundle;

import org.gusdb.wdk.model.WdkModel;
import org.gusdb.wdk.model.WdkModelException;

public interface ColumnToolBundleBuilder {

  /**
   * Gets the name of the tool bundle to be built.
   *
   * Tool bundle names must be unique in scope of the WDK.
   */
  String getName();

  /**
   * Sets the name of the tool bundle to be built.
   *
   * Tool bundle names must be unique in scope of the WDK.
   *
   * @param name unique name of the tool bundle.
   */
  void setName(String name);

  /**
   * Adds a new unique bundle tool to this tool bundle.
   *
   * @param tool
   *   bundle tool to add.
   */
  @SuppressWarnings("unused") // Referenced by name in ModelXmlParser
  void addTool(ColumnToolSetBuilder tool) throws WdkModelException;

  /**
   * Validates and resolves any internal references and return a constructed
   * {@link ColumnToolBundle}.
   *
   * @param wdk
   *   WdkModel, can be used to retrieve additional information or
   *   implementations of named references.
   *
   * @throws WdkModelException
   *   thrown if any error is encountered while attempting to resolve model
   *   references.
   */
  ColumnToolBundle build(WdkModel wdk) throws WdkModelException;
}
