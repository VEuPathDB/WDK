import Store from '../Store';
import {
  RECORD_CLASSES_LOAD_SUCCESS
} from '../ActionType';


export default class RecordClassStore extends Store {

  init() {
    this.state = {
      recordClasses: []
    };
    this.handleAction(RECORD_CLASSES_LOAD_SUCCESS, this.setRecordClasses);
  }


  setRecordClasses({ recordClasses }) {
    this.state.recordClasses = recordClasses;
  }

}
