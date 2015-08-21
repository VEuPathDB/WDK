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

  function dispatch(action) {
    if (isDispatching) {
      throw new Error('Cannot dispatch during a dispach.');
    }

    isDispatching = true;

    let oldState = state;
    state = reducer(state, action);

    isDispatching = false;

    return Promise.resolve(state).then(function(state) {
      if (state !== oldState) {
        callbacks.forEach(function(callback) {
          callback(state);
        });
      }
    });
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
