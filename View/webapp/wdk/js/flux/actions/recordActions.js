import {
  APP_ERROR,
  RECORD_DETAILS_ADDED,
  RECORD_TOGGLE_CATEGORY
} from '../constants/actionTypes';

function createActions({ dispatcher, service, getStore }) {
  /**
   * @param {string} recordClass
   * @param {object} spec
   * @param {object} spec.primaryKey
   * @param {array}  spec.attributes
   * @param {array}  spec.tables
   */
  function fetchRecordDetails(recordClass, recordSpec) {
    let reqBody = { recordInstanceSpecification: recordSpec };
    service.postResource(`/record/${recordClass}/get`, reqBody).then(function(data) {
      let { record, meta } = data;
      dispatcher.dispatch({ type: RECORD_DETAILS_ADDED, meta, record });
    }).catch(function(error) {
      dispatcher.dispatch({ type: APP_ERROR, error });
    });
  }

  function toggleCategory({ recordClass, category, isVisible }) {
    dispatcher.dispatch({
      type: RECORD_TOGGLE_CATEGORY,
      recordClass: recordClass.fullName,
      name: category.name,
      isVisible
    });
  }

  return {
    fetchRecordDetails,
    toggleCategory
  };
}

export default { createActions };
