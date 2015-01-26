import _ from 'lodash';
import ActionType from '../ActionType';
import Dispatcher from '../Dispatcher';
import * as ServiceAPI from '../ServiceAPI';


/* helpers */

function dispatchLoadSuccess(answer, requestData) {
  // Dispatcher.dispatch({ type: ActionType.App.LOADING, isLoading: false });
  Dispatcher.dispatch({ type: ActionType.Answer.LOAD_SUCCESS, answer, requestData });
}

function dispatchLoadError(error, requestData) {
  // Dispatcher.dispatch({ type: ActionType.App.ERROR, error });
  Dispatcher.dispatch({ type: ActionType.Answer.LOAD_ERROR, error, requestData });
}

function dispatchLoading(requestData) {
  // Dispatcher.dispatch({ type: ActionType.App.LOADING, isLoading: true });
  Dispatcher.dispatch({ type: ActionType.Answer.LOADING, requestData });
}


/* actions */

/**
 * Retrieve's an Answer resource and dispatches an action with the resource.
 *
 * TODO Validate options?
 * TODO Implement caching?
 *
 *
 * Actions:
 *
 *   - { type: ActionType.Answer.LOADING, answer: answerResource }
 *     Answer resource is being loaded
 *
 *   - { type: ActionType.Answer.LOAD_SUCCESS, answer: answerResource }
 *     Answer resource has been loaded successfully
 *
 *   - { type: ActionType.Answer.LOAD_ERROR, error: reason }
 *     Answer resource could not be loaded
 *
 *
 * Options:
 *
 *   - params: Object of key-value pairs for Question params.
 *   - filters: Object of key-value pairs for Question filters.
 *   - displayInfo: Object with display details (see Request data format below).
 *
 *
 * Usage:
 *
 *    loadAnswer('GeneRecords.GenesByTaxon', { params: { ... }, filters: { ... }, displayInfo: { ... } });
 *
 *
 * Request data format:
 *
 *     {
 *       “questionDefinition”: {
 *         “questionName”: String,
 *         “params”: [ {
 *           “name”: String, “value”: Any
 *         } ],
 *         “filters”: [ {
 *           “name”: String, value: Any
 *         } ]
 *       },
 *       displayInfo: {
 *         pagination: { offset: Number, numRecords: Number },
 *         columns: [ columnName: String ],
 *         sorting: [ { columnName: String, direction: Enum[ASC,DESC] } ]
 *       }
 *     }
 *
 * @param {string} questionName Fully qualified WDK Question name.
 * @param {object} opts Addition data to include in request.
 */
export function loadAnswer(questionName, opts = {}) {
  var { params, filters, displayInfo } = _.defaults(opts, {   // _.defaults is from the lodash utiliies library
    params: [],
    filters: [],
    displayInfo: {
      pagination: { offset: 0, numRecords: 100 },
      columns: null,
      sorting: null
    }
  });
  var questionDefinition = { questionName, params, filters };
  var requestData = { questionDefinition, displayInfo };
  dispatchLoading(requestData);
  ServiceAPI.postResource('/answer', requestData)
    .then(function(answer) {
      dispatchLoadSuccess(answer, requestData);
    }, function(error) {
      dispatchLoadError(error, requestData);
    })
    // catch errors caused by Store callbacks
    .catch(err => console.assert(false, err));
}

export function initialize() {
  Dispatcher.dispatch({ type: ActionType.Answer.INIT });
}

export function moveColumn(columnName, newPosition) {
  console.assert(typeof columnName === "string", `columnName ${columnName} should be a string.`);
  console.assert(typeof newPosition === "number", `newPosition ${newPosition} should be a number.`);
  Dispatcher.dispatch({
    type: ActionType.Answer.MOVE_COLUMN,
    columnName,
    newPosition
  });
}

export function changeAttributes(attributes) {
  console.assert(Array.isArray(attributes), `attributes ${attributes} should be an array.`);
  Dispatcher.dispatch({
    type: ActionType.Answer.CHANGE_ATTRIBUTES,
    attributes
  });
}
