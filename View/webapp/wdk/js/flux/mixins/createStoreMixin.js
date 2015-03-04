import React from 'react';

export default function createStoreMixin(...storeNames) {

  return {

    contextTypes: {
      getStore: React.PropTypes.func.isRequired
    },

    getInitialState() {
      if (typeof this.getStateFromStores !== 'function') {
        throw new TypeError(
          `A getStateFromStores method must be provided when using the store mixin.
          See the component ${this.displayName}.`
        );
      }
      return this.getStateFromStores(this._getStores());
    },

    componentDidMount() {
      for (var storeName in this._getStores()) {
        this._getStores()[storeName].subscribe(this._setStateFromStores);
      }
    },

    componentWillUnmount() {
      for (var storeName in this._getStores()) {
        this._getStores()[storeName].unsubscribe(this._setStateFromStores);
      }
    },

    _getStores() {
      return storeNames.reduce((stores, storeName) => {
        stores[storeName] = this.context.getStore(storeName);
        return stores;
      }, {});
    },

    _setStateFromStores() {
      this.setState(this.getStateFromStores(this._getStores()));
    }

  };

}
