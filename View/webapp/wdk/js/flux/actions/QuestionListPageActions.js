import di from 'di';
import Dispatcher from '../Dispatcher';
import ServiceAPI from '../ServiceAPI';
import * as ActionType from '../ActionType';

export default class QuestionListPageActions {

  constructor(dispatcher, serviceAPI) {
    this.dispatcher = dispatcher;
    this.serviceAPI = serviceAPI;
  }

  loadQuestions() {
    var { dispatcher, serviceAPI } = this;

    dispatcher.dispatch({ type: ActionType.QUESTION_LIST_LOADING });

    serviceAPI.getResource('/question')
      .then(questions => {
        dispatcher.dispatch({ type: ActionType.QUESTION_LIST_LOAD_SUCCESS, questions });
      }, error => {
        dispatcher.dispatch({ type: ActionType.QUESTION_LIST_LOAD_ERROR, error });
      })
      .catch(err => console.assert(false, err));
  }

}

di.annotate(QuestionListPageActions, new di.Inject(Dispatcher, ServiceAPI));
