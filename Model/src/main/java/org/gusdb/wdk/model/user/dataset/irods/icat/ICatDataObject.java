package org.gusdb.wdk.model.user.dataset.irods.icat;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * Thin iCAT data based representation of a single data object or file in iRODS.
 */
public class ICatDataObject extends ICatNode {

  /**
   * Size of the data object's content in bytes
   */
  private final long bytes;

  /**
   * Constructs an iCAT data object representation with no link to a parent
   * collection.
   *
   * @param dir
   *   Full path to the collection containing this data object in iRODS
   * @param file
   *   Name of this data object specifically
   * @param bytes
   *   Size of this data object in bytes
   * @param updated
   *   timestamp of the last modification to this data object
   */
  public ICatDataObject(
    final Path   dir,
    final String file,
    final long   bytes,
    final long   updated
  ) {
    super(dir.resolve(file), updated);
    this.bytes = bytes;
  }

  /**
   * @return the size of this data object's contents in bytes
   */
  public long getSize() {
    return this.bytes;
  }

  @Override
  public boolean isObject() {
    return true;
  }

  @Override
  public Optional<ICatDataObject> asObject() {
    return Optional.of(this);
  }

  @Override
  public boolean equals(Object o) {
    if (!super.equals(o))
      return false;
    if (!(o instanceof ICatDataObject))
      return false;
    ICatDataObject that = (ICatDataObject) o;
    return bytes == that.bytes;
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), bytes);
  }

  @Override
  public String toString() {
    return "ICatDataObject{path=" + getPath() + ", size=" + bytes + '}';
  }

  @Override
  public String render() {
    return getName();
  }
}

