import { keyBy, mapValues } from 'lodash';

import { Epic } from '../utils/ActionCreatorUtils';
import {
  ActiveQuestionUpdatedAction,
  ParamErrorAction,
  ParamInitAction,
  ParamStateUpdatedAction,
  ParamsUpdatedAction,
  ParamValueUpdatedAction,
  questionEpic,
  QuestionErrorAction,
  QuestionLoadedAction,
  QuestionNotFoundAction,
  GroupStateUpdatedAction,
  GroupVisibilityChangedAction
} from '../actioncreators/QuestionActionCreators';
import { Action } from '../dispatcher/Dispatcher';
import { paramEpic, reduce as paramReducer } from '../params';
import { Parameter, ParameterGroup, Question, RecordClass } from '../utils/WdkModel';
import WdkStore, { BaseState } from './WdkStore';

interface GroupState {
  isVisible: boolean;
}

export type QuestionState = {
  questionStatus: 'loading' | 'error' | 'not-found' | 'complete';
  question: Question & {
    parametersByName: Record<string, Parameter>;
    groupsByName: Record<string, ParameterGroup>
  };
  recordClass: RecordClass;
  paramValues: Record<string, string>;
  paramUIState: Record<string, any>;
  groupUIState: Record<string, GroupState>;
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

  getEpics(): Epic[] {
    return [ questionEpic, paramEpic ];
  }

}

function reduceQuestionState(state: QuestionState, action: Action): QuestionState {
  if (ActiveQuestionUpdatedAction.isType(action)) return {
    ...state,
    paramValues: action.payload.paramValues || {},
    questionStatus: 'loading'
  }

  if (QuestionLoadedAction.isType(action)) return {
    ...state,
    questionStatus: 'complete',
    question: normalizeQuestion(action.payload.question),
    recordClass: action.payload.recordClass,
    paramValues: action.payload.paramValues,
    paramErrors: action.payload.question.parameters.reduce((paramValues, param) =>
      Object.assign(paramValues, { [param.name]: undefined }), {}),
    paramUIState: action.payload.question.parameters.reduce((paramUIState, parameter) =>
      Object.assign(paramUIState, {
        [parameter.name]: paramReducer(
          parameter,
          undefined,
          ParamInitAction.create({
            parameter,
            questionName: action.payload.questionName,
            paramValues: action.payload.paramValues
          })
        )
      }), {}),
    groupUIState: action.payload.question.groups.reduce((groupUIState, group) =>
      Object.assign(groupUIState, { [group.name]: { isVisible: group.isVisible }}), {})
  }

  if (QuestionErrorAction.isType(action)) return {
    ...state,
    questionStatus: 'error'
  };

  if (QuestionNotFoundAction.isType(action)) return {
    ...state,
    questionStatus: 'not-found'
  };

  if (ParamValueUpdatedAction.isType(action)) return {
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

  if (ParamErrorAction.isType(action)) return {
    ...state,
    paramErrors: {
      ...state.paramErrors,
      [action.payload.paramName]: action.payload.error
    }
  };

  if (ParamsUpdatedAction.isType(action)) {
    const newParamsByName = keyBy(action.payload.parameters, 'name');
    const newParamValuesByName = mapValues(newParamsByName, param => param.defaultValue);
    const newParamErrors = mapValues(newParamsByName, () => undefined);
    // merge updated parameters into quesiton and reset their values
    return {
      ...state,
      paramValues: {
        ...state.paramValues,
        ...newParamValuesByName
      },
      paramErrors: {
        ...state.paramErrors,
        ...newParamErrors
      },
      question: {
        ...state.question,
        parameters: state.question.parameters
          .map(parameter => newParamsByName[parameter.name] || parameter)

      }
    };
  }

  if (ParamStateUpdatedAction.isType(action)) return {
    ...state,
    paramUIState: {
      ...state.paramUIState,
      [action.payload.paramName]: action.payload.paramState
    }
  };

  if (GroupVisibilityChangedAction.isType(action)) return {
    ...state,
    groupUIState: {
      ...state.groupUIState,
      [action.payload.groupName]: {
        ...state.groupUIState[action.payload.groupName],
        isVisible: action.payload.isVisible
      }
    }
  }

  if (GroupStateUpdatedAction.isType(action)) return {
    ...state,
    groupUIState: {
      ...state.groupUIState,
      [action.payload.groupName]: action.payload.groupState
    }
  }

  // finally, handle parameter specific actions
  return reduceParamState(state, action);
}

/**
 * Add parametersByName and groupsByName objects
 * @param question
 */
function normalizeQuestion(question: Question) {
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
