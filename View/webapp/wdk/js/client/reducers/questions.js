import {
  QUESTIONS_ADDED
} from '../constants/actionTypes';

export default function questions(questions = [], action) {
  if (action.type === QUESTIONS_ADDED) {
    questions = action.response;
  }
  return questions;
}
