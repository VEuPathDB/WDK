import map from 'lodash/collection/map';
import flattenDeep from 'lodash/array/flattenDeep';
import {
  APP_ERROR,
  RECORD_DETAILS_ADDED,
  RECORD_CATEGORY_VISIBILITY_TOGGLED,
  RECORD_CATEGORY_COLLAPSED_TOGGLED
} from '../constants/actionTypes';
import CommonActions from './commonActions';

function fetchRecord(recordClassName, primaryKey) {
  primaryKey = Object.keys(primaryKey).map(function(name) {
    return { name, value: primaryKey[name] };
  });
  return function(dispatch, state, { restAPI }) {
    return restAPI.getResource('/record/' + recordClassName)
    .then(function(recordClass) {
      let attributes = recordClass.attributes.map(a => a.name);
      let tables = recordClass.tables.map(t => t.name);
      let recordSpec = {
        primaryKey,
        attributes,
        tables
      };
      let reqBody = { recordInstanceSpecification: recordSpec };
      return restAPI.postResource(`/record/${recordClass.fullName}/get`, reqBody).then(function(data) {
        let { record, meta } = data;
        dispatch({ type: RECORD_DETAILS_ADDED, meta, record });
      });
    });
  };
}

/**
 * @param {string} recordClass
 * @param {object} spec
 * @param {object} spec.primaryKey
 * @param {array}  spec.attributes
 * @param {array}  spec.tables
 */
function fetchRecordDetails(recordClass, recordSpec) {
  return function(dispatch, state, { restAPI }) {
    // REST service wants this as an array of objects... should we change this?
    recordSpec.primaryKey = Object.keys(recordSpec.primaryKey).map(function(name) {
      return { name, value: recordSpec.primaryKey[name] };
    });
    let reqBody = { recordInstanceSpecification: recordSpec };
    return restAPI.postResource(`/record/${recordClass}/get`, reqBody).then(function(data) {
      let { record, meta } = data;
      dispatch({ type: RECORD_DETAILS_ADDED, meta, record });
    }).catch(function(error) {
      dispatch({ type: APP_ERROR, error });
    });
  };
}

/**
 * @param {object} recordClass
 * @param {object} primaryKey
 * @param {string} categoryName
 */
function fetchCategoryDetails(recordClass, primaryKey, categoryName) {
  return function(dispatch, state, { restAPI }) {
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

    return restAPI.postResource(resourcePath, requestBody)
      .then(function(data) {
        dispatch({
          type: RECORD_DETAILS_ADDED,
          record: data.record,
          meta: data.meta
        });
      })
      .catch(function(error) {
        dispatch({ type: APP_ERROR, error });
      });
  }
}

function toggleCategoryVisibility({ recordClass, category, isVisible }) {
  return {
    type: RECORD_CATEGORY_VISIBILITY_TOGGLED,
    recordClass: recordClass.fullName,
    name: category.name,
    isVisible
  };
}

function toggleCategoryCollapsed({ recordClass, category, isCollapsed }) {
  return {
    type: RECORD_CATEGORY_COLLAPSED_TOGGLED,
    recordClass: recordClass.fullName,
    name: category.name,
    isCollapsed
  }
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

export default {
  fetchRecord,
  fetchRecordDetails,
  fetchCategoryDetails,
  toggleCategoryVisibility,
  toggleCategoryCollapsed
};
