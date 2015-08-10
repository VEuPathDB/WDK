import React from 'react';
import { Dispatcher } from 'flux';

function createDispatcher() {

  if ("production" !== process.env.NODE_ENV) {
    let proto = Dispatcher.prototype;
    let dispatch = proto.dispatch;


    proto.dispatch = function debugDispatch(action) {
      if (action.type === undefined) {
        console.warn(
          'Warning: Expected a `type` property on action %o, but got undefined.',
          action
        );
      }
      console.info('dispatching', action);
      dispatch.call(this, action);
    };

  }

  return new Dispatcher;
}

export default {
  createDispatcher
}
