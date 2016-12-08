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

    // create promise for recordClass
    let recordClassPromise = wdkService.findRecordClass(r => r.urlSegment === recordClassUrlSegment);

    // create promise for record, dependent on result of recordClass promise
    let recordPromise = recordClassPromise.then(recordClass => {
      let pkValues = primaryKeyString.split(',');
      let pkArray = recordClass.primaryKeyColumnRefs.map((ref, index) => ({ name: ref, value: pkValues[index] }));
      return wdkService.getRecord(recordClass.name, pkArray, { attributes: [recordClass.recordIdAttributeName ] })
    });

    // create promise for bundle, dependent on previous two promises and primaryKeyString
    let bundlePromise = Promise
      .all([ recordClassPromise, recordPromise, primaryKeyString ])
      .then(getSingleRecordStepBundlePromise);

    // dispatch appropriate actions
    return bundlePromise.then(
      stepBundle => {
        return dispatch({
          type: actionTypes.DOWNLOAD_FORM_INITIALIZE_STORE,
          payload: Object.assign(stepBundle, { scope: 'record' })
        });
      },
      error => {
        console.error(error);
        return dispatch({
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
      answerSpec: step.answerSpec,
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
