import React from 'react';
import Router from 'react-router';
import Dispatcher from './dispatcher';
import Service from './service';

function createContext(config = {}) {

  const {
    endpoint = '/service',
    rootUrl = '/',
    rootElement = document.body,
    routes
  } = config;

  const stores = new Map();
  const actions = new Map();
  const dispatcher = Dispatcher.createDispatcher();
  const service = Service.createService(endpoint);
  const router = Router.create({
    routes: routes,
    location: Router.HistoryLocation
  });

  const context = {

    get dispatcher() {
      return dispatcher;
    },

    get service() {
      return service;
    },

    get router() {
      return router;
    },

    addStore(token, store) {
      stores.set(token, store);
    },

    getStore(token) {
      return stores.get(token);
    },

    addActions(token, actionSet) {
      actions.set(token, actionSet);
    },

    getActions(token) {
      return actions.get(token);
    }

  };

  // Defer routing so that stores and actions can be added.
  // We can probably be a little smarter about this.
  setTimeout(makeRouterRunFn(router, rootElement, context), 0);

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
      React.render(<Handler state={state} application={context}/>, rootElement);
    }
  };
}

function makeRouterRunFn(router, rootElement, context) {
  return function run() {
    router.run(makeRouterCallback(context, rootElement));
  };
}

export default {
  createContext
};
