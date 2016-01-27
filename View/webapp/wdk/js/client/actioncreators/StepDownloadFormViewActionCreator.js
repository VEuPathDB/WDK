import ActionCreator from '../utils/ActionCreator';
import { submitAsForm } from '../utils/FormSubmitter';

// Action types
let actionTypes = {
  STEP_DOWNLOAD_LOADING: 'stepDownload/loading',
  STEP_DOWNLOAD_INITIALIZE_STORE: 'stepDownload/initialize',
  STEP_DOWNLOAD_SELECT_REPORTER: 'stepDownload/selectReporter',
  STEP_DOWNLOAD_FORM_UPDATE: 'stepDownload/formUpdate',
  STEP_DOWNLOAD_FORM_UI_UPDATE: 'stepDownload/formUiUpdate',
  APP_ERROR: 'stepDownload/error'
};

export default class StepDownloadFormViewActionCreator extends ActionCreator {

  selectReporter(reporterName) {
    this._dispatch({
      type: actionTypes.STEP_DOWNLOAD_SELECT_REPORTER,
      payload: { selectedReporter: reporterName }
    })
  }

  updateFormState(newState) {
    this._dispatch({
      type: actionTypes.STEP_DOWNLOAD_FORM_UPDATE,
      payload: { formState: newState }
    });
  }

  updateFormUiState(newUiState) {
    this._dispatch({
      type: actionTypes.STEP_DOWNLOAD_FORM_UI_UPDATE,
      payload: { formUiState: newUiState }
    });
  }

  reloadData(stepId) {

    this._dispatch({ type: actionTypes.STEP_DOWNLOAD_LOADING });

    let stepPromise = this._service.findStep(stepId);
    let questionPromise = stepPromise.then(step => {
      return this._service.findQuestion( q => q.name === step.answerSpec.questionName );
    });
    let recordClassPromise = questionPromise.then(question => {
      return this._service.findRecordClass( rc => rc.name === question.recordClass );
    });

    Promise.all([ stepPromise, questionPromise, recordClassPromise ])
    .then(([ step, question, recordClass]) => {
      this._dispatch({
        type: actionTypes.STEP_DOWNLOAD_INITIALIZE_STORE,
        payload: {
          step,
          question,
          recordClass
        }
      })
    }, error => {
      this._dispatch({
        type: APP_ERROR,
        payload: { error }
      });
      throw error;
    })
    .catch(error => console.assert(error));
  }

  submitForm(step, selectedReporter, formState, target = '_blank') {
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
      action: this._service.getAnswerServiceUrl(),
      target: target,
      inputs: {
        data: JSON.stringify(submissionJson)
      }
    });
  }
}

StepDownloadFormViewActionCreator.actionTypes = actionTypes;
