import assert from 'assert';
import { Dispatcher } from 'flux';
import Store from 'wdk/flux/Store';
import Action from 'wdk/flux/utils/Action';

const createStore = Store.createStore;
const update = (function() {
  function spy() {
    spy.count++;
  }
  spy.reset = function() {
    spy.count = 0;
  }
  spy.count = 0;
  return spy;
}());

let dispatcher;

beforeEach(function() {
  update.reset();
  dispatcher = new Dispatcher();
});

describe('Store', function() {

  var IncrementAction = Action({
    incrementBy: 1
  });

  describe('.createStore', function() {

    it('should return an object with Store methods', function() {
      const store = createStore(null);
      assert(typeof store === 'object');
      assert(typeof store.register === 'function');
      assert(typeof store.unregister === 'function');
      assert(typeof store.subscribe === 'function');
    });

    it('should use undefined as initial value if not defined', function() {
      const store = createStore((state, action) => action.value);
      store.register(dispatcher);
      assert(store.value === undefined);
      dispatcher.dispatch({ value: 10 });
      assert(store.value === 10);
    });

    it('should not alter state if update function not defined', function() {
      const store = createStore(10);
      store.register(dispatcher);
      assert(store.value === 10);
      dispatcher.dispatch({ value: 10 });
      assert(store.value === 10);
    });

  });

  describe('store.register', function() {
    let store;

    beforeEach(function() {
      store = Store.createStore(1, update);
    });

    it('should only allowing registering with one dispatcher at a time', function() {
      store.register(dispatcher);
      assert.throws(function() {
        store.register(dispatcher);
      });
      store.unregister(dispatcher);
    });

    it('should wire up update function with calls to dispatcher.dispatch', function() {
      store.register(dispatcher);
      dispatcher.dispatch({ type: 'any' });
      assert(update.count === 1);
      dispatcher.dispatch({ type: 'any' });
      assert(update.count === 2);
      store.unregister(dispatcher);
    });
  });

  describe('store.unregister', function() {
    it('should remove update function from being called when dispatcher.dispatch called', function() {
      var store = Store.createStore(1, update);
      store.register(dispatcher);
      dispatcher.dispatch({ type: 'any' });
      dispatcher.dispatch({ type: 'any' });
      store.unregister(dispatcher);
      dispatcher.dispatch({ type: 'any' });
      assert(update.count === 2);
    });
  });

  describe('store.subscribe', function() {
    let store;

    beforeEach(function() {
      store = Store.createStore(1, function(count, action) {
        if (action.type === 'inc') {
          return count + action.by;
        }
      });
      store.register(dispatcher);
    });

    afterEach(function() {
      store.unregister(dispatcher);
    });

    it('should notify immediately upon subscription with latest value', function() {
      dispatcher.dispatch({ type: 'inc', by: 2 });
      const sub = store.subscribe(value => assert(value === 3));
      sub.dispose();
    });

    it('should notify on updates with latest value', function(done) {
      // inc by 2, then subscribe, then inc by 2 again.
      dispatcher.dispatch({ type: 'inc', by: 2 });
      const sub = store.subscribe(value => {
        if (value === 5) {
          done();
        }
      });
      dispatcher.dispatch({ type: 'inc', by: 2 });
      sub.dispose();
    });

    it('should stop notifying when disposed is called on subscription object', function() {
      const sub = store.subscribe(update);
      update.reset();
      dispatcher.dispatch({ type: 'inc', by: 2 });
      sub.dispose();
      dispatcher.dispatch({ type: 'inc', by: 2 });
      assert(update.count === 1);
    });

    it('should only notify on actions it handles', function() {
      const sub = store.subscribe(update);
      update.reset();
      dispatcher.dispatch({ type: 'inc', by: 2 });
      dispatcher.dispatch({ type: '??', by: 2 });
      assert(update.count === 1);
      sub.dispose();
    });
  });

});
