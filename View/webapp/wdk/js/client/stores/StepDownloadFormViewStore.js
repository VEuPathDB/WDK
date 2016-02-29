import {ReduceStore} from 'flux/utils';
import {filterRecords} from '../utils/recordUtils';
import { actionTypes } from '../actioncreators/StepDownloadFormViewActionCreator';

export default class StepDownloadFormViewStore extends ReduceStore {

  // defines the structure of this store's data
  getInitialState() {
    return {

      // 'static' data that should not change for the life of the page
      step: null,
      question: null,
      recordClass: null,
      ontology: null,

      // 'dynamic' data that is updated with user actions
      isLoading: false,
      selectedReporter: null,
      formState: null,
      formUiState: null

    };
  }

  reduce(state, { type, payload }) {
    switch(type) {

      case actionTypes.STEP_DOWNLOAD_LOADING:
        return formLoading(state, { isLoading: true });

      case actionTypes.STEP_DOWNLOAD_INITIALIZE_STORE:
        return initialize(state, payload);

      case actionTypes.STEP_DOWNLOAD_RESET_STORE:
        return initialize(state, this.getInitialState());

      case actionTypes.STEP_DOWNLOAD_SELECT_REPORTER:
        return updateReporter(state, payload);

      case actionTypes.STEP_DOWNLOAD_FORM_UPDATE:
        return updateFormState(state, payload);

      case actionTypes.STEP_DOWNLOAD_FORM_UI_UPDATE:
        return updateFormUiState(state, payload);

      case actionTypes.APP_ERROR:
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
    ontology: payload.ontology,
    isLoading: false
  });
}

function updateReporter(state, payload) {
  return Object.assign({}, state, {
    selectedReporter: payload.selectedReporter,
    formState: null,
    formUiState: null
  });
}

function updateFormState(state, payload) {
  return Object.assign({}, state, {
    formState : payload.formState
  });
}

function updateFormUiState(state, payload) {
  return Object.assign({}, state, {
    formUiState : payload.formUiState
  });
}
