import isMatch from 'lodash/lang/isMatch';
import {
  APP_ERROR,
  ANSWER_ADDED,
  ANSWER_MOVE_COLUMN,
  ANSWER_CHANGE_ATTRIBUTES,
  ANSWER_UPDATE_FILTER,
  ANSWER_LOADING
} from '../constants/actionTypes';
import { restAction } from '../filters/restFilter';


/**
 * Retrieve's an Answer resource from the WDK REST Service and dispatches an
 * action with the resource. This uses the restAction helper function
 * (see ../filters/restFilter).
 *
 * Request data format, POSTed to service:
 *
 *     {
 *       "questionDefinition": {
 *         "questionName": String,
 *         "params": [ {
 *           "name": String, “value”: Any
 *         } ],
 *         "filters": [ {
 *           “name": String, value: Any
 *         } ],
 *         "viewFilters": [ {
 *           “name": String, value: Any
 *         } ]
 *       },
 *       displayInfo: {
 *         pagination: { offset: Number, numRecords: Number },
 *         attributes: [ attributeName: String ],
 *         sorting: [ { attributeName: String, direction: Enum[ASC,DESC] } ]
 *       }
 *     }
 *
 * @param {string} questionName Fully qualified WDK Question name.
 * @param {Object} opts Addition data to include in request.
 * @param {Array<Object>} opts.params Array of param spec objects: { name: string; value: any }
 * @param {Array<Object>} opts.filters Array of filter spec objects: { name: string; value: any }
 * @param {Array<Object>} opts.viewFilters Array of view filter  spec objects: { name: string; value: any }
 * @param {Object} opts.displayInfo.pagination Pagination specification.
 * @param {number} opts.displayInfo.pagination.offset 0-based index for first record.
 * @param {number} opts.displayInfo.pagination.numRecord The number of records to include.
 * @param {Array<string>} opts.displayInfo.attributes Array of attribute names to include.
 * @param {Array<Object>} opts.displayInfo.sorting Array of sorting spec objects: { attributeName: string; direction: "ASC" | "DESC" }
 */
export function loadAnswer(questionName, opts = {}) {
  let { params = [], filters = [], displayInfo } = opts;

  // FIXME Set attributes to whatever we're soring on. This is required by
  // the service, but it doesn't appear to have any effect at this time. We
  // should be passing the attribute in based on info from the RecordClass.
  displayInfo.attributes = displayInfo.sorting.map(s => s.attributeName);
  displayInfo.tables = [];

  // Build XHR request data for '/answer'
  let questionDefinition = { questionName, params, filters };

  return restAction({
    method: 'POST',
    resource: '/answer',
    data: { questionDefinition, displayInfo },
    types: [ ANSWER_LOADING, APP_ERROR, ANSWER_ADDED ],
    shouldFetch(state) {
      return !isMatch(state.views.answer.displayInfo, displayInfo);
    }
  });
}

/**
 * Change the position of a column in the answer table.
 *
 * @param {string} columnName The name of the attribute to move.
 * @param {number} newPosition The new 0-based index position of the attribute.
 */
export function moveColumn(columnName, newPosition) {
  return {
    type: ANSWER_MOVE_COLUMN,
    columnName: columnName,
    newPosition: newPosition
  };
}

/**
 * Update the set of visible attributes in the answer table.
 *
 * @param {Array<Object>} attributes The new set of attributes to show in the table.
 */
export function changeAttributes(attributes) {
  return {
    type: ANSWER_CHANGE_ATTRIBUTES,
    attributes: attributes
  };
}

/**
 * Set the filter for the answer table.
 *
 * FIXME Remove questionName param, and use a service object to filter the answer.
 *
 * @param {Object} spec The filter specification.
 * @param {string} spec.questionName The question name associated with the answer.
 * @param {string} spec.terms The string to parse and filter.
 * @param {Array<string>} spec.attributes The set of attribute names whose values should be queried.
 * @param {Array<string>} spec.tables The set of table names whose values should be queried.
 */
export function updateFilter({ questionName, terms, attributes, tables }) {
  return {
    type: ANSWER_UPDATE_FILTER,
    questionName,
    terms,
    attributes,
    tables
  };
}
