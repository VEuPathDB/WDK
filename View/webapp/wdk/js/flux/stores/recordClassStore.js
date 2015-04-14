import createStore from '../utils/createStore';
import {
  RECORD_CLASS_LOADING,
  RECORD_CLASS_LOAD_SUCCESS,
  RECORD_CLASSES_LOAD_SUCCESS,
  RECORD_CLASS_LOAD_ERROR
} from '../ActionType';


export default createStore ({

  init() {
    this.state = {
      recordClasses: []
    };
  },

  dispatchHandler(action, emitChange) {
    switch(action.type) {

      case RECORD_CLASS_LOADING:
        this.handleLoading(action, emitChange);
        break;

      case RECORD_CLASS_LOAD_SUCCESS:
        this.handleLoadSuccess(action, emitChange);
        break;

      case RECORD_CLASSES_LOAD_SUCCESS:
        this.handleListLoadSuccess(action, emitChange);
        break;

      case RECORD_CLASS_LOAD_ERROR:
        this.handleLoadError(action, emitChange);
        break;

    }
  },

  handleLoading(action, emitChange) {
    this.state.isLoading = true;
    this.state.error = null;
    emitChange();
  },

  handleLoadSuccess(action, emitChange) {
    this.state.recordClasses.push(action.recordClass);
    emitChange();
  },

  handleListLoadSuccess(action, emitChange) {
    this.state.recordClasses = action.recordClasses;
    emitChange();
  },

  handleLoadError(action, emitChange) {
    this.state.isLoading = false;
    this.state.error = action.error;
    emitChange();
  },

  getState() {
    return this.state;
  }
});
