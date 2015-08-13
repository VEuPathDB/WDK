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
import Application from './core/application';
import ContextMixin from './utils/contextMixin';
import Routes from './routes';
import * as components from './components';
import * as state from './state';
import * as actions from './actions';

// expose libraries to global object, but only if they aren't already defined
if (window._ == null) window._ = _;
if (window.Immutable == null) window.Immutable = Immutable;
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
  let context = Application.create(state, config);

  let router = Router.create({
    routes: Routes.getRoutes(config.rootUrl),
    location: Router.HistoryLocation
  });

  // Defer routing so that stores and actions can be added.
  // We can probably be a little smarter about this.
  setTimeout(makeRouterRunFn(router, config.rootElement, context), 0);

  return context;
}


function makeRouterCallback(context, rootElement) {
  return function routerCallback(Handler, state) {
    // XXX Implement router filters?
    if ('auth_tkt' in state.query) {
      state.query.auth_tkt = undefined;
      context.router.replaceWith(
        state.pathname,
        state.params,
        state.query
      );
    }
    else {
      React.render(<Handler state={state} context={context}/>, rootElement);
    }
  };
}

function makeRouterRunFn(router, rootElement, context) {
  return function run() {
    router.run(makeRouterCallback(context, rootElement));
  };
}

export default Wdk;
