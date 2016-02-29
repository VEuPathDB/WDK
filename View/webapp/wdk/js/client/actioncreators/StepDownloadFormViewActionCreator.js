import { submitAsForm } from '../utils/FormSubmitter';
import { getStepBundle, getSingleRecordStepBundle } from '../utils/actionCreatorUtils';

// Action types
export let actionTypes = {
  STEP_DOWNLOAD_LOADING: 'stepDownload/loading',
  STEP_DOWNLOAD_INITIALIZE_STORE: 'stepDownload/initialize',
  STEP_DOWNLOAD_RESET_STORE: 'stepDownload/reset',
  STEP_DOWNLOAD_SELECT_REPORTER: 'stepDownload/selectReporter',
  STEP_DOWNLOAD_FORM_UPDATE: 'stepDownload/formUpdate',
  STEP_DOWNLOAD_FORM_UI_UPDATE: 'stepDownload/formUiUpdate',
  APP_ERROR: 'stepDownload/error'
};

export function selectReporter(reporterName) {
  return {
    type: actionTypes.STEP_DOWNLOAD_SELECT_REPORTER,
    payload: { selectedReporter: reporterName }
  };
}

export function updateFormState(newState) {
  return {
    type: actionTypes.STEP_DOWNLOAD_FORM_UPDATE,
    payload: { formState: newState }
  };
}

export function updateFormUiState(newUiState) {
  return {
    type: actionTypes.STEP_DOWNLOAD_FORM_UI_UPDATE,
    payload: { formUiState: newUiState }
  };
}

export function loadPageData(stepId) {
  return function run(dispatch, { wdkService }) {
    dispatch({ type: actionTypes.STEP_DOWNLOAD_LOADING });
    return getStepBundle(stepId, wdkService).then(stepBundle => {
      return dispatch({
        type: actionTypes.STEP_DOWNLOAD_INITIALIZE_STORE,
        payload: stepBundle
      });
    }, error => {
      dispatch({
        type: actionTypes.APP_ERROR,
        payload: { error }
      });
      throw error;
    });
  }
}

export function loadPageDataFromRecord(recordClassUrlSegment, primaryKeyString) {
  return function run(dispatch, { wdkService }) {
    dispatch({ type: actionTypes.STEP_DOWNLOAD_LOADING });
    return wdkService.findRecordClass(r => r.urlSegment === recordClassUrlSegment).then( recordClass => {
      return dispatch({
        type: actionTypes.STEP_DOWNLOAD_INITIALIZE_STORE,
        payload: getSingleRecordStepBundle(recordClass, primaryKeyString)
      });
    }, error => {
      dispatch({
        type: actionTypes.APP_ERROR,
        payload: { error }
      });
      throw error;
    });
  }
}

export function unloadPageData() {
  return {
    type: actionTypes.STEP_DOWNLOAD_RESET_STORE,
    payload: null
  };
}

export function submitForm(step, selectedReporter, formState, target = '_blank') {
  return function run(dispatch, { wdkService }) {
    // a submission must trigger a form download, meaning we must POST the form
    let submissionJson = {
      questionDefinition: step.answerSpec,
      formatting: {}
    };
    if (selectedReporter != null) {
      submissionJson.formatting.format = selectedReporter;
    }
    submissionJson.formatting.formatConfig = (formState == null ?
        { contentDisposition: 'attachment' } : formState);
    submitAsForm({
      method: 'post',
      action: wdkService.getAnswerServiceUrl(),
      target: target,
      inputs: {
        data: JSON.stringify(submissionJson)
      }
    });
  }
}
