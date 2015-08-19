function create(reducer, filters = []) {
  let state = reducer(undefined, { type: '@@wdk/INIT' });
  let callbacks = [];

  return applyDispatchFilters({
    getState,
    dispatch,
    subscribe
  }, filters);

  function getState() {
    return state;
  }

  function dispatch(action) {
    // Apply action asynchronously. Using a Promise callback will enqueue the
    // task as a microtask. This means that it will be invoked immediately
    // after the current execution script, but before the next event loop.
    // The effect of this is that if an action is called within a subscribe
    // callback, the rest of the callback will be executed before the action
    // is dispatched. With React, this is mostly not an issue since the callback
    // will typically simply call `component.setState`, which is generally
    // asynchronous. However, React does not guarantee this in all scenarios,
    // thus we could potentially operate on a stale state. Most likely, this
    // would only become a serious issue with nested components observing the
    // store, which would ideally be avoided.
    return Promise.resolve().then(function() {
      let nextState = reducer(state, action);
      if (state !== nextState) {
        state = nextState;
        callbacks.forEach(function(callback) {
          callback(state);
        });
      }
      return state;
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
