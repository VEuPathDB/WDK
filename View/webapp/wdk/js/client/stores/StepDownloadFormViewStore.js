import {ReduceStore} from 'flux/utils';
import {filterRecords} from '../utils/recordUtils';
import StepDownloadFormViewActionCreator from '../actioncreators/StepDownloadFormViewActionCreator';

let {
  STEP_DOWNLOAD_LOADING,
  STEP_DOWNLOAD_INITIALIZE_STORE,
  STEP_DOWNLOAD_SELECT_REPORTER,
  STEP_DOWNLOAD_FORM_UPDATE,
  APP_ERROR
} = StepDownloadFormViewActionCreator.actionTypes;

export default class StepDownloadFormViewStore extends ReduceStore {

  // defines the structure of this store's data
  getInitialState() {
    return {

      // 'static' data that should not change for the life of the page
      step: null,
      question: null,
      recordClass: null,

      // 'dynamic' data that is updated with user actions
      isLoading: false,
      selectedReporter: null,
      formState: null,

    };
  }

  reduce(state, { type, payload }) {
    switch(type) {

      case STEP_DOWNLOAD_LOADING:
        return formLoading(state, { isLoading: true });

      case STEP_DOWNLOAD_INITIALIZE_STORE:
        return initialize(state, payload);

      case STEP_DOWNLOAD_SELECT_REPORTER:
        return updateReporter(state, payload);

      case STEP_DOWNLOAD_FORM_UPDATE:
        return updateFormState(state, payload);

      case APP_ERROR:
        return formLoading(state, { isLoading: false });

      default:
        return state;
    }
  }
}

function formLoading(state, payload) {
  return Object.assign({}, state, {
    isLoading: payload.isLoading
  });
}

function initialize(state, payload) {
  return Object.assign({}, state, {
    step: payload.step,
    question: payload.question,
    recordClass: payload.recordClass,
    isLoading: false
  });
}

function updateReporter(state, payload) {
  return Object.assign({}, state, {
    selectedReporter : payload.selectedReporter
  });
}

function updateFormState(state, payload) {
  return Object.assign({}, state, {
    formState : payload.formState
  });
}
