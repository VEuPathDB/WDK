import createActionCreators from '../utils/createActionCreators';
import {
  AppLoading,
  AppError,
  QuestionsAdded
} from '../ActionType';

export default createActionCreators({

  loadQuestions() {
    var { dispatch, serviceAPI } = this;

    dispatch(AppLoading({ isLoading: true }));

    serviceAPI.getResource('/question?expandQuestions=true')
      .then(questions => {
        dispatch(QuestionsAdded({ questions }));
        dispatch(AppLoading({ isLoading: false }));
      }, error => {
        dispatch(AppError({ error }));
      })
      .catch(err => console.assert(false, err));
  }

});
