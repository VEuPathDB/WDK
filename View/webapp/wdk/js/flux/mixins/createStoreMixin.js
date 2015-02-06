import React from 'react';

export default function createStoreMixin(...storeNames) {

  // cache of store instances
  var _stores;

  // Callback function for stores. We will define this in getInitialState()
  // since it need to be bound to component instance.
  var _setStateFromStores;

  return {

    contextTypes: {
      getStore: React.PropTypes.func.isRequired
    },

    getInitialState() {
      // fill cache of store instances
      _stores = storeNames.reduce((stores, storeName) => {
        stores[storeName] = this.context.getStore(storeName);
        return stores;
      }, {});

      // Create callback function which will call setState on the component
      // this mixin is added to. This will be used with the .subscribe() and
      // .unsubscribe() methods of the stores.
      _setStateFromStores = () => {
        this.setState(this.getStateFromStores(_stores));
      };

      return this.getStateFromStores(_stores);
    },

    componentDidMount() {
      for (var storeName in _stores) {
        _stores[storeName].subscribe(_setStateFromStores);
      }
    },

    componentWillUnmount() {
      for (var storeName in _stores) {
        _stores[storeName].unsubscribe(_setStateFromStores);
      }
    }

  };

}
