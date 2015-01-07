var ActionType = require('../ActionType');
var Dispatcher = require('../Dispatcher');
var ServiceAPI = require('../ServiceAPI');


/* Type defs */

type record = any;
type answer = {
  total: number;
  records: Array<record>;
};
type recordsOpts = {
  filters:      ?any;
  columns:      ?Array<string>;
  offset:       ?number;
  numRecords:   ?number;
  sortBy:       ?string;
  reverseSort:  ?boolean;
};


/* helpers */

function dispatchLoaded(answer: answer) {
  Dispatcher.dispatch({ type: ActionType.Answer.LOADED, answer });
}

function dispatchLoading() {
  Dispatcher.dispatch({ type: ActionType.Answer.LOADING });
}


/* actions */

function loadAnswer(questionName: string, params: ?any, opts: ?recordsOpts) {
  dispatchLoading();
  var data = _.assign({ questionName, params }, opts);
  ServiceAPI.post('/answer', data).then(dispatchLoaded);
}


module.exports = {
  loadAnswer
};
