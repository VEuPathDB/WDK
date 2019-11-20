package org.gusdb.wdk.model.user.dataset.irods.icat;

import org.gusdb.wdk.model.WdkModelException;

import java.nio.file.Path;
import java.util.*;

import static java.util.Objects.requireNonNull;

/**
 * Represents an entry in iRODS which may be either a data object (file) or a
 * collection (directory).
 * <p>
 * Used to build an in memory representation of the full state of an iRODS
 * collection including any sub collections.  This in memory representation can
 * be constructed quickly and cheaply using a metadata query which allows WDK
 * code to ask some simple questions about the data in iRODS without making
 * heavy calls to the actual data store.
 *
 * @see ICatCollection
 * @see ICatDataObject
 */
public abstract class ICatNode {

  private static final String ERR_PATH_MISMATCH = "The path for the iRODS "
    + "collection %s (%s) does not align with the expected path for the node %s"
    + " (%s).";

  /**
   * Parent collection containing this node
   */
  private ICatCollection parent;

  /**
   * Path to this node in iRODS
   */
  private final Path path;

  /**
   * Last modified timestamp in seconds for this node in iRODS
   */
  private final long updated;

  /**
   * Custom metadata associated with this node in iRODS
   */
  private final Map<String, List<String>> metadata;

  ICatNode(final Path path, final long updated) {
    this.path = requireNonNull(path);
    this.updated = updated;
    this.metadata = new HashMap<>();
  }

  /*⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺*\
  ▏                                                        ▕
  ▏  Public API Methods                                    ▕
  ▏                                                        ▕
  \*⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽*/

  /**
   * Generates a text representation of this Node's contents.
   *
   * @return text representation of this Node's contents.
   */
  public abstract String render();

  /**
   * Append a metadata item to the metadata list for this node at the specified
   * key.
   *
   * @param key
   *   metadata identifier key
   * @param value
   *   metadata value string
   */
  public void addMetadata(final String key, final String value) {
    metadata.computeIfAbsent(key, __ -> new ArrayList<>()).add(value);
  }

  /**
   * Retrieves the metadata item at index 0 from the list of metadata items at
   * the given key.
   *
   * @param key
   *   key for which the metadata value should be returned.
   *
   * @return an option containing the requested value if such a value exists.
   */
  public Optional<String> getMetadata(final String key) {
    return getMetadata(key, 0);
  }

  /**
   * Retrieves the metadata item at the given index from the list of metadata
   * items at the given key.
   *
   * @param key
   *   key for which the metadata value should be returned.
   * @param index
   *   index for the value to retrieve
   *
   * @return an option containing the requested value if such a value exists.
   */
  private Optional<String> getMetadata(final String key, final int index) {
    return Optional.ofNullable(metadata.get(key))
      .filter(l -> l.size() > index)
      .map(l -> l.get(index));
  }

  /**
   * Removes all metadata stored in this node.
   */
  public void clearMetadata() {
    metadata.clear();
  }

  public long getLastModified() {
    return updated;
  }

  /**
   * @return the full path in iRODS to this {@code ICatNode}
   */
  public Path getPath() {
    return this.path;
  }

  /**
   * @return whether or not this {@code ICatNode} is an {@code ICatDataObject}
   *   rather than an {@code ICatCollection}.
   */
  public boolean isObject() {
    return false;
  }

  /**
   * @return whether or not this {@code ICatNode} is an {@code ICatCollection}
   *   rather than an {@code ICatDataObject}.
   */
  public boolean isCollection() {
    return false;
  }

  /**
   * @return an option which will contain this {@code ICatNode} downcast as an
   *   {@code ICatDataObject} if this node is a data object.
   */
  public Optional<ICatDataObject> asObject() {
    return Optional.empty();
  }

  /**
   * @return an option which will contain this {@code ICatNode} downcast as an
   *   {@code ICatCollection} if this node is a collection.
   */
  public Optional<ICatCollection> asCollection() {
    return Optional.empty();
  }

  /**
   * @return an option which will contain this {@code ICatNode}'s parent
   *   {@code ICatCollection} if such a parent exists.  If this node is the
   *   root, then it will have no parent.
   */
  public Optional<ICatCollection> getParent() {
    return Optional.ofNullable(parent);
  }

  /**
   * @return the name of this iRODS collection or data object
   */
  public String getName() {
    return getPath().getFileName().toString();
  }

  /*⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺*\
  ▏                                                        ▕
  ▏  Method Overrides                                      ▕
  ▏                                                        ▕
  \*⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽*/

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof ICatNode))
      return false;
    ICatNode iCatNode = (ICatNode) o;
    return getPath().equals(iCatNode.getPath());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getPath());
  }

  /*⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺*\
  ▏                                                        ▕
  ▏  Package Private API Methods                           ▕
  ▏                                                        ▕
  \*⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽*/

  /**
   * Updates the parent for this node to the given collection.
   *
   * @param parent
   *   the new parent collection for this {@code ICatNode}
   *
   * @return this {@code ICatNode}
   *
   * @throws WdkModelException
   *   if the input parent collection's path does not align wih the path the
   *   current {@code ICatNode}.
   */
  ICatNode setParent(final ICatCollection parent) throws WdkModelException {
    if (!getPath().getParent().equals(parent.getPath()))
      throw new WdkModelException(String.format(
        ERR_PATH_MISMATCH,
        parent.getName(),
        parent.getPath(),
        this.getName(),
        this.getPath()
      ));

    this.parent = parent;
    return this;
  }
}
