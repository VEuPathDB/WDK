import { ReduceStore } from 'flux/utils';

export default class WdkStore extends ReduceStore {

  constructor(dispatcher, storeContainer) {
    super(dispatcher);
    this._storeContainer = storeContainer;
  }

  getInitialState() {
    return null;
  }

  reduce() {
    return null;
  }

}
