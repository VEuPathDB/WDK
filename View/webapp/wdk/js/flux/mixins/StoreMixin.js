// TODO Require consumer of mixin to define getStateFromStores() and use that
// in subscribe() and getInitialState()
import _ from 'lodash';

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
