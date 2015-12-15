import _ from 'lodash';
import React from 'react';
import ReactRouter from 'react-router';
import Dispatcher from './dispatcher/Dispatcher';
import WdkService from './utils/WdkService';
import AnswerViewStore from './stores/AnswerViewStore';
import RecordViewStore from './stores/RecordViewStore';
import AnswerViewActionCreator from './actioncreators/AnswerViewActionCreator';
import RecordViewActionCreator from './actioncreators/RecordViewActionCreator';
import * as Router from './router';
import dynamicModules from './dynamicModules';

export let { ActionCreators, Components, Stores } = dynamicModules;

export function run({ rootUrl, endpoint, rootElement }) {
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

// expose libraries to global object, but only if they aren't already defined
if (window._ == null) window._ = _;
if (window.React == null) window.React = React;
if (window.ReactRouter == null) window.ReactRouter = ReactRouter;

// Simple wrapper to Map that only exposes `get` method.
// This is used to hold singleton instance of Stores and ActionCreators
// and is passed to ViewControllers so they can "look up" dependencies.
//
// An alternative approach to is create the singletons in a separate module
// and to export them from that module. This would remove the need to pass the
// Container throughout the app, but it would also make the singletons globally
// accessible. Both of these help to make WDK extensible (additional Stores and
// ActionCreators, custom Components, etc).
class Container {

  constructor(values) {
    this._map = new Map(values);
  }

  get(token) {
    return this._map.get(token);
  }

}
