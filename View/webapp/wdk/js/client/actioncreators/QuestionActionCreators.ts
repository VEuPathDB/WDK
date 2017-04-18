import { ActionCreator } from '../ActionCreator';
import { Question } from '../utils/WdkModel';

export const ACTIVE_QUESTION_UPDATED = 'question/active-question-updated';
export const QUESTION_LOADED = 'question/question-loaded';
export const QUESTION_ERROR = 'question/question-error';
export const QUESTION_NOT_FOUND = 'question/question-not-found';

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
  };
} | {
  type: typeof QUESTION_ERROR | typeof QUESTION_NOT_FOUND;
  payload: {
    id: string;
  };
}

export const loadQuestion: ActionCreator<Action> = (urlSegment: string) => (dispatch, { wdkService }) => {
  dispatch({ type: ACTIVE_QUESTION_UPDATED, payload: { id: urlSegment } });
  wdkService.getQuestionAndParameters(urlSegment).then(
    question => {
      dispatch({
        type: QUESTION_LOADED,
        payload: { id: urlSegment, question }
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
