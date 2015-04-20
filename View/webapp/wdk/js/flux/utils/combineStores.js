/**
 * Create a combined subscription that notifies when any store in stores is
 * updated. The first notification will happen once all stores have notified
 * at least once. Since stores notify immediately, this will happen within the
 * current frame. A disposable object will be returned.
 *
 * This is very much inspired by Rx.Observable.combineLatest. See
 * https://github.com/Reactive-Extensions/RxJS/blob/master/doc/api/core/operators/combinelatest.md.
 *
 * Example
 *
 *    this.subscription = combineStores(
 *      store1,
 *      store2,
 *      function(s1, s2) {
 *        component.setState({
 *          item: s1.item,
 *          otherItem: s2.otherItem
 *        });
 *      }
 *    );
 */
export default function combineStores( /* ...stores, onNext */ ) {
  const stores = Array.from(arguments);
  const onNext = stores.pop();

  let allDone = false;
  const combinedState = stores.map(() => false);
  const subscriptions = stores.map(function(store, index) {
    return store.subscribe(function(state) {
      combinedState[index] = state;
      if (!allDone) allDone = !combinedState.some(s => s === false);
      if (allDone) onNext(...combinedState);
    });
  });
  return createDisposable(subscriptions);
}

function createDisposable(subscriptions) {
  return {
    dispose() {
      subscriptions.forEach(function(s) {
        s.dispose();
      });
    }
  };
}
