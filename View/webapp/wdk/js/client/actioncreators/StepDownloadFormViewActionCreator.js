import ActionCreator from '../utils/ActionCreator';

// Action types
let actionTypes = {
  STEP_DOWNLOAD_LOADING: 'stepDownload/loading',
  STEP_DOWNLOAD_INITIALIZE_STORE: 'stepDownload/initialize',
  STEP_DOWNLOAD_SELECT_REPORTER: 'stepDownload/selectReporter',
  STEP_DOWNLOAD_FORM_UPDATE: 'stepDownload/formUpdate',
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
      payload: { formState, newState }
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
    })
    .catch(error => console.assert(error));
  }

  submitForm(step, selectedReporter, formState) {
    // a submission must trigger a form download, meaning we must POST the form
    let submissionJson = {
      questionDefinition: step.answerSpec,
      formatting: {}
    };
    if (selectedReporter != null) {
      submissionJson.formatting.format = selectedReporter;
    }
    submissionJson.formatting.formatConfig = (formState == null ?
        { contentDisposition: 'attachment' } :
        formState);
    // build the form and submit
    let form = document.createElement("form");
    form.setAttribute("method", "post");
    form.setAttribute("action", this._service.getAnswerServiceUrl());
    let input = document.createElement("input");
    input.setAttribute("name", "data");
    input.setAttribute("value", JSON.stringify(submissionJson));
    form.appendChild(input);
    form.submit();
  }
}

StepDownloadFormViewActionCreator.actionTypes = actionTypes;
