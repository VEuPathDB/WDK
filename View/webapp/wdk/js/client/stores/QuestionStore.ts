import { keyBy } from 'lodash';

import {
  Action,
  ACTIVE_QUESTION_UPDATED,
  PARAM_ERROR,
  PARAM_STATE_UPDATED,
  PARAM_VALUE_UPDATED,
  PARAMS_UPDATED,
  QUESTION_ERROR,
  QUESTION_LOADED,
  QUESTION_NOT_FOUND,
} from '../actioncreators/QuestionActionCreators';
import { reduce as paramReducer } from '../params';
import { Question, ParameterGroup, Parameter, RecordClass } from '../utils/WdkModel';
import WdkStore, { BaseState } from './WdkStore';

type QuestionState = {
  questionStatus: 'loading' | 'error' | 'not-found' | 'complete';
  question: Question & {
    parametersByName: Record<string, Parameter>;
    groupsByName: Record<string, ParameterGroup>
  };
  recordClass: RecordClass;
  paramValues: Record<string, string>;
  paramUIState: Record<string, any>;
  paramErrors: Record<string, string | undefined>;
}

export type State = BaseState & {
  questions: Record<string, QuestionState>;
}

export default class QuestionStore extends WdkStore<State> {

  getInitialState() {
    return {
      ...super.getInitialState(),
      questions: {}
    }
  }

  handleAction(state: State, action: Action): State {
    const { questionName } = action.payload;
    return questionName == null ? state : {
      ...state,
      questions: {
        ...state.questions,
        [questionName]: reduceQuestionState(state.questions[questionName], action)
      }
    };
  }

}

function reduceQuestionState(state: QuestionState, action: Action): QuestionState {
  if (action.type === ACTIVE_QUESTION_UPDATED) {
    return {
      ...state,
      paramValues: action.payload.paramValues || {},
      questionStatus: 'loading'
    }
  }

  switch (action.type) {

    case QUESTION_LOADED:
      return {
        ...state,
        questionStatus: 'complete',
        question: normalizeQuesiton(action.payload.question),
        recordClass: action.payload.recordClass,
        paramValues: action.payload.paramValues,
        paramErrors: action.payload.question.parameters.reduce((paramValues, param) =>
          Object.assign(paramValues, { [param.name]: undefined }), {}),
        paramUIState: action.payload.question.parameters.reduce((paramUIState, param) =>
          Object.assign(paramUIState, { [param.name]: paramReducer(param, undefined, { type: 'init' }) }), {})
      };

    case QUESTION_ERROR:
      return {
        ...state,
        questionStatus: 'error'
      };

    case QUESTION_NOT_FOUND:
      return {
        ...state,
        questionStatus: 'not-found'
      };

    case PARAM_VALUE_UPDATED:
      return {
        ...state,
        paramValues: {
          ...state.paramValues,
          [action.payload.parameter.name]: action.payload.paramValue
        },
        paramErrors: {
          ...state.paramErrors,
          [action.payload.parameter.name]: undefined
        }
      };

    case PARAM_ERROR:
      return {
        ...state,
        paramErrors: {
          ...state.paramErrors,
          [action.payload.paramName]: action.payload.error
        }
      };

    case PARAMS_UPDATED: {
      let newParamsByName = keyBy(action.payload.parameters, 'name');
      // merge updated parameters into quesiton
      return {
        ...state,
        question: {
          ...state.question,
          parameters: state.question.parameters
            .map(parameter => newParamsByName[parameter.name] || parameter)

        }
      };
    }

    case PARAM_STATE_UPDATED:
      return {
        ...state,
        paramUIState: {
          ...state.paramUIState,
          [action.payload.paramName]: action.payload.paramState
        }
      };

  }

  // finally, handle parameter specific actions
  return reduceParamState(state, action);
}

/**
 * Add parametersByName and groupsByName objects
 * @param question
 */
function normalizeQuesiton(question: Question) {
  return {
    ...question,
    parametersByName: keyBy(question.parameters, 'name'),
    groupsByName: keyBy(question.groups, 'name')
  }
}

function reduceParamState(state: QuestionState, action: any) {
  const { parameter } = action.payload;
  if (parameter) {
    return {
      ...state,
      paramUIState: {
        ...state.paramUIState,
        [parameter.name]: paramReducer(parameter, state.paramUIState[parameter.name], action)
      }
    }
  }

  return state;

}
