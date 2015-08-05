import Dispatcher from './dispatcher';
import Service from './service';

function createContext(config = {}) {

  const {
    endpoint = '/service',
    rootUrl = '/',
    rootElement = document.body,
    router
  } = config;

  const stores = {};
  const actions = {};
  const dispatcher = Dispatcher.createDispatcher();
  const service = Service.createService(endpoint);

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

    get stores() {
      return stores;
    },

    get actions() {
      return actions;
    },

    addStore(name, store) {
      Object.defineProperty(stores, name, {
        enumerable: true,
        value: store
      });
    },

    addActions(name, actionSet) {
      Object.defineProperty(actions, name, {
        enumerable: true,
        value: actionSet
      });
    }

  };

  return context;
}

export default {
  createContext
};
