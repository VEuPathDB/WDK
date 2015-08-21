/**
 * Bootstrap the WDK Flux application. Initialize the environment and wire up
 * dependencies.
 *
 * Export the wdk object.
 */

import React from 'react';
import Router from 'react-router';
import _ from 'lodash';

import * as components from './components';
import * as actions from './actions';

import Store from './core/store';
import reducer from './reducer';
import RestAPI from './services/restAPI';
import Routes from './routes';
import { createRestFilter } from './filters/restFilter';

// expose libraries to global object, but only if they aren't already defined
if (window._ == null) window._ = _;
if (window.React == null) window.React = React;
if (window.ReactRouter == null) window.ReactRouter = Router;


let Wdk = {
  actions,
  components,
  createApplication
};

/**
 * Starts a WDK application instance based on the provided configuration.
 *
 * @param {object} config Application configuration
 * @param {string} config.endpoint Base URL for the RESTful WDK Service
 * @param {string} config.rootUrl Root element to render application
 * @param {element} config.rootElement Root element to render application
 */
function createApplication(config) {
  let restAPI = RestAPI.create(config.endpoint);
  let store = Store.create(reducer, [
    createRestFilter(restAPI),
    logFilter
  ]);

  let router = Router.run(
    Routes.getRoutes(config.rootUrl),
    Router.HistoryLocation,
    function (Root, state) {
      // XXX Implement router filters?
      if ('auth_tkt' in state.query) {
        state.query.auth_tkt = undefined;
        router.replaceWith(
          state.pathname,
          state.params,
          state.query
        );
      }
      else {
        React.render(<Root state={state} store={store}/>, config.rootElement);
      }
    }
  );

  return store;
}

function logFilter(store, next, action) {
  console.group(action.type);
  console.info('dispatching', action);
  let result = next(action);
  console.log('state', store.getState());
  console.groupEnd(action.type);
  return result;
}

export default Wdk;
