import React from 'react';

export default function createStoreMixin(...stores) {

  return {

    setStateFromStore() {
      stores
        .forEach(store => this.setState(store.getState()));
    },

    getInitialState() {
      return stores
        .reduce((state, store) => _.assign(state, store.getState()), {});
    },

    componentWillMount() {
      stores
        .forEach(store => store.subscribe(this.setStateFromStore));
    },

    componentWillUnmount() {
      stores
        .forEach(store => store.unsubscribe(this.setStateFromStore));
    }

  };

}
