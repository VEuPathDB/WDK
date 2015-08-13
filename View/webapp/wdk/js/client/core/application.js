function create(stores, config) {
  let state = mapState(stores, 'getInitialState', {});
  let observers = [];

  function dispatch(action) {
    if (typeof action === 'function') {
      return action(dispatch, state, config);
    }
    else if (Array.isArray(action)) {
      let groupLabel = action.map(a => a.type).join(' ');
      console.groupCollapsed(groupLabel);
      dispatchBatch(action);
      console.groupEnd(groupLabel);
    }
    else {
      console.groupCollapsed(action.type);
      dispatchSingle(action);
      console.groupEnd(action.type);
    }
    observers.forEach(function({ onNext, onError }) {
      typeof error === 'undefined' ? onNext(state) : onError(error);
    });
  }

  function dispatchSingle(action) {
    let error;
    try {
      state = mapState(stores, 'update', state, action);
    }
    catch (e) {
      error = e;
    }
    finally {
      console.info('Dispatched', action);
      error ? console.error('Error', error) : console.info('State', state);
    }
  }

  function dispatchBatch(actions) {
    actions.forEach(dispatchSingle);
  }

  function subscribe(onNext, onError = noop) {
    let observer = { onNext, onError };
    observers.push(observer);
    observer.onNext(state);
    return {
      dispose() {
        let index = observers.indexOf(observer);
        if (index < 0) return;
        observers.splice(index, 1);
      }
    };
  }

  return {
    get state() {
      return state;
    },
    get config() {
      return config;
    },
    dispatch,
    subscribe
  };
}

function mapState(reducers, funcName, state, action) {
  return Object.keys(reducers).reduce(function(nextState, key) {
    let func = reducers[key][funcName] || noop;
    return Object.assign(nextState, {
      [key]: func(state[key], action)
    });
  }, {});
}

function noop(){}

export default { create };
