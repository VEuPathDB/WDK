import {
  QUESTIONS_ADDED
} from '../constants/actionTypes';

function update(questions = [], action) {
  if (action.type === QUESTIONS_ADDED) {
    questions = action.questions;
  }
  return questions;
}

export default {
  update
};
