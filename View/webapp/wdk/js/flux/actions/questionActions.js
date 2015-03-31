import createActionCreators from '../utils/createActionCreators';
import {
  QUESTION_LIST_LOADING,
  QUESTION_LIST_LOAD_SUCCESS,
  QUESTION_LIST_LOAD_ERROR
} from '../ActionType';

export default createActionCreators({

  loadQuestions() {
    var { dispatch, serviceAPI } = this;

    dispatch({ type: QUESTION_LIST_LOADING });

    serviceAPI.getResource('/question?expandQuestions=true')
      .then(questions => {
        dispatch({ type: QUESTION_LIST_LOAD_SUCCESS, questions });
      }, error => {
        dispatch({ type: QUESTION_LIST_LOAD_ERROR, error });
      })
      .catch(err => console.assert(false, err));
  }

});
