import createActionCreators from '../utils/createActionCreators';
import * as ActionType from '../ActionType';

export default createActionCreators({

  loadQuestions() {
    var { dispatch, serviceAPI } = this;

    dispatch({ type: ActionType.QUESTION_LIST_LOADING });

    serviceAPI.getResource('/question')
      .then(questions => {
        dispatch({ type: ActionType.QUESTION_LIST_LOAD_SUCCESS, questions });
      }, error => {
        dispatch({ type: ActionType.QUESTION_LIST_LOAD_ERROR, error });
      })
      .catch(err => console.assert(false, err));
  }

});
