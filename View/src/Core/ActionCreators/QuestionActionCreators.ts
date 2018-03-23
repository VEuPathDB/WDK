import { Observable } from 'rxjs/Rx';

import { Action, combineEpics, EpicServices, makeActionCreator, payload } from 'Utils/ActionCreatorUtils';
import { Parameter, ParameterValue, ParameterValues, QuestionWithParameters, RecordClass } from 'Utils/WdkModel';
import WdkService, { ServiceError } from 'Utils/WdkService';
import QuestionStore from 'Views/Question/QuestionStore';

type BasePayload = {
  questionName: string;
}

export const ActiveQuestionUpdatedAction = makeActionCreator(
  'quesiton/active-question-updated',
  payload<BasePayload & {
    paramValues?: ParameterValues;
    stepId: number | undefined;
  }>()
);

export const QuestionLoadedAction = makeActionCreator(
  'question/question-loaded',
  payload<BasePayload & {
    question: QuestionWithParameters;
    recordClass: RecordClass;
    paramValues: ParameterValues;
  }>()
)

export const UnloadQuestionAction = makeActionCreator('question/unload-question', payload<BasePayload>());

export const QuestionErrorAction = makeActionCreator('question/question-error', payload<BasePayload>());

export const QuestionNotFoundAction = makeActionCreator('question/question-not-found', payload<BasePayload>());

export const ParamValueUpdatedAction = makeActionCreator(
  'question/param-value-update',
  payload<BasePayload & {
    parameter: Parameter,
    dependentParameters: Parameter[],
    paramValues: ParameterValues,
    paramValue: ParameterValue
  }>()
);

export const ParamErrorAction = makeActionCreator(
  'question/param-error',
  payload<BasePayload & {
    error: string,
    paramName: string
  }>()
);

export const ParamsUpdatedAction = makeActionCreator(
  'question/params-updated',
  payload<BasePayload & {
    parameters: Parameter[]
  }>()
);

export const ParamInitAction = makeActionCreator(
  'question/param-init',
  payload<BasePayload & {
    parameter: Parameter;
    paramValues: ParameterValues
  }>()
);

export const ParamStateUpdatedAction = makeActionCreator(
  'question/param-state-updated',
  payload<BasePayload & {
    paramName: string;
    paramState: any
  }>()
);

export const GroupVisibilityChangedAction = makeActionCreator(
  'question/group-visibility-change',
  payload<BasePayload & {
    groupName: string;
    isVisible: boolean;
  }>()
)

export const GroupStateUpdatedAction = makeActionCreator(
  'question/group-state-update',
  payload<BasePayload & {
    groupName: string;
    groupState: any;
  }>()
)


// Epics
// -----

export const questionEpic = combineEpics(loadQuestionEpic, updateDependentParamsEpic);

function loadQuestionEpic(action$: Observable<Action>, { wdkService, store }: EpicServices<QuestionStore>): Observable<Action> {
  return action$
    .filter(ActiveQuestionUpdatedAction.isType)
    .mergeMap(action =>
      Observable.from(loadQuestion(wdkService, action.payload.questionName, action.payload.paramValues))
      .takeUntil(action$.filter(killAction => (
        UnloadQuestionAction.isType(killAction) &&
        killAction.payload.questionName === action.payload.questionName
      )))
    )
}

function updateDependentParamsEpic(action$: Observable<Action>, {wdkService}: EpicServices): Observable<Action> {
  return action$
    .filter(ParamValueUpdatedAction.isType)
    .filter(action => action.payload.parameter.dependentParams.length > 0)
    .debounceTime(1000)
    .mergeMap(action => {
      const { questionName, parameter, paramValues, paramValue } = action.payload;
      return Observable.from(wdkService.getQuestionParamValues(
        questionName,
        parameter.name,
        paramValue,
        paramValues
      ).then(
        parameters => ParamsUpdatedAction.create({questionName, parameters}),
        error => ParamErrorAction.create({ questionName, error: error.message, paramName: parameter.name })
      ))
      .takeUntil(action$.filter(ParamValueUpdatedAction.isType))
      .takeUntil(action$.filter(killAction => (
        UnloadQuestionAction.isType(killAction) &&
        killAction.payload.questionName === action.payload.questionName
      )))
    });
}


// Helpers
// -------

function loadQuestion(wdkService: WdkService, questionName: string, paramValues?: ParameterValues) {
  const question$ = paramValues == null
    ? wdkService.getQuestionAndParameters(questionName)
    : wdkService.getQuestionGivenParameters(questionName, paramValues);

  const recordClass$ = question$.then(question =>
    wdkService.findRecordClass(rc => rc.name == question.recordClassName));

  return Promise.all([question$, recordClass$]).then(
    ([question, recordClass]) => {
      if (paramValues == null) {
        paramValues = makeDefaultParamValues(question.parameters);
      }
      return QuestionLoadedAction.create({ questionName, question, recordClass, paramValues })
    },
    error => error.status === 404
      ? QuestionNotFoundAction.create({ questionName })
      : QuestionErrorAction.create({ questionName })
  );
}

function makeDefaultParamValues(parameters: Parameter[]) {
  return parameters.reduce(function(values, { name, defaultValue = ''}) {
    return Object.assign(values, { [name]: defaultValue });
  }, {} as ParameterValues);
}
