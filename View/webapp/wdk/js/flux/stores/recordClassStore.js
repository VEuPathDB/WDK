import Immutable from 'immutable';
import createStore from '../utils/createStore';
import * as ActionType from '../ActionType';


export default createStore ({

  state: Immutable.fromJS({
    recordClasses: [],
    isLoading: false,
    error: null
  }),

  dispatchHandler(action, emitChange) {
    switch(action.type) {

      case ActionType.RECORD_CLASS_LOADING:
        this.handleLoading(action, emitChange);
        break;

      case ActionType.RECORD_CLASS_LOAD_SUCCESS:
        this.handleLoadSuccess(action, emitChange);
        break;

      case ActionType.RECORD_CLASSES_LOAD_SUCCESS:
        this.handleListLoadSuccess(action, emitChange);
        break;

      case ActionType.RECORD_CLASS_LOAD_ERROR:
        this.handleLoadError(action, emitChange);
        break;

    }
  },

  handleLoading(action, emitChange) {
    this.state = this.state.merge({
      isLoading: true,
      error: null
    });
    emitChange();
  },

  handleLoadSuccess(action, emitChange) {
    this.state = this.state.mergeIn(['recordClasses'], [action.recordClass]);
    emitChange();
  },

  handleListLoadSuccess(action, emitChange) {
    this.state = this.state.merge({
      recordClasses: action.recordClasses
    });
    emitChange();
  },

  handleLoadError(action, emitChange) {
    this.state = this.state.merge({
      isLoading: false,
      error: action.error
    });
    emitChange();
  },

  getState() {
    return this.state;
  }
});
