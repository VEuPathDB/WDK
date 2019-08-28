package org.gusdb.wdk.model.user.dataset.irods.icat.query;

import org.irods.jargon.core.query.RodsGenQueryEnum;

/**
 * Details and helpers for working with the iRODS iCAT database.
 */
interface ICat {
  interface Prefs {
    int RESULT_FETCH_SIZE = 1_000;
  }

  class Column {
    static final RodsGenQueryEnum
      // Name of the data object (file) in iRODS
      DATA_OBJECT_NAME = RodsGenQueryEnum.COL_DATA_NAME,

      // Byte size of the data object (file) in iRODS
      DATA_OBJECT_SIZE = RodsGenQueryEnum.COL_DATA_SIZE,

      // Last modified time for a data object
      DATA_OBJECT_LAST_MODIFIED = RodsGenQueryEnum.COL_D_MODIFY_TIME,

      // iCAT metadata key/value key for a data object
      DATA_OBJECT_META_KEY = RodsGenQueryEnum.COL_META_DATA_ATTR_NAME,

      // iCAT metadata key/value value for a data object
      DATA_OBJECT_META_VALUE = RodsGenQueryEnum.COL_META_DATA_ATTR_VALUE,

      // Name of the collection (directory) in iRODS
      COLLECTION_NAME = RodsGenQueryEnum.COL_COLL_NAME,

      // Last modified time for a collection
      COLLECTION_LAST_MODIFIED = RodsGenQueryEnum.COL_COLL_MODIFY_TIME,

      // iCAT metadata key/value key for a collection
      COLLECTION_META_KEY = RodsGenQueryEnum.COL_META_COLL_ATTR_NAME,

      // iCAT metadata key/value value for a collection
      COLLECTION_META_VALUE = RodsGenQueryEnum.COL_META_COLL_ATTR_VALUE,

      // iRODS parent collection
      PARENT_COLLECTION = RodsGenQueryEnum.COL_COLL_PARENT_NAME;
  }
}
