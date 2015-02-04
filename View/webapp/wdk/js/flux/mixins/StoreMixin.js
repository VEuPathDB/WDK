// TODO Require consumer of mixin to define getStateFromStores() and use that
// in subscribe() and getInitialState()
import _ from 'lodash';

export default function createStoreMixin(...Stores) {

  return {

    getStores() {
      return Stores.map(Store => this.props.lookup(Store), this);
    },

    setStateFromStore() {
      this.getStores()
        .forEach(store => this.setState(store.getState()));
    },

    getInitialState() {
      return this.getStores()
        .reduce((state, store) => _.assign(state, store.getState()), {});
    },

    componentWillMount() {
      this.getStores()
        .forEach(store => store.subscribe(this.setStateFromStore));
    },

    componentWillUnmount() {
      this.getStores()
        .forEach(store => store.unsubscribe(this.setStateFromStore));
    }

  };

}
