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
import { reducer } from './reducers';
import RestAPI from './services/restAPI';
import Routes from './routes';
import { createRestFilter } from './filters/restFilter';
import logFilter from './filters/logFilter';

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
function createApplication({ endpoint, rootElement, rootUrl }) {
  let restAPI = RestAPI.create(endpoint);
  let store = Store.create(reducer, [
    createRestFilter(restAPI),
    logFilter
  ]);

  let router = Router.create({
    routes: Routes.getRoutes(rootUrl),
    location: Router.HistoryLocation
  });

  router.run(function (Root, state) {
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
      React.render(<Root {...state} store={store}/>, rootElement);
    }
  });

  return { store, router, restAPI };
}

export default Wdk;
