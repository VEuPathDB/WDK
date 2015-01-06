var ActionType = require('../ActionType');
var Dispatcher = require('../Dispatcher');
var ServiceAPI = require('../ServiceAPI');

// TODO Should we wrap records with Immutable here?
type record = any;
function dispatchLoaded(records: Array<record>) {
  Dispatcher.dispatch({ type: ActionType.Answer.LOADED, records });
}

function dispatchLoading() {
  Dispatcher.dispatch({ type: ActionType.Answer.LOADING });
}

// The service will use default values for unspecified opts
type recordsOpts = {
  filters:      ?any;
  columns:      ?Array<string>;
  offset:       ?number;
  numRecords:   ?number;
  sortBy:       ?string;
  reverseSort:  ?boolean;
};
function getRecords(questionName: string, params: ?any, opts: ?recordsOpts) {
  dispatchLoading();
  var data = _.assign({ questionName, params }, opts);
  ServiceAPI.post('/answer', data).then(dispatchLoaded);
}

module.exports = {
  getRecords
};
