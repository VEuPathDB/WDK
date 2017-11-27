import { ActionThunk } from '../ActionCreator';
import { init } from '../params';
import { Parameter, ParameterValue, ParameterValues, Question, RecordClass } from '../utils/WdkModel';

export const ACTIVE_QUESTION_UPDATED = 'question/active-question-updated';
export const QUESTION_LOADED = 'question/question-loaded';
export const QUESTION_ERROR = 'question/question-error';
export const QUESTION_NOT_FOUND = 'question/question-not-found';
export const PARAM_VALUE_UPDATED = 'question/param-updated';
export const PARAM_ERROR = 'question/param-error';
export const PARAMS_UPDATED = 'question/params-updated';
export const PARAM_STATE_UPDATED = 'question/param-state-updated';

type ActionBase = {
  payload: {
    questionName: string;
  }
}

export type Action = ActionBase & ({
  type: typeof ACTIVE_QUESTION_UPDATED;
  payload: {
    paramValues?: ParameterValues;
  };
} | {
  type: typeof QUESTION_LOADED;
  payload: {
    question: Question;
    recordClass: RecordClass;
    paramValues: ParameterValues;
  };
} | {
  type: typeof QUESTION_ERROR | typeof QUESTION_NOT_FOUND;
} | {
  type: typeof PARAM_VALUE_UPDATED,
  payload: {
    parameter: Parameter,
    paramValues: ParameterValues,
    paramValue: ParameterValue
  }
} | {
  type: typeof PARAM_ERROR,
  payload: {
    error: string,
    paramName: string
  }
} | {
  type: typeof PARAMS_UPDATED,
  payload: {
    parameters: Parameter[]
  }
} | {
  type: typeof PARAM_STATE_UPDATED,
  payload: {
    paramName: string;
    paramState: any
  }
})

export function loadQuestion(questionName: string, paramValues?: ParameterValues): ActionThunk<Action> {
  return (dispatch, { wdkService }) => {
    dispatch({ type: ACTIVE_QUESTION_UPDATED, payload: { questionName, paramValues } });

    const question$ = paramValues == null
      ? wdkService.getQuestionAndParameters(questionName)
      : wdkService.getQuestionGivenParameters(questionName, paramValues);

    const recordClass$ = question$.then(question =>
      wdkService.findRecordClass(rc => rc.name == question.recordClassName));

    Promise.all([question$, recordClass$]).then(
      ([question, recordClass]) => {
        paramValues = paramValues || makeDefaultParamValues(question.parameters);
        dispatch({ type: QUESTION_LOADED, payload: { questionName, question, recordClass, paramValues } });
        for (let parameter of question.parameters) {
          dispatch(init({questionName, parameter, paramValues}));
        }
      },
      error => {
        dispatch({
          type: error.status === 404 ? QUESTION_NOT_FOUND : QUESTION_ERROR,
          payload: { questionName }
        })
      }
    )
  }
}

export function updateParamValue(
  ctx: {
    questionName: string,
    parameter: Parameter,
    paramValues: ParameterValues
  },
  paramValue: ParameterValue
): Action {
  return { type: PARAM_VALUE_UPDATED, payload: { ...ctx, paramValue } };
}

export function updateDependentParams(
  ctx: {
    questionName: string,
    parameter: Parameter,
    paramValues: ParameterValues
  },
  paramValue: ParameterValue
): ActionThunk<any> {
  return (dispatch, { wdkService }) => {
    const { questionName, parameter, paramValues } = ctx;
    const newParamValues = { ...paramValues, [parameter.name]: paramValue };
    wdkService.getQuestionParamValues(questionName, parameter.name, paramValue, paramValues).then(
      parameters => {
        dispatch({ type: PARAMS_UPDATED, payload: { questionName, parameters } });
        for (let parameter of parameters) {
          // TODO Only init visible params. Probably take `visibleGroups` as
          // second param above and only call the following if parameter.group
          // is in list.
          dispatch(init({questionName, parameter, paramValues: newParamValues}));
        }
      },
      error => {
        dispatch({
          type: PARAM_ERROR,
          payload: { questionName, error: error.message, paramName: parameter.name }
        });
      }
    );
  }
}


// Helpers
// -------

function makeDefaultParamValues(parameters: Parameter[]) {
  return parameters.reduce(function(values, param) {
    return Object.assign(values, { [param.name]: param.defaultValue });
  }, {} as ParameterValues);
}
