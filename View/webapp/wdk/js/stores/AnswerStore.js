var Immutable = require('immutable');
var Store = require('./Store');


/**
 * Contains the list of records being viewed.
 *
 * Records can be sorted, paged, etc.
 *
 * TODO Store multiple record lists...
 */
class AnswerStore extends Store {

  constructor() {
    super();
    this._records = Immutable.fromJS([]);
  }

  getRecords() {
    return this._records;
  }

  handleDispatch(action) {
    switch(action.type) {

      case ActionTypes.Answer.LOADING:
        this._loading = true;
        this.emitChange();
        break;

      case ActionTypes.Answer.LOADED:
        this._loading = false;
        this._records = this._records.mergeDeep(action.records);
        this.emitChange();
        break;

    }
  }

}


module.exports = new AnswerStore();
