package org.gusdb.wdk.model.user.dataset.irods.icat;

import org.gusdb.wdk.model.WdkModelException;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Stream;

/**
 * In memory tree representation of an iRODS collection and all its contents.
 */
public class ICatCollection extends ICatNode {

  private static final String
    ERR_OBJ_AS_COLL = "Invalid path \"%s\", object %s is not a collection.",
    ERR_MERGE_TYPE_MISMATCH = "Merge failed. Attempted to merge node '%s' of type "
      + "'%s' into node '%s' of type '%s' at path '%s'.",
    ERR_PATH_CONFLICT = "Attempted to put 2 different nodes with the same"
      + "name (%s) into a single collection (%s)";

  private static final Path ROOT = Paths.get("/");

  private final HashMap<String, ICatNode> children;

  /**
   * Constructs a 'root' collection tree element.
   * <p>
   * Sub elements cannot be constructed directly, they will be constructed
   * internally as necessary as paths are added to this tree.
   */
  public ICatCollection() {
    this(ROOT, -1);
  }

  /**
   * Constructs a non-root collection tree element.
   *
   * @param fullPath
   *   full path to this collection
   * @param updated
   *   last modified timestamp for this collection
   */
  public ICatCollection(final Path fullPath, final long updated) {
    super(fullPath, updated);
    this.children = new HashMap<>();
  }

  /*⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺*\
  ▏                                                        ▕
  ▏  Public API Methods                                    ▕
  ▏                                                        ▕
  \*⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽*/

  /**
   * Recursively checks and returns whether or not this collection contains an
   * element at the given path.
   *
   * @param path
   *   Path to check
   *
   * @return true if the full path given points to an existing element in this
   *   collection or one of it's child collections.
   */
  public boolean contains(final Path path) {
    return get(fixPath(path)).isPresent();
  }

  /**
   * Checks whether or not this {@code ICatCollection} contains any direct child
   * nodes with the given {@code name}.
   *
   * @param name
   *   the name of the object to check this collection for
   *
   * @return whether or not this {@code ICatCollection} contains a direct child
   *   node with the given {@code name}
   */
  public boolean contains(final String name) {
    return children.containsKey(name);
  }

  /**
   * Recursively checks and returns whether or not this collection contains a
   * child collection at the given path.
   *
   * @param path
   *   Path to check
   *
   * @return true if the full path given points to an existing collection in
   *   this or one of it's child collections.
   */
  public boolean containsCollection(final Path path) {
    return getCollection(fixPath(path)).isPresent();
  }

  /**
   * Recursively checks and returns whether or not this collection contains a
   * child data object at the given path.
   *
   * @param path
   *   Path to check
   *
   * @return true if the full path given points to an existing data object in
   *   this or one of it's child collections.
   */
  public boolean containsObject(final Path path) {
    return getObject(fixPath(path)).isPresent();
  }

  /**
   * Retrieves the collection located at the given path from within this
   * collection.
   *
   * @param path
   *   Path to the collection to retrieve
   *
   * @return an option containing the collection pointed to by the given path,
   *   if such a collection exists, or none if the path does not point to an
   *   existing element or the existing element is not a collection.
   */
  public Optional<ICatCollection> getCollection(final Path path) {
    return get(fixPath(path)).flatMap(ICatNode::asCollection);
  }

  /**
   * Retrieves the data object located at the given path from within this
   * collection.
   *
   * @param path
   *   Path to the data object to retrieve
   *
   * @return an option containing the data object pointed to by the given path,
   *   if such a data object exists, or none if the path does not point to an
   *   existing element or the existing element is not a data object.
   */
  public Optional<ICatDataObject> getObject(final Path path) {
    return get(fixPath(path)).flatMap(ICatNode::asObject);
  }

  /**
   * Retrieves the element located at the given path from within this
   * collection.
   *
   * @param path
   *   Path to the element to retrieve
   *
   * @return an option containing the element pointed to by the given path, if
   *   such an element exists, or none if the path does not point to an existing
   *   element.
   */
  public Optional<ICatNode> get(final Path path) {
    return get(this, fixPath(path), 0);
  }

  /**
   * @return whether or not this {@code ICatCollection} contains any child
   *   nodes.
   */
  public boolean isEmpty() {
    return children.isEmpty();
  }

  /**
   * Adds the given element to the collection, creating any child collections
   * necessary to construct the path returned by the given element's {@link
   * ICatNode#getPath() getPath()} method.
   *
   * @param object
   *   Element to add to this collection
   * @throws WdkModelException
   *   If the path returned by the input parameter's {@link ICatNode#getPath()
   *   getPath()} method cannot be constructed.
   */
  public void push(final ICatNode object) throws WdkModelException {
    push(object, fixPath(object.getPath()), 0);
  }

  /**
   * Recursively merges the given {@code ICatCollection} into this one.
   *
   * @param col
   *   collection to merge into the current collection
   *
   * @throws WdkModelException
   *   if both collections contain different data objects at the same path
   */
  public void merge(final ICatCollection col) throws WdkModelException {
    if (col == this || col.isEmpty()) {
      return;
    }

    mergeCollections(this, col);
  }

  /**
   * Traverses the collection tree to the node the given path points to and
   * removes that node from its parent collection.
   * <p>
   * If the path does not point to any node in this tree, no action will be
   * taken.
   *
   * @param path
   *   the path to the node in the tree to remove.
   */
  public void remove(final Path path) {
    remove(this, fixPath(path), 0);
  }

  /**
   * Removes the direct child node from this {@code ICatCollection} with the
   * given name.
   * <p>
   * If this collection does not directly contain a node with the given name,
   * no action will be taken.
   *
   * @param name
   *   the name of the direct child node to remove from this {@code
   *   ICatCollection}.
   */
  public void remove(final String name) {
    children.remove(name);
  }

  /**
   * @return a stream over all direct children of this node.
   *
   * @see #streamCollectionsShallow()
   * @see #streamObjectsShallow()
   */
  public Stream<ICatNode> streamShallow() {
    return children.values().stream();
  }

  /**
   * @return a stream over any direct children of this node that are
   *   collections.
   *
   * @see #streamShallow()
   * @see #streamObjectsShallow()
   */
  public Stream<ICatCollection> streamCollectionsShallow() {
    return streamShallow().filter(ICatNode::isCollection)
      .map(ICatCollection.class::cast);
  }

  /**
   * @return a stream over any direct children of this node that are data
   *   objects.
   *
   * @see #streamShallow()
   * @see #streamCollectionsShallow()
   */
  public Stream<ICatDataObject> streamObjectsShallow() {
    return streamShallow().filter(ICatNode::isObject)
      .map(ICatDataObject.class::cast);
  }

  /**
   * @return a stream over all sub nodes contained in this collection or its
   *   child collections.
   */
  public Stream<ICatNode> streamRecursive() {
    return Stream.concat(streamShallow(), streamCollectionsShallow()
      .flatMap(ICatCollection::streamRecursive));
  }

  /**
   * @return a stream over all sub collections contained in this collection or
   *   its sub collections.
   */
  public Stream<ICatCollection> streamCollectionsRecursive() {
    return Stream.concat(streamCollectionsShallow(), streamCollectionsShallow()
      .flatMap(ICatCollection::streamCollectionsRecursive));
  }

  /**
   * @return a stream over all data objects contained in this collection or its
   *   sub collections.
   */
  public Stream<ICatDataObject> streamObjectsRecursive() {
    return Stream.concat(streamObjectsShallow(), streamCollectionsShallow()
      .flatMap(ICatCollection::streamObjectsRecursive));
  }

  /*⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺*\
  ▏                                                        ▕
  ▏  Method Overrides                                      ▕
  ▏                                                        ▕
  \*⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽*/

  @Override
  public Optional<ICatCollection> asCollection() {
    return Optional.of(this);
  }

  @Override
  public boolean isCollection() {
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (!super.equals(o))
      return false;
    if (!(o instanceof ICatCollection))
      return false;
    ICatCollection that = (ICatCollection) o;
    return children.equals(that.children);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), children);
  }

  @Override
  public String toString() {
    return "ICatCollection{path=" + getPath()
      + ", children="+ children.size() + "}";
  }

  @Override
  public String render() {
    final String               name  = getName();
    final StringBuilder        out   = new StringBuilder();
    final List<ICatCollection> next  = new ArrayList<>();
    final Iterator<String>     keyIt = children.keySet().iterator();

    out.append(name.isEmpty() ? "/" : name + '/')
      .append("\n");

    while (keyIt.hasNext()) {
      final ICatNode child = children.get(keyIt.next());

      if (child.isCollection()) {
        next.add((ICatCollection) child);
        continue;
      }

      out.append("  |- ")
        .append(child.render())
        .append("\n");

      if (!keyIt.hasNext() && !next.isEmpty())
        out.append("  |\n");
    }

    final Iterator<ICatCollection> colIt = next.iterator();

    while (colIt.hasNext()) {
      final ICatCollection child = colIt.next();
      final boolean last = !colIt.hasNext();
      final String indent = last ? "     " : "  |  ";
      out.append("  |- ")
        .append(child.render().replaceAll("(?m)^", indent).substring(5))
        .append(last ? "\n" : "  |\n");
    }

    return out.toString();
  }

  /*⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺*\
  ▏                                                        ▕
  ▏  Internal API Methods                                  ▕
  ▏                                                        ▕
  \*⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽*/

  private Path fixPath(final Path path) {
    final Path p = getPath();
    return path.startsWith(p) ? p.relativize(path) : path;
  }

  /*⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺⎺*\
  ▏                                                        ▕
  ▏  Stateless Internal API Methods                        ▕
  ▏                                                        ▕
  \*⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽⎽*/

  /**
   * Recursively traverses the tree following the given path to try and locate
   * an element at the given path.
   *
   * @param col
   *   collection to check for the next path element
   * @param path
   *   Full path to the node
   * @param index
   *   Current name in the path to check
   *
   * @return an option which will contain the node located at the given path, if
   *   such a node exists
   */
  private static Optional<ICatNode> get(
    final ICatCollection col,
    final Path           path,
    final int            index
  ) {
    final int len = path.getNameCount();
    if (index == len)
      return Optional.empty();

    final ICatNode subPath = col.children.get(pathElementName(path, index));

    if (subPath == null)
      return Optional.empty();

    if (index + 1 == len)
      return Optional.of(subPath);

    return subPath.isCollection()
      ? get((ICatCollection) subPath, path, index+1)
      : Optional.empty();
  }

  /**
   * Traverses the tree, creating collections as necessary to reach the
   * collection directly containing the given node, then appends the node to
   * that collection.
   *
   * @param obj
   *   element to add to the tree
   * @param path
   *   location in the tree to place {@code obj}
   * @param index
   *   index of the current name in the path to check
   *
   * @throws WdkModelException
   *   If the path returned by the input element's {@link ICatNode#getPath()
   *   getPath()} method cannot be constructed.
   * @throws WdkModelException
   *   if the given node conflicts with a non-equal element at the same path.
   *   See {@link ICatDataObject#equals(Object)}
   */
  private void push(final ICatNode obj, final Path path, final int index)
  throws WdkModelException {
    final int    next = index+1;
    final String cur  = pathElementName(path, index);

    // If we've reached the end of the input path
    // then attempt to append the element to the current collection
    if (next == path.getNameCount()) {
      // if the current collection already contains a node with this name
      //   and that existing node is not the same as the node we are pushing
      // then throw a path conflict exception
      if (this.children.containsKey(cur) && !obj.equals(this.children.get(cur)))
        throw new WdkModelException(String.format(ERR_PATH_CONFLICT,
          this.getPath().getFileName(), path));
      // else append the node and stop recursing
      this.children.put(cur, obj.setParent(this));
      return;
    }

    final ICatNode child = children.get(cur);
    final ICatCollection irc;

    // if we've reached a child node that already exists
    // then confirm that it is a collection
    // else create a new empty collection and append it to this collection
    if (child != null) {

      // Attempt to downcast the child node as a collection or throw an
      // exception for attempting to use a data object as a collection
      irc = child.asCollection().orElseThrow(() -> new WdkModelException(
        String.format(ERR_OBJ_AS_COLL, path, cur)));
    } else {
      irc = new ICatCollection(getPath().resolve(cur), -1);
      this.children.put(cur, irc.setParent(this));
    }

    irc.push(obj, path, index + 1);
  }

  /**
   * Appends all nodes from tree {@code from} to tree {@code into} if they do
   * not already exist in that tree.
   * <p>
   * Warnings:
   * <ul>
   * <li>This method does not guarantee that metadata in tree {@code from} will
   *   be copied to tree {@code into}.
   * <li>On collision of data objects, this method only confirms that the data
   *   objects are of the same size when deciding whether or not they are the
   *   same.
   * </ul>
   *
   * @param into
   *   tree that will be merged into
   * @param from
   *   source tree that will be merged in
   *
   * @throws WdkModelException
   *   if both trees contain nodes of different types at the same path
   * @throws WdkModelException
   *   if both trees contain data objects of different sizes at the same path
   */
  private static void mergeCollections(
    final ICatCollection into,
    final ICatCollection from
  ) throws WdkModelException {
    for (final Entry<String, ICatNode> fEntry : from.children.entrySet()) {
      final String   fKey   = fEntry.getKey();
      final ICatNode fChild = fEntry.getValue();
      final ICatNode tChild = into.children.get(fKey);

      // if the collection being merged into does not have this entry
      // then add the from branch to the into collection and halt
      // else attempt to merge children
      if (tChild == null) {
        into.children.put(fKey, fChild);
        continue;
      }

      // if the types of the child nodes do not match
      // then throw a merge type mismatch
      // else attempt to merge children
      if (tChild.isCollection() != fChild.isCollection())
        throw new WdkModelException(String.format(
          ERR_MERGE_TYPE_MISMATCH,
          fChild.getName(),
          fChild.getClass().getSimpleName(),
          tChild.getName(),
          tChild.getClass().getSimpleName(),
          fChild.getPath()
        ));

      // if child nodes are objects
      // then check if they are the same object
      // else merge the two collections
      if (tChild.isObject()) {
        // if child nodes are not the same
        // then throw an exception for path conflict
        // else nothing to do, files are the same
        if (!into.equals(from))
          throw new WdkModelException(String.format(
            ERR_PATH_CONFLICT,
            into.getPath().getFileName(),
            from.getPath()
          ));
      } else {
        mergeCollections((ICatCollection) tChild, (ICatCollection) fChild);
      }
    }
  }

  /**
   * Recursively traverses the tree to remove the node at the specified
   * {@code path} if it exists.
   * <p>
   * If the given path is not present in this tree, no action will be taken.
   *
   * @param col
   *   collection potentially directly or indirectly containing the node to
   *   remove
   * @param path
   *   full path to the node that should be removed
   * @param index
   *   index of the current path element that is being examined while traversing
   *   the tree.
   */
  private static void remove(
    final ICatCollection col,
    final Path           path,
    final int            index
  ) {
    final int    next = index + 1;
    final String cur  = pathElementName(path, index);

    if (next == path.getNameCount()) {
      col.children.remove(cur);
      return;
    }

    final ICatNode child = col.children.get(cur);

    if (child == null || child.isObject())
      return;

    remove((ICatCollection) child, path, next);
  }

  /**
   * Returns the name of the path segment at the given index as a string.
   *
   * @param path
   *   path from which to retrieve the individual segment
   * @param index
   *   index of the segment to retrieve
   *
   * @return the string name of the path segment at the given index.
   */
  private static String pathElementName(final Path path, final int index) {
    return path.getName(index).toString();
  }
}
