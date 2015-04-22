import Store from '../Store';
import {
  RecordClassesAdded
} from '../ActionType';


export default class RecordClassStore extends Store {

  init() {
    this.state = {
      recordClasses: []
    };
    this.handleAction(RecordClassesAdded, this.setRecordClasses);
  }


  setRecordClasses({ recordClasses }) {
    this.state.recordClasses = recordClasses;
  }

}
