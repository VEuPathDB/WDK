import React from 'react';

export default function createStoreMixin(store) {

  return {

    setStateFromStore() {
      this.setState(store.getState());
    },

    getInitialState() {
      return store.getState();
    },

    componentWillMount() {
      store.subscribe(this.setStateFromStore);
    },

    componentWillUnmount() {
      store.unsubscribe(this.setStateFromStore);
    }

  };

}
