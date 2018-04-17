import { createStore } from 'redux';
import ReducerFactory from 'Mesa/State/Reducers';

const StoreFactory = {
  create (base) {
    let reducer = ReducerFactory(base);
    return createStore(reducer);
  }
};

export default StoreFactory;
