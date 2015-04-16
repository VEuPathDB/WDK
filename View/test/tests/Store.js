import assert from 'assert';
import Dispatcher from 'wdk/flux/Dispatcher';
import Store from 'wdk/flux/Store';

describe('Store', function() {

  it('should create an object', function() {
    var dispatcher = new Dispatcher();
    var store = new Store(dispatcher);
    assert(store !== undefined);
    dispatcher.unregister(store.dispatchToken);
  });

  it('should call derived class\'s init method', function() {

    class DerivedStore extends Store {
      init() {
        called = true;
      }
    }

    var called = false;
    var dispatcher = new Dispatcher();
    var store = new DerivedStore(dispatcher);
    assert(called);
    dispatcher.unregister(store.dispatchToken);
  });

  it('should register callbacks with dispatcher via `handleAction`', function(done) {

    class DerivedStore extends Store {
      init() {
        this.handleAction('test', this.callDone);
      }

      callDone() {
        done();
      }
    }

    var dispatcher = new Dispatcher();
    var store = new DerivedStore(dispatcher);
    dispatcher.dispatch({ type: 'test' });
    dispatcher.unregister(store.dispatchToken);
  });

  describe('asObservable', function() {
    class DerivedStore extends Store {

      init() {
        this.state = {
          value: 0
        };
        this.handleAction('test', this.updateState);
      }

      updateState(action) {
        this.state.value = action.value;
      }

    }

    var store;
    var dispatcher = new Dispatcher();

    beforeEach(function() {
      store = new DerivedStore(dispatcher);
    });

    afterEach(function() {
      dispatcher.unregister(store.dispatchToken);
    });

    it('should invoke callback immediately with current state', function() {
      var observable = store.asObservable();
      observable.subscribe(function(state) {
        assert(state.value === 0);
      }).dispose();
    });

    it('should notify on actions it handles', function() {
      var values = [];
      var observable = store.asObservable();
      var subscription = observable.subscribe(function(state) {
        values.push(state.value);
      });
      dispatcher.dispatch({
        type: 'test',
        value: 1
      });
      dispatcher.dispatch({
        type: 'test',
        value: 2
      });
      dispatcher.dispatch({
        type: 'test',
        value: 3
      });
      dispatcher.dispatch({
        type: 'test',
        value: 4
      });
      dispatcher.dispatch({
        type: 'ignore',
        value: 3
      });
      assert.deepEqual(values, [ 0, 1, 2, 3, 4 ]);
      subscription.dispose();
    });

    it('should remove callbacks when #dispose() is called', function() {
      var values = [];
      var observable = store.asObservable();
      observable.subscribe(function(state) {
        values.push(state.value);
      }).dispose();
      dispatcher.dispatch({
        type: 'test',
        value: 1
      });
      assert.deepEqual(values, [ 0 ]);
    })
  });

});
