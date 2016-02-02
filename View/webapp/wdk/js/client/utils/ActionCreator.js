export default class ActionCreator {

  constructor(dispatcher, service) {
    this._dispatch = dispatcher.dispatch.bind(dispatcher);
    this._service = service;
    this._errorHandler = actionType => error => {
      this._dispatch({
        type: actionType,
        payload: { error }
      });
      throw error;
    };
  }
}
