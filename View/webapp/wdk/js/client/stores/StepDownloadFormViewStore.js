import WdkStore from './WdkStore';
import { actionTypes } from '../actioncreators/StepDownloadFormViewActionCreator';
import { actionTypes as userActionTypes } from '../actioncreators/UserActionCreator';
import WdkServiceJsonReporterForm from '../components/WdkServiceJsonReporterForm';

export default class StepDownloadFormViewStore extends WdkStore {

  // defines the structure of this store's data
  getInitialState() {
    return {

      // 'static' data that should not change for the life of the page
      step: null,
      question: null,
      recordClass: null,
      availableReporters: [],
      ontology: null,

      // 'dynamic' data that is updated with user actions
      isLoading: false,
      selectedReporter: null,
      formState: null,
      formUiState: null

    };
  }

  reduce(state, { type, payload }) {
    let userStore = this._storeContainer.UserStore;
    switch(type) {

      case actionTypes.STEP_DOWNLOAD_LOADING:
        return formLoading(state, { isLoading: true });

      case actionTypes.STEP_DOWNLOAD_INITIALIZE_STORE:
        return initialize(state, payload, userStore);

      case userActionTypes.USER_INITIALIZE_STORE:
        // wait for user store to process this action, then try to complete
        this.getDispatcher().waitFor([userStore.getDispatchToken()]);
        return tryInitCompletion(state, userStore);

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

StepDownloadFormViewStore.actionTypes = actionTypes;

function formLoading(state, payload) {
  return Object.assign({}, state, {
    isLoading: payload.isLoading
  });
}

function initialize(state, payload, userStore) {

  // only use reporters configured for the report download page
  let reporters = payload.recordClass.formats.filter(f => f.isInReport);

  let partialState = Object.assign({}, state, {
    step: payload.step,
    question: payload.question,
    recordClass: payload.recordClass,
    availableReporters: reporters,
    ontology: payload.ontology
  });

  return tryInitCompletion(partialState, userStore);
}

function tryInitCompletion(state, userStore) {

  // otherwise, calculate form state for WDK JSON reporter
  let userStoreState = userStore.getState();
  if (state.step != null && userStoreState.user != null) {
    // both this and the user store have received initialize actions;
    //    calculate state and set isLoading to false
    return Object.assign({}, state, { isLoading: false },
        WdkServiceJsonReporterForm.getInitialState(state, userStoreState));
  }

  // one of the initialize actions has not yet been sent
  return state;
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
