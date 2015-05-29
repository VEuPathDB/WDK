/**
 * Bootstrap the WDK Flux application. Initialize the environment and wire up
 * dependencies.
 *
 * Export the wdk object.
 */

import React from 'react';
import Router from 'react-router';
import Immutable from 'immutable';
import _ from 'lodash';
import Context from './core/context';
import Routes from './routes';
import AnswerStore from './stores/answerStore';
import AppStore from './stores/appStore';
import QuestionStore from './stores/questionStore';
import RecordClassStore from './stores/recordClassStore';
import AnswerActions from './actions/answerActions';
import QuestionActions from './actions/questionActions';

// expose libraries to global object, but only if they aren't already defined
if (window._ == null) window._ = _;
if (window.Immutable == null) window.Immutable = Immutable;
if (window.React == null) window.React = React;
if (window.ReactRouter == null) window.ReactRouter = Router;


const Wdk = {

  /**
   * Starts a WDK application instance based on the provided configuration.
   *
   * @param {object} config Application configuration
   * @param {string} config.endpoint Base URL for the RESTful WDK Service
   * @param {string} config.rootUrl Root element to render application
   * @param {element} config.rootElement Root element to render application
   * @param {function} config.recordComponentResolver Function used to resolve
   *        a record component based on the record class name. The function
   *        will be called with the record class name and a reference to the
   *        default record component. This is useful for wrapping or for using
   *        the default without modifications.
   */
  createApplication(config) {
    config.routes = Routes.getRoutes(config.rootUrl);
    const context = Context.createContext(config);
    _.each(Wdk.stores, function(Store) {
      context.addStore(Store, Store.createStore(context));
    });
    _.each(Wdk.actions, function(Actions) {
      context.addActions(Actions, Actions.createActions(context));
    });
    return context;
  },

  stores: {
    AnswerStore,
    AppStore,
    QuestionStore,
    RecordClassStore
  },

  actions: {
    AnswerActions,
    QuestionActions
  }

};


export default Wdk;
