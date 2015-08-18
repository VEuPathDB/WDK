// FIXME Use class. Extension with closures is tricky!

function create(reducer, filters = []) {
  let state = reducer(undefined, { type: '@@wdk/INIT' });
  let callbacks = [];
  let isDispatching = false;

  return applyDispatchFilters({
    getState,
    dispatch,
    subscribe
  }, filters);

  function getState() {
    return state;
  }

  // Return a Promise. This allows the caller to perform an operation
  // after an aysnc action creator is called. We're using Promises here
  // becuase they compose well, but we might decide on Observables in
  // the future since they support cancellation.
  function dispatch(action) {
    if (isDispatching) {
      throw new Error('Cannot dispatch during a dispatch');
    }

    isDispatching = true;

    let nextState = reducer(state, action);
    if (state !== nextState) {
      state = nextState;
      callbacks.forEach(function(callback) {
        callback(state);
      });
    }

    isDispatching = false;

    return state;
  }

  // Call `callback` when the state is changed. Also calls `callback`
  // immediately with current state, a la Rx replay(1) operator.
  function subscribe(callback) {
    callbacks.push(callback);
    // callback(state);
    return {
      dispose() {
        let index = callbacks.indexOf(callback);
        if (index < 0) return;
        callbacks.splice(index, 1);
      }
    };
  }
}

// Creates a dispatch function that calls each filter in the filters array from
// left to right, finally calling the original dispatch function. A new Store
// is return.
function applyDispatchFilters(store, filters) {
  let newStore = Object.assign({}, store);
  let finalDispatch = filters.reduceRight(function(next, filter) {
    return function(action) {
      return filter(newStore, next, action);
    };
  }, newStore.dispatch);

  return Object.assign(newStore, {
    dispatch: finalDispatch
  });
}

export default { create, applyDispatchFilters };
