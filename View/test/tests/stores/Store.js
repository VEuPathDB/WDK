import Store from 'wdk/stores/Store';
import Dispatcher from 'wdk/Dispatcher';

describe('wdk/stores/Store', function() {
   
  it('should handle basic dispatching', function() {
    var myAction = 'my action';

    var store = new Store({
      dispatchHandler(action) {
        expect(action).to.equal(myAction);
      },
      getState() {
        return 'my state';
      }
    });

    Dispatcher.dispatch(myAction);

    expect(store.getState()).to.equal('my state');
  });

  it('should throw if passed an incomplete spec', function() {
    var errors = [];

    try { var store = new Store(); }
    catch (error) { errors.push(error); }

    try { var store = new Store({ getState() {}}); }
    catch (error) { errors.push(error); }

    try { var store = new Store({ dispatchHandler() {}}); }
    catch (error) { errors.push(error); }

    expect(errors).to.have.length(3);
    errors.forEach((e) => expect(e).to.be.instanceof(TypeError));
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

    expect(messages).to.include('a');
    expect(messages).to.include('b');

  })
   
});