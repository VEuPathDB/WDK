import { submitAsForm } from '../utils/FormSubmitter';
import { getStepBundlePromise, getSingleRecordStepBundlePromise } from '../utils/stepUtils';

export let actionTypes = {
  DOWNLOAD_FORM_LOADING: 'downloadForm/loading',
  DOWNLOAD_FORM_INITIALIZE_STORE: 'downloadForm/initialize',
  DOWNLOAD_FORM_SELECT_REPORTER: 'downloadForm/selectReporter',
  DOWNLOAD_FORM_UPDATE: 'downloadForm/formUpdate',
  DOWNLOAD_FORM_UI_UPDATE: 'downloadForm/formUiUpdate',
  APP_ERROR: 'downloadForm/error'
};

export function selectReporter(reporterName) {
  return {
    type: actionTypes.DOWNLOAD_FORM_SELECT_REPORTER,
    payload: { selectedReporter: reporterName }
  };
}

export function updateFormState(newState) {
  return {
    type: actionTypes.DOWNLOAD_FORM_UPDATE,
    payload: { formState: newState }
  };
}

export function updateFormUiState(newUiState) {
  return {
    type: actionTypes.DOWNLOAD_FORM_UI_UPDATE,
    payload: { formUiState: newUiState }
  };
}

export function loadPageDataFromStepId(stepId) {
  return function run(dispatch, { wdkService }) {
    dispatch({ type: actionTypes.DOWNLOAD_FORM_LOADING });
    return getStepBundlePromise(stepId, wdkService).then(stepBundle => {
      return dispatch({
        type: actionTypes.DOWNLOAD_FORM_INITIALIZE_STORE,
        payload: Object.assign(stepBundle, { scope: 'results' })
      });
    }, error => {
      console.error(error);
      dispatch({
        type: actionTypes.APP_ERROR,
        payload: { error }
      });
    });
  }
}

export function loadPageDataFromRecord(recordClassUrlSegment, primaryKeyString) {
  return function run(dispatch, { wdkService }) {
    dispatch({ type: actionTypes.DOWNLOAD_FORM_LOADING });
    wdkService.findRecordClass(r => r.urlSegment === recordClassUrlSegment).then(recordClass =>
      getSingleRecordStepBundlePromise(recordClass, primaryKeyString)).then(stepBundle => {
        return dispatch({
          type: actionTypes.DOWNLOAD_FORM_INITIALIZE_STORE,
          payload: Object.assign(stepBundle, { scope: 'record' })
        });
      },
      error => {
        console.error(error);
        dispatch({
          type: actionTypes.APP_ERROR,
          payload: { error }
        });
      });
  }
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
