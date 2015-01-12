import assert from 'assert';
import Store from 'wdk/flux/stores/Store';
import Dispatcher from 'wdk/flux/Dispatcher';

describe('wdk/stores/Store', function() {
   
  it('should handle basic dispatching', function() {
    var myAction = 'my action';

    var store = new Store({
      dispatchHandler(action) {
        assert(action == myAction)
      },
      getState() {
        return 'my state';
      }
    });

    Dispatcher.dispatch(myAction);

    assert(store.getState() == 'my state');
  });

  it('should throw if passed an incomplete spec', function() {
    var dispatchHandler = () => {};
    var getState = () => {};

    assert.throws(() => new Store(), TypeError);
    assert.throws(() => new Store({ dispatchHandler }), TypeError);
    assert.throws(() => new Store({ getState }), TypeError);
    assert.doesNotThrow(() => new Store({ getState, dispatchHandler }));
  });

  it('preserves class semantics', function() {
    var messages = [];
    var noop = () => {};

    class MyStore extends Store {
      subscribe(callback, message) {
        super.subscribe(callback);
        messages.push(message)
      }
    }

    var store = new MyStore({
      dispatchHandler: noop,
      getState: noop
    });

    store.subscribe(noop, 'a');
    store.subscribe(noop, 'b');

    assert(messages.indexOf('a') > -1);
    assert(messages.indexOf('b') > -1);

  })
   
});
