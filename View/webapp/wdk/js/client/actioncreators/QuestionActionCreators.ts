import { Observable } from 'rxjs/Rx';

import { combineEpics, ActionCreatorServices } from '../utils/ActionCreatorUtils';
import { Context } from '../params/Utils';
import { makeActionCreator, payload } from '../utils/ActionCreatorUtils';
import { Parameter, ParameterValue, ParameterValues, Question, RecordClass } from '../utils/WdkModel';
import WdkService from '../utils/WdkService';
import { Action } from '../dispatcher/Dispatcher';

type BasePayload = {
  questionName: string;
}

export const ActiveQuestionUpdatedAction = makeActionCreator(
  'quesiton/active-question-updated',
  payload<BasePayload & {
    paramValues?: ParameterValues
  }>()
);

export const QuestionLoadedAction = makeActionCreator(
  'question/question-loaded',
  payload<BasePayload & {
    question: Question;
    recordClass: RecordClass;
    paramValues: ParameterValues;
  }>()
)

export const QuestionErrorAction = makeActionCreator('question/question-error', payload<BasePayload>());

export const QuestionNotFoundAction = makeActionCreator('question/question-not-found', payload<BasePayload>());

export const ParamLoadedAction = makeActionCreator(
  'param-loaded',
  payload<{
    ctx: Context<Parameter>
  }>()
);

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

export const ParamStateUpdatedAction = makeActionCreator(
  'question/param-state-updated',
  payload<{
    paramName: string;
    paramState: any
  }>()
);


// Epics
// -----

export const questionEpic = combineEpics(loadQuestionEpic, updateDependentParamsEpic);

function loadQuestionEpic(action$: Observable<Action>, { wdkService }: ActionCreatorServices): Observable<Action> {
  return action$
    .filter(ActiveQuestionUpdatedAction.isType)
    .mergeMap(action =>
      Observable.fromPromise(loadQuestion(wdkService, action.payload.questionName, action.payload.paramValues))
        .takeUntil(action$.filter(ActiveQuestionUpdatedAction.isType))
        .mergeMap(action => QuestionLoadedAction.isType(action)
          ? Observable.from([action, ...action.payload.question.parameters.map(parameter =>
            ParamLoadedAction.create({ ctx: { questionName: action.payload.questionName, paramValues: action.payload.paramValues, parameter } }))])
          : Observable.empty()
        )
    )
}

function updateDependentParamsEpic(action$: Observable<Action>, {wdkService}: ActionCreatorServices): Observable<Action> {
  return action$.filter(ParamValueUpdatedAction.isType)
    .debounceTime(1000)
    .mergeMap(action => {
      const { questionName, parameter, paramValues, paramValue } = action.payload;
      const newParamValues = { ...paramValues, [parameter.name]: paramValue };
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
    error => error.status === 404 ? QuestionNotFoundAction.create({ questionName }) : QuestionErrorAction.create({ questionName })
  )
}

function makeDefaultParamValues(parameters: Parameter[]) {
  return parameters.reduce(function(values, param) {
    return Object.assign(values, { [param.name]: param.defaultValue });
  }, {} as ParameterValues);
}
