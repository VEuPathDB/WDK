import { default as WdkStore, BaseState } from './WdkStore';
import { Action, ACTIVE_QUESTION_UPDATED, QUESTION_LOADED, QUESTION_ERROR, QUESTION_NOT_FOUND } from '../actioncreators/QuestionActionCreators';
import { Question } from '../utils/WdkModel';

export type State = BaseState & {
  questionId: string;
  questionStatus: 'loading' | 'error' | 'complete' | 'not-found';
  question?: Question;
}

export default class QuestionStore extends WdkStore<BaseState> {

  handleAction(state: State, action: Action): State {
    switch(action.type) {

      case ACTIVE_QUESTION_UPDATED:
        return {
          ...state,
          questionId: action.payload.id,
          questionStatus: 'loading',
          question: undefined
        }

      case QUESTION_LOADED:
        return state.questionId === action.payload.id ? {
          ...state,
          questionStatus: 'complete',
          question: action.payload.question
        } : state;

      case QUESTION_ERROR:
        return state.questionId === action.payload.id ? {
          ...state,
          questionStatus: 'error'
        } : state;

      case QUESTION_NOT_FOUND:
        return state.questionId === action.payload.id ? {
          ...state,
          questionStatus: 'not-found'
        }: state;

      default:
        return state;

    }
  }

}
