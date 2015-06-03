import {
  AppError,
  RecordDetailsReceived
} from '../ActionType';

function createActions({ dispatcher, service }) {
  return {
    /**
     * @param {string} recordClass
     * @param {object} spec
     * @param {object} spec.primaryKey
     * @param {array}  spec.attributes
     * @param {array}  spec.tables
     */
    fetchRecordDetails(recordClass, recordSpec) {
      let reqBody = { recordInstanceSpecification: recordSpec };
      service.postResource(`/record/${recordClass}/get`, reqBody).then(function(data) {
        let { record, meta } = data;
        dispatcher.dispatch(RecordDetailsReceived({ meta, record }));
      }).catch(function(error) {
        dispatcher.dispatch(AppError({ error }));
      });
    }
  };
}

export default { createActions };
