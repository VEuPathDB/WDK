import map from 'lodash/collection/map';
import flattenDeep from 'lodash/array/flattenDeep';
import {
  APP_ERROR,
  RECORD_DETAILS_ADDED,
  RECORD_CATEGORY_COLLAPSED_TOGGLED,
  RECORD_TABLE_COLLAPSED_TOGGLED
} from '../constants/actionTypes';
import CommonActions from './commonActions';
import { restAction } from '../filters/restFilter';

/**
 * Actions related to Records, including fetching from the WDK REST Service and
 * toggling the visibility of attribute categories.
 */

/**
 * @param {string} recordClassName
 * @param {Object} spec
 * @param {Object} spec.primaryKey
 * @param {Array<string>}  spec.attributes
 * @param {Array<string>}  spec.tables
 */
export function fetchRecordDetails(recordClassName, recordSpec) {
  // TODO Only fetch what is needed. This will require being able to read the
  // application state here. Thus, we probably need to add a thunk filter.
  return restAction({
    method: 'POST',
    resource: '/record/' + recordClassName + '/instance',
    data: recordSpec,
    types: [ null, null, RECORD_DETAILS_ADDED ]
  });
}

export function toggleCategoryCollapsed({ recordClass, category, isCollapsed }) {
  return {
    type: RECORD_CATEGORY_COLLAPSED_TOGGLED,
    recordClass: recordClass.fullName,
    name: category.name,
    isCollapsed
  }
}

export function toggleTableCollapsed({ recordClass, tableName, isCollapsed }) {
  return {
    type: RECORD_TABLE_COLLAPSED_TOGGLED,
    recordClass: recordClass.fullName,
    name: tableName,
    isCollapsed
  }
}
