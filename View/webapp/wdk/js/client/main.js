import './exposeModules';

import mapValues from 'lodash/object/mapValues';
import values from 'lodash/object/values';

import Dispatcher from './dispatcher/Dispatcher';
import WdkService from './utils/WdkService';
import * as Router from './router';
import * as ActionCreators from './actioncreators';
import * as Components from './components';
import * as Stores from './stores';

export { Components };

export function run({ rootUrl, endpoint, rootElement }) {
  let dispatcher = new Dispatcher;
  let service = new WdkService(endpoint);
  let stores = mapValues(Stores, Store => new Store(dispatcher));
  let actionCreators = mapValues(ActionCreators, ActionCreator => new ActionCreator(dispatcher, service));

  let context = {
    dispatcher,
    service,
    stores,
    actionCreators
  };

  if (__DEV__) logActions(context);

  let router = Router.start(rootUrl, rootElement, context);

  return Object.assign({ router }, context);
}

function logActions(context) {
  let { dispatcher, stores } = context;
  // Debug logging - TODO Only enable in development environments
  dispatcher.register(action => {
    dispatcher.waitFor(values(stores).map(s => s.getDispatchToken()));
    console.group(action.type);
    console.info("dispatching", action);
    console.info("state", mapValues(stores, store => store.getState()));
    console.groupEnd(action.type);
  });
}
