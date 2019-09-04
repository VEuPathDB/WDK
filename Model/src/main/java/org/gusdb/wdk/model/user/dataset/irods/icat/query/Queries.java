package org.gusdb.wdk.model.user.dataset.irods.icat.query;

import org.gusdb.fgputil.TraceLog;
import org.gusdb.wdk.model.WdkModelException;
import org.gusdb.wdk.model.user.dataset.irods.icat.query.ICat.Column;
import org.irods.jargon.core.query.IRODSGenQuery;

import java.nio.file.Path;

/**
 * Predefined iRODS iCAT query builders for standard use cases for user dataset
 * actions.
 */
class Queries {

  private static final TraceLog TRACE = new TraceLog(Queries.class);

  /**
   * Configures an iRODS iCAT query to fetch information about all data
   * objects under a given path or any of its child paths.
   * <p>
   * <b>NOTE</b>: This query result will not include empty collections.
   *
   * @param path
   *   path in iRODS to search for data objects
   *
   * @return an iCAT query configured to locate data objects
   *   recursively in the given path.
   */
  static IRODSGenQuery allObjectsRecursive(final Path path)
  throws WdkModelException {
    return TRACE.start(path).end(baseObjectQuery()
      .whereLike(Column.COLLECTION_NAME, wildcard(path))
      .build());
  }

  /**
   * Configures an iRODS iCAT query to fetch information about all collections
   * under a given path or any of its child paths.
   * <p>
   * <b>NOTE</b>: This query result will not include empty data objects.
   *
   * @param path
   *   path in iRODS to search for collections
   *
   * @return an iCAT query configured to locate collections
   *   recursively in the given path.
   */
  static IRODSGenQuery allCollectionsRecursive(final Path path)
  throws WdkModelException {
    return TRACE.start(path).end(baseCollectionQuery()
      .whereLike(Column.COLLECTION_NAME, wildcard(path))
      .build());
  }

  /**
   * Configures an iRODS iCAT query to fetch all available metadata for a single
   * specific iRODS collection located at the given path.
   *
   * @param path
   *   path to the collection in iRODS fow which all metadata should be
   *   retrieved
   *
   * @return a prepared query for use with the iRODS library.
   *
   * @throws WdkModelException
   *   if the query could not be constructed do to an iRODS library error.
   */
  static IRODSGenQuery allCollectionMeta(final Path path)
  throws WdkModelException {
    return TRACE.start(path).end(new ICatQueryBuilder()
      .select(Column.COLLECTION_META_KEY)
      .select(Column.COLLECTION_META_VALUE)
      .whereEqual(Column.COLLECTION_NAME, path.toString())
      .build());
  }

  /**
   * Configures an iRODS iCAT query to fetch all available metadata for a single
   * specific iRODS data object located at the given path.
   *
   * @param path
   *   path to the data object in iRODS fow which all metadata should be
   *   retrieved
   *
   * @return a prepared query for use with the iRODS library.
   *
   * @throws WdkModelException
   *   if the query could not be constructed do to an iRODS library error.
   */
  static IRODSGenQuery allDataObjectMeta(final Path path)
  throws WdkModelException {
    return TRACE.start(path).end(new ICatQueryBuilder()
      .select(Column.DATA_OBJECT_META_KEY)
      .select(Column.DATA_OBJECT_META_VALUE)
      .whereEqual(Column.COLLECTION_NAME, path.getParent().toString())
      .whereEqual(Column.DATA_OBJECT_NAME, path.getFileName().toString())
      .build());
  }

  /**
   * Configures an iRODS iCAT query to fetch all available metadata for the
   * collection located at the given path as well as the metadata for all child
   * collections.
   *
   * @param path
   *   path to the collection for which all metadata should be retrieved
   *
   * @return a prepared query for use with the iRODS library.
   *
   * @throws WdkModelException
   *   if the query could not be constructed do to an iRODS library error.
   */
  static IRODSGenQuery allCollectionMetaRecursive(final Path path)
  throws WdkModelException {
    return TRACE.start(path).end(new ICatQueryBuilder()
      .select(Column.COLLECTION_META_KEY)
      .select(Column.COLLECTION_META_VALUE)
      .select(Column.COLLECTION_NAME)
      .whereLike(Column.COLLECTION_NAME, wildcard(path))
      .build());
  }

  /**
   * Configures an iRODS iCAT query to fetch all available metadata for all data
   * objects under the collection located at the given path as well as all data
   * objects in any child collection under the given path.
   *
   * @param path
   *   path to the collection for which all data object metadata should be
   *   retrieved
   *
   * @return a prepared query for use with the iRODS library.
   *
   * @throws WdkModelException
   *   if the query could not be constructed do to an iRODS library error.
   */
  static IRODSGenQuery allDataObjectMetaRecursive(final Path path)
  throws WdkModelException {
    return TRACE.start(path).end(new ICatQueryBuilder()
      .select(Column.DATA_OBJECT_META_KEY)
      .select(Column.DATA_OBJECT_META_VALUE)
      .select(Column.DATA_OBJECT_NAME)
      .select(Column.COLLECTION_NAME)
      .whereLike(Column.COLLECTION_NAME, wildcard(path))
      .build());
  }

  static IRODSGenQuery directChildCollections(final Path path)
  throws WdkModelException {
    return TRACE.start(path).end(baseCollectionQuery()
      .whereEqual(Column.PARENT_COLLECTION, path)
      .build());
  }

  static IRODSGenQuery directChildObjects(final Path path)
  throws WdkModelException {
    return TRACE.start(path).end(baseObjectQuery()
      .whereEqual(Column.COLLECTION_NAME, path)
      .build());
  }

  static IRODSGenQuery specificCollection(final Path path)
  throws WdkModelException {
    return TRACE.start(path).end(baseCollectionQuery()
      .whereEqual(Column.COLLECTION_NAME, path)
      .build());
  }

  static IRODSGenQuery specificObject(final Path path)
  throws WdkModelException {
    return TRACE.start(path).end(baseObjectQuery()
      .whereEqual(Column.COLLECTION_NAME, path.getParent())
      .whereEqual(Column.DATA_OBJECT_NAME, path.getFileName())
      .build());
  }

  private static String wildcard(final Path p) {
    return TRACE.start(p).end(p.toString() + "%");
  }

  private static ICatQueryBuilder baseCollectionQuery() {
    return TRACE.start().end(new ICatQueryBuilder()
      .select(Column.COLLECTION_NAME)
      .select(Column.COLLECTION_LAST_MODIFIED));
  }

  private static ICatQueryBuilder baseObjectQuery() {
    return TRACE.start().end(new ICatQueryBuilder()
      .select(Column.DATA_OBJECT_NAME)
      .select(Column.DATA_OBJECT_SIZE)
      .select(Column.COLLECTION_NAME)
      .select(Column.DATA_OBJECT_LAST_MODIFIED));
  }
}
