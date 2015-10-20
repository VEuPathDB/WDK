import _ from 'lodash';
import React from 'react/addons';
import ReactRouter from 'react-router';

import Dispatcher from './dispatcher/Dispatcher';
import WdkService from './utils/WdkService';
import AnswerViewStore from './stores/AnswerViewStore';
import RecordViewStore from './stores/RecordViewStore';
import AnswerViewActionCreator from './actioncreators/AnswerViewActionCreator';
import RecordViewActionCreator from './actioncreators/RecordViewActionCreator';

import * as Router from './router';

import dynamicModules from './dynamicModules';
let { ActionCreators, Components, Stores } = dynamicModules;

// expose libraries to global object, but only if they aren't already defined
if (window._ == null) window._ = _;
if (window.React == null) window.React = React;
if (window.ReactRouter == null) window.ReactRouter = ReactRouter;

class Container {

  constructor(values) {
    this._map = new Map(values);
  }

  get(token) {
    return this._map.get(token);
  }

}

function run({ rootUrl, endpoint, rootElement }) {
  let dispatcher = new Dispatcher;
  let service = new WdkService(endpoint);
  let container = new Container([
    [ AnswerViewStore, new AnswerViewStore(dispatcher) ],
    [ RecordViewStore, new RecordViewStore(dispatcher) ],
    [ AnswerViewActionCreator, new AnswerViewActionCreator(dispatcher, service) ],
    [ RecordViewActionCreator, new RecordViewActionCreator(dispatcher, service) ]
  ]);
  Router.start(rootUrl, rootElement, { container });
}

export { ActionCreators, Components, Stores, run };
