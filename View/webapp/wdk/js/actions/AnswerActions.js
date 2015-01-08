"use strict";

var ActionType = require('../ActionType');
var Dispatcher = require('../Dispatcher');
var ServiceAPI = require('../ServiceAPI');


/* helpers */

function dispatchLoadSuccess(answer) {
  Dispatcher.dispatch({ type: ActionType.Answer.LOAD_SUCCESS, answer });
}

function dispatchLoadError(error) {
  Dispatcher.dispatch({ type: ActionType.Answer.LOAD_ERROR, error });
}

function dispatchLoading() {
  Dispatcher.dispatch({ type: ActionType.Answer.LOADING });
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
function loadAnswer(questionName, opts) {
  var { params, filters, displayInfo } = opts;
  var questionDefinition = { questionName, params, filters };
  var data = { questionDefinition, displayInfo };
  dispatchLoading();
  ServiceAPI.post('/answer', data).then(dispatchLoadSuccess, dispatchLoadError);
}


module.exports = {
  loadAnswer
};
