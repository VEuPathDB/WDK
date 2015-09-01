import test from 'tape';
import Store from '../../../webapp/wdk/js/client/core/store';

// helpers
// =======

function timeout(time) {
  return new Promise(function(resolve) {
    setTimeout(resolve, time);
  });
}

// create
// ======

test('create should call reducer for initial value', function(t) {
  t.plan(1);
  Store.create(function() { t.ok(true) });
});


// dispatch
// ========

test('dispatch should map state with reducer', function(t) {
  t.plan(1);

  let store = Store.create(function counter(count = 0, action) {
    return action.type === 'inc' ? count + 1 : count;
  });

  let dispatches = [];

  for (let i = 0; i < 5; i++) {
    dispatches.push(store.dispatch({ type: 'inc' }));
  }

  Promise.all(dispatches).then(function() {
    t.ok(store.getState() === 5, 'actual state is ' + store.getState());
  });
});

test('subscribe callback execution should complete before the next dispatch is executed', function(t) {
  t.plan(1);

  let store = Store.create(function(count = 0, action) {
    return action === 'inc' ? count + 1 : count;
  });

  let called = false;

  store.subscribe(function() {
    if (called) return;
    called = true;

    let stateBeforeDispatch = store.getState();
    store.dispatch('inc');
    let stateAfterDispatch = store.getState();

    t.deepEqual(
      stateBeforeDispatch,
      stateAfterDispatch,
      "The store's state was changed before the callback completed"
    );
  });

  store.dispatch('inc');
});

// filters
test('filters should be called before original dispatch', function(t) {
  t.plan(1);

  let store = Store.create(reducer);
  let originalDispatch = store.dispatch;
  store = Store.applyDispatchFilters(store, [ filter ]);

  function reducer(state, action) {
    return 1;
  }

  function filter(store, next, action) {
    t.equal(next, originalDispatch);
    next(action);
  }

  store.dispatch();
});

test.skip('dispatch should return a Promise that resolves after action is handled', function(t) {
  t.plan(2);

  let store = Store.create(function(state = 0, action) {
    if (action.type === 'inc') {
      return state + 1;
    }
    return state;
  });

  store.dispatch(function(dispatch) {
    return new Promise(function(resolve, reject) {
      setTimeout(function() {
        resolve({ type: 'inc' });
      }, 1000);
    }).then(dispatch);
  })
  .then(function() {
    t.ok(store.getState() === 1, 'post dispatch');
  });

  t.ok(store.getState() === 0, 'pre dispatch');

});

test.skip('Multiple dispatch calls can be composed into a single transaction', function(t) {
  t.plan(1);

  let store = Store.create(function(state = 0, action) {
    return action.type === 'inc' ? state + 1 : state;
  });

  function actionA() {
    return {
      type: 'inc'
    };
  }

  function actionB() {
    return function(dispatch) {
      return timeout(1000).then(function() {
        return dispatch({
          type: 'inc'
        });
      });
    };
  }

  let composedActions = Promise.all([
    store.dispatch(actionA()),
    store.dispatch(actionB())
  ]);

  composedActions.then(function() {
    t.ok(store.getState() === 2, 'post dispatch');
  });
});

// subscribe
// =========

test('subscribe should call callback function when state changes', function(t) {
  let plan = 5;
  t.plan(plan);

  let store = Store.create(function(state = 0, action) {
    return action.type === 'test' ? state + 1 : state;
  });

  store.subscribe(function(state) {
    t.ok(true);
  });

  for (let i = 0; i < plan; i++) {
    store.dispatch({ type: 'test' });
    store.dispatch({ type: 'skip' });
  }
});
