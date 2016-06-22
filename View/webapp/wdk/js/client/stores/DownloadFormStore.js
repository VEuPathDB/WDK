import WdkStore from './WdkStore';
import { StaticDataProps } from '../utils/StaticDataUtils';
import { actionTypes } from '../actioncreators/DownloadFormActionCreator';
import WdkServiceJsonReporterForm from '../components/WdkServiceJsonReporterForm';

export default class DownloadFormStore extends WdkStore {

  getRequiredStaticDataProps() {
    return [ StaticDataProps.PREFERENCES, StaticDataProps.ONTOLOGY ];
  }

  // defines the structure of this store's data
  getInitialState() {
    return {

      // static data loaded automatically
      preferences: null,
      ontology: null,

      // 'static' data that should not change for the life of the page
      step: null,
      question: null,
      recordClass: null,
      scope: null,
      availableReporters: [],

      // 'dynamic' data that is updated with user actions
      isLoading: false,
      selectedReporter: null,
      formState: null,
      formUiState: null
    };
  }

  handleStaticDataItemAction(state, itemName, payload) {
    let newState = super.handleStaticDataItemAction(state, itemName, payload);
    // FIXME: calling this for all static data actions means form state may reset
    //    if user changes preferences in one of the forms.  We don't have any
    //    forms like this now but may in the future and others may already.
    return tryFormInit(this, newState);
  }

  handleAction(state, { type, payload }) {

    switch(type) {

      case actionTypes.DOWNLOAD_FORM_LOADING:
        return setFormLoading(state, true);

      case actionTypes.DOWNLOAD_FORM_INITIALIZE_STORE:
        return initialize(this, state, payload);

      case actionTypes.DOWNLOAD_FORM_SELECT_REPORTER:
        return updateReporter(this, state, payload.selectedReporter);

      case actionTypes.DOWNLOAD_FORM_UPDATE:
        return updateFormState(state, payload.formState);

      case actionTypes.DOWNLOAD_FORM_UI_UPDATE:
        return updateFormUiState(state, payload.formUiState);

      case actionTypes.APP_ERROR:
        return setFormLoading(state, false);

      default:
        return state;
    }
  }

  // subclasses should override to enable reporters configured in WDK
  getSelectedReporter(selectedReporterName, recordClassName) {
    return WdkServiceJsonReporterForm;
  }
}

DownloadFormStore.actionTypes = actionTypes;

function setFormLoading(state, isLoading) {
  return Object.assign({}, state, { isLoading });
}

function initialize(thisStore, state, { step, question, recordClass, scope }) {

  // only use reporters configured for the report download page
  let availableReporters = recordClass.formats.filter(reporter => reporter.scopes.indexOf(scope) > -1);

  // set portion of static page state not loaded automatically
  let partialState = Object.assign({}, state, { step, question, recordClass, scope, availableReporters });

  return tryFormInit(thisStore, partialState);
}

function tryFormInit(thisStore, state) {
  // try to calculate form state for WDK JSON reporter
  if (thisStore.isAllRequiredStaticDataLoaded(state) && state.step != null) {
    // step, preferences, and ontology have been loaded;
    //    calculate state and set isLoading to false
    let selectedReporterName = (state.availableReporters.length == 1 ?
        state.availableReporters[0].name : null);
    return Object.assign({}, state, {
      isLoading: false,
      selectedReporter: selectedReporterName
    },
    thisStore.getSelectedReporter(selectedReporterName, state.recordClass.name).getInitialState(state));
  }

  // one of the initialize actions has not yet been sent
  return state;
}

function updateReporter(thisStore, state, selectedReporter) {
  return Object.assign({}, state, { selectedReporter },
      thisStore.getSelectedReporter(selectedReporter, state.recordClass.name).getInitialState(state));
}

function updateFormState(state, formState) {
  return Object.assign({}, state, { formState });
}

function updateFormUiState(state, formUiState) {
  return Object.assign({}, state, { formUiState });
}
