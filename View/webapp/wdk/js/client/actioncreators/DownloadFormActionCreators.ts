import { submitAsForm } from '../utils/FormSubmitter';
import { getStepBundlePromise, getSingleRecordStepBundlePromise } from '../utils/stepUtils';
import { ActionCreator } from '../ActionCreator';
import { Step } from '../utils/WdkUser';
import {Question, RecordClass} from "../utils/WdkModel";

export type LoadingAction = {
  type: 'downloadForm/loading'
}

export type InitializeAction = {
  type: 'downloadForm/initialize',
  payload: {
    step: Step,
    question: Question,
    recordClass: RecordClass,
    scope: string
  }
}

export type SelectReporterAction = {
  type: 'downloadForm/selectReporter',
  payload: {
    selectedReporter: string
  }
}

export type UpdateAction = {
  type: 'downloadForm/formUpdate',
  payload: {
    formState: any
  }
}

export type UiUpdateAction = {
  type: 'downloadForm/formUiUpdate',
  payload: {
    formUiState: any
  }
}

export type ErrorAction = {
  type: 'downloadForm/error',
  payload: {
    error: Error
  }
}


export function selectReporter(reporterName: string): SelectReporterAction {
  return {
    type: 'downloadForm/selectReporter',
    payload: { selectedReporter: reporterName }
  };
}

export function updateFormState(newState: any): UpdateAction {
  return {
    type: 'downloadForm/formUpdate',
    payload: { formState: newState }
  };
}

export function updateFormUiState(newUiState: any): UiUpdateAction {
  return {
    type: 'downloadForm/formUiUpdate',
    payload: { formUiState: newUiState }
  };
}

export const loadPageDataFromStepId: ActionCreator<LoadingAction | ErrorAction | InitializeAction> = (stepId: number) => {
  return function run(dispatch, { wdkService }) {
    dispatch({ type: 'downloadForm/loading' });
    return getStepBundlePromise(stepId, wdkService).then(
      stepBundle => {
        dispatch({
          type: 'downloadForm/initialize',
          payload: Object.assign(stepBundle, {scope: 'results'})
        })
      },
      (error: Error) => {
        dispatch({
          type: 'downloadForm/error',
          payload: {error}
        })
      }
    );
  }
}

export const loadPageDataFromRecord: ActionCreator<LoadingAction | ErrorAction | InitializeAction> = (recordClassUrlSegment: string, primaryKeyString: string) => {
  return function run(dispatch, { wdkService }) {
    dispatch({ type: 'downloadForm/loading' });

    // create promise for recordClass
    let recordClassPromise = wdkService.findRecordClass(r => r.urlSegment === recordClassUrlSegment);

    // create promise for record, dependent on result of recordClass promise
    let recordPromise = recordClassPromise.then(recordClass => {
      if (recordClass == null)
        throw new Error("Could not find record class identified by `" + recordClassUrlSegment + "`.");

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
        dispatch({
          type: 'downloadForm/initialize',
          payload: Object.assign(stepBundle, {scope: 'record'})
        })
      },
      error => {
        dispatch({
          type: 'downloadForm/error',
          payload: { error }
        })
      }
    );
  }
}

// FIXME figure out what to do about "ActionCreators" that don't dispatch actions
// In this case, we just want access to wdkService.
export const submitForm: ActionCreator<{ type: '__' }> = (step: Step, selectedReporter: string, formState: any, target = '_blank') => {
  return function run(dispatch, { wdkService }) {
    // a submission must trigger a form download, meaning we must POST the form
    let submissionJson = {
      answerSpec: step.answerSpec,
      formatting: {
        format: selectedReporter ? selectedReporter : 'wdk-service-json',
        formatConfig: formState == null ?
          { contentDisposition: 'attachment' } : formState
      }
    };
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
