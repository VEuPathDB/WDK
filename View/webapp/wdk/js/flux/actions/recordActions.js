import map from 'lodash/collection/map';
import flattenDeep from 'lodash/array/flattenDeep';
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

  /**
   * @param {object} recordClass
   * @param {object} primaryKey
   * @param {string} categoryName
   */
  function fetchCategoryDetails(recordClass, primaryKey, categoryName) {
    let category = findCategory(recordClass.attributeCategories, categoryName);
    if (category === undefined) {
      console.warn('Could not find category %s', categoryName);
      return;
    }
    let categoryNames = [categoryName].concat(findSubCategoryNames(category));

    let attributes = recordClass.attributes
      .filter(function(a) {
        return categoryNames.includes(a.category);
      })
      .map(function(a) {
        return a.name;
      });

    let tables = recordClass.tables
      .filter(function(t) {
        return categoryNames.includes(t.category);
      })
      .map(function(t) {
        return t.name;
      });

    let resourcePath = '/record/' + recordClass.fullName + '/get';
    let requestBody = {
      recordInstanceSpecification: { primaryKey, attributes, tables }
    };

    service.postResource(resourcePath, requestBody)
      .then(function(data) {
        dispatcher.dispatch({
          type: RECORD_DETAILS_ADDED,
          record: data.record,
          meta: data.meta
        });
      })
      .catch(function(error) {
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
    fetchCategoryDetails,
    toggleCategory
  };
}

// Search categories for the category with name == categoryName
function findCategory(categories = [], categoryName) {
  let category;
  for (category of categories) {
    if (category.name === categoryName) {
      break;
    }
    category = findCategory(category.subCategories, categoryName);
    if (category) {
      break;
    }
  }
  return category;
}

// Return a flat array of category names that are children of categoryName
function findSubCategoryNames(category) {
  return flattenDeep(map(category.subCategories, function(subCategory) {
    return [subCategory.name].concat(findSubCategoryNames(subCategory));
  }));
}

export default { createActions };
