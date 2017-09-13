import { ActionThunk } from '../ActionCreator';
import { Question, QuestionParameter, ParameterValues, ParameterValue, OntologyTermSummary } from '../utils/WdkModel';

export const ACTIVE_QUESTION_UPDATED = 'question/active-question-updated';
export const QUESTION_LOADED = 'question/question-loaded';
export const QUESTION_ERROR = 'question/question-error';
export const QUESTION_NOT_FOUND = 'question/question-not-found';
export const PARAM_UPDATED = 'question/param-updated';
export const PARAM_ERROR = 'question/param-error';
export const PARAMS_UPDATED = 'question/params-updated';
export const ONTOLOGY_TERM_SUMMARY_LOADED = 'question/ontology-term-summary-loaded';
export const ONTOLOGY_TERM_SUMMARY_ERROR = 'question/ontology-term-summary-error';

export type Action = {
  type: typeof ACTIVE_QUESTION_UPDATED;
  payload: {
    id: string;
  };
} | {
  type: typeof QUESTION_LOADED;
  payload: {
    id: string;
    question: Question;
    paramValues: ParameterValues;
  };
} | {
  type: typeof QUESTION_ERROR | typeof QUESTION_NOT_FOUND;
  payload: {
    id: string;
  };
} | {
  type: typeof PARAM_UPDATED,
  payload: {
    id: string,
    paramName: string,
    paramValue: ParameterValue
  }
} | {
  type: typeof PARAM_ERROR,
  payload: {
    id: string,
    error: string,
    paramName: string
  }
} | {
  type: typeof PARAMS_UPDATED,
  payload: {
    id: string,
    paramValues: ParameterValues
  }
} | {
  type: typeof ONTOLOGY_TERM_SUMMARY_LOADED,
  payload: {
    id: string,
    paramName: string,
    ontologyId: string,
    ontologyTermSummary: OntologyTermSummary
  }
} | {
  type: typeof ONTOLOGY_TERM_SUMMARY_ERROR,
  payload: {
    id: string,
    paramName: string,
    ontologyId: string,
    error: string
  }
}

// TODO Initialize paramState. How will param initializer be resolved?
export function loadQuestion(urlSegment: string): ActionThunk<Action> {
  return (dispatch, { wdkService }) => {
    dispatch({ type: ACTIVE_QUESTION_UPDATED, payload: { id: urlSegment } });
    wdkService.getQuestionAndParameters(urlSegment).then(
      question => {
        dispatch({
          type: QUESTION_LOADED,
          payload: { id: urlSegment, question, paramValues: makeDefaultParamValues(question.parameters) }
        });
      },
      error => {
        dispatch({
          type: error.status === 404 ? QUESTION_NOT_FOUND : QUESTION_ERROR,
          payload: { id: urlSegment }
        });
      }
    )
  }
}

export function updateParamValue(
  urlSegment: string,
  paramName: string,
  paramValue: ParameterValue,
  paramValues: ParameterValues
): ActionThunk<Action> {
  return (dispatch, { wdkService }) => {
    dispatch({ type: PARAM_UPDATED, payload: { id: urlSegment, paramName, paramValue } });
    wdkService.getQuestionParamValues(urlSegment, paramName, paramValue, paramValues).then(
      paramValues => {
        dispatch({
          type: PARAMS_UPDATED,
          payload: { id: urlSegment, paramValues }
        });
      },
      error => {
        dispatch({
          type: PARAM_ERROR,
          payload: { id: urlSegment, error: error.message, paramName }
        });
      }
    );
  }
}

export function loadOnotologyTermSummary(
  urlSegment: string,
  paramName: string,
  paramValue: any,
  ontologyId: string,
  paramValues: ParameterValues
): ActionThunk<Action> {
  return (dispatch, { wdkService }) => {
    wdkService.getOntologyTermSummary(urlSegment, paramName, paramValue, ontologyId, paramValues).then(
      ontologyTermSummary => {
        dispatch({
          type: ONTOLOGY_TERM_SUMMARY_LOADED,
          payload: {
            id: urlSegment,
            paramName,
            ontologyId,
            ontologyTermSummary
          }
        })
      },
      error => {
        dispatch({
          type: ONTOLOGY_TERM_SUMMARY_ERROR,
          payload: {
            id: urlSegment,
            paramName,
            ontologyId,
            error: error.message
          }
        })
      }
    );
  }
}


// helpers

function makeDefaultParamValues(parameters: QuestionParameter[]) {
  return parameters.reduce(function(values, param) {
    return Object.assign(values, { [param.name]: param.defaultValue });
  }, {} as ParameterValues);
}
