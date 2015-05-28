import {
  AppLoading,
  AppError,
  QuestionsAdded
} from '../ActionType';

function createActions({ dispatcher, service }) {
  return {
    loadQuestions() {
      dispatcher.dispatch(AppLoading({ isLoading: true }));
      service.getResource('/question?expandQuestions=true')
        .then(questions => {
          dispatcher.dispatch(QuestionsAdded({ questions }));
          dispatcher.dispatch(AppLoading({ isLoading: false }));
        }, error => {
          dispatcher.dispatch(AppError({ error }));
        })
        .catch(err => console.assert(false, err));
    }
  };
}

export default { createActions };
