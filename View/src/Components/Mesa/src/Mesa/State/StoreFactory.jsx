import { createStore } from 'redux';
import ReducerFactory from 'Mesa/State/Reducers';

const StoreFactory = {
  create ({ options, columns, rows }) {
    let reducer = ReducerFactory({ options, columns, rows });
    return createStore(reducer);
  }
};

export default StoreFactory;
