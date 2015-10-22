export default class ActionCreator {

  constructor(dispatcher, service) {
    this._dispatch = dispatcher.dispatch.bind(dispatcher);
    this._service = service;
  }

}
