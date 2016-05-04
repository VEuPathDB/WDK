import stringify from 'json-stable-stringify';
import difference from 'lodash/array/difference';
import predicate from './Predicate';
import {preorderSeq} from './TreeUtils';
import {getTree, getPropertyValue} from './OntologyUtils';
import {getTargetType, getRefName, getDisplayName} from './CategoryUtils';

/**
 * A helper to request resources from a Wdk REST Service.
 * @class WdkService
 */
export default class WdkService {

  /**
   * @param {string} serviceUrl Base url for Wdk REST Service.
   */
  constructor(serviceUrl) {
    this._serviceUrl = serviceUrl;

    // caches
    this._questions = null;
    this._recordClasses = null;
    this._records = new Map();
    this._ontologies = new Map();
    this._basketStatus = new Map();
  }

  getConfig() {
    return fetchJson('get', this._serviceUrl);
  }

  getAnswerServiceUrl() {
    return this._serviceUrl + '/answer';
  }

  /**
   * Get all Questions defined in WDK Model.
   *
   * @return {Promise<Array<Object>>}
   */
  getQuestions() {
    let method = 'get';
    let url = this._serviceUrl + '/question?expandQuestions=true';

    if (this._questions == null) {
      this._questions = fetchJson(method, url);
    }

    return this._questions;
  }

  /**
   * Get the first Question that matches `test`.
   *
   * @param {Function} test Predicate function the Question must satisfy
   * @return {Promise<Object?>}
   */
  findQuestion(test) {
    return this.getQuestions().then(qs => qs.find(test));
  }

  /**
   * Get all RecordClasses defined in WDK Model.
   *
   * @return {Promise<Array<Object>>}
   */
  getRecordClasses() {
    let method = 'get';
    let url = this._serviceUrl + '/record?expandRecordClasses=true&' +
      'expandAttributes=true&expandTables=true&expandTableAttributes=true';

    if (this._recordClasses == null) {
      this._recordClasses = fetchJson(method, url).then(
        recordClasses => {
          for (let recordClass of recordClasses) {
            // create indexes by name property for attributes and tables
            Object.assign(recordClass, {
              attributesMap: makeIndex(recordClass.attributes, 'name'),
              tablesMap: makeIndex(recordClass.tables, 'name')
            });
          }
          return recordClasses;
        },
        error => {
          // clear record classes; don't want partially populated list
          this._recordClasses = null;
          throw error;
        }
      );
    }

    return this._recordClasses;
  }

  /**
   * Get the first RecordClass that matches `test`.
   *
   * @param {Function} test Predicate the RecordClass must satisfy.
   * @return {Promise<Object?>}
   */
  findRecordClass(test) {
    return this.getRecordClasses().then(rs => rs.find(test));
  }

  getRecord(recordClassName, primaryKey, options = {}) {
    let key = makeRecordKey(recordClassName, primaryKey);
    let method = 'post';
    let url = this._serviceUrl + '/record/' + recordClassName + '/instance';

    let { attributes = [], tables = [] } = options;

    // if we don't have the record, fetch whatever is requested
    if (!this._records.has(key)) {
      let request = { primaryKey, attributes, tables };
      let response = fetchJson(method, url, stringify(request));
      this._records.set(key, { request, response });
    }

    else {
      let { request, response } = this._records.get(key);
      // determine which tables and attributes we need to retreive
      let reqAttributes = difference(attributes, request.attributes);
      let reqTables = difference(tables, request.tables);

      // get addition attributes and tables
      if (reqAttributes.length > 0 || reqTables.length > 0) {
        let newRequest = {
          primaryKey,
          attributes: reqAttributes,
          tables: reqTables
        };
        let newResponse = fetchJson(method, url, stringify(newRequest));

        let finalRequest = {
          primaryKey,
          attributes: request.attributes.concat(newRequest.attributes),
          tables: request.tables.concat(newRequest.tables)
        };
        // merge old record attributes and tables with new record
        let finalResponse = Promise.all([ response, newResponse ])
        .then(([record, newRecord]) => {
          return Object.assign({}, record, {
            attributes: Object.assign({}, record.attributes, newRecord.attributes),
            tables: Object.assign({}, record.tables, newRecord.tables)
          });
        });
        this._records.set(key, { request: finalRequest, response: finalResponse });
      }
    }

    return this._records.get(key).response;
  }

  /**
   * Get an answer from the answer service.
   *
   * @param {Object} questionDefinition
   * @param {string} questionDefinition.questionName
   * @param {Object} questionDefinition.parameters
   * @param {string} questionDefinition.legacyFilterName
   * @param {Array<Object>} questionDefinition.filters
   * @param {Array<Object>} questionDefinition.viewFilters
   * @param {number} questionDefinition.wdk_weight
   * @param {Object} formatting
   * @returns {Promise<Answer>}
   */
  getAnswer(questionDefinition, formatting) {
    let method = 'post';
    let url = this.getAnswerServiceUrl();
    let body = stringify({ questionDefinition, formatting });
    return fetchJson(method, url, body).then(response => {
      // we will only cache individual records
      let { recordClassName } = response.meta;
      for (let record of response.records) {
        let key = makeRecordKey(recordClassName, record.id);
        // this._records.set(key, Promise.resolve(record));
      }
      return response;
    });
  }

  // FIXME Replace with service call, e.g. GET /user/basket/{recordId}
  getBasketStatus(record) {
    let action = 'check';
    let data = JSON.stringify([ record.id.reduce((data, p) => (data[p.name] = p.value, data), {}) ]);
    let method = 'get';
    let url = `${this._serviceUrl}/../processBasket.do?action=${action}&type=${record.recordClassName}&data=${data}`;
    return fetchJson(method, url).then(data => data.processed > 0);
  }

  // FIXME Replace with service call, e.g. PATCH /user/basket { add: [ {recordId} ] }
  updateBasketStatus(record, status) {
    let action = status ? 'add' : 'remove';
    let data = JSON.stringify([ record.id.reduce((data, p) => (data[p.name] = p.value, data), {}) ]);
    let method = 'get';
    let url = `${this._serviceUrl}/../processBasket.do?action=${action}&type=${record.recordClassName}&data=${data}`;
    return fetchJson(method, url).then(() => status);
  }

  // FIXME Replace with service call, e.g. GET /user/basket/{recordId}
  getFavoritesStatus(record) {
    let action = 'check';
    let data = JSON.stringify([ record.id.reduce((data, p) => (data[p.name] = p.value, data), {}) ]);
    let method = 'get';
    let url = `${this._serviceUrl}/../processFavorite.do?action=${action}&type=${record.recordClassName}&data=${data}`;
    return fetchJson(method, url).then(data => data.countProcessed > 0);
  }

  // FIXME Replace with service call, e.g. PATCH /user/basket { add: [ {recordId} ] }
  updateFavoritesStatus(record, status) {
    let action = status ? 'add' : 'remove';
    let data = JSON.stringify([ record.id.reduce((data, p) => (data[p.name] = p.value, data), {}) ]);
    let method = 'get';
    let url = `${this._serviceUrl}/../processFavorite.do?action=${action}&type=${record.recordClassName}&data=${data}`;
    return fetchJson(method, url).then(() => status);
  }

  getCurrentUser() {
    return fetchJson('get', this._serviceUrl + '/user/current');
  }

  updateCurrentUser(user) {
    let data = JSON.stringify(user);
    let method = 'put';
    let url = this._serviceUrl + '/user/current/profile';
    return fetchJson(method, url, data).then(() => status);
  }

  getCurrentUserPreferences() {
    return fetchJson('get', this._serviceUrl + '/user/current/preference');
  }

  findStep(stepId) {
    return fetchJson('get', this._serviceUrl + '/step/' + stepId);
  }

  getOntology(name = '__wdk_categories__') {
    if (!this._ontologies.has(name)) {
      let ontology$ = fetchJson('get', this._serviceUrl + '/ontology/' + name);
      let recordClasses$ = this.getRecordClasses().then(r => makeIndex(r, 'name'));
      let questions$ = this.getQuestions().then(q => makeIndex(q, 'name'));
      let entities$ = Promise.all([ recordClasses$, questions$ ])
      .then(([ recordClasses, questions ]) => ({ recordClasses, questions }));

      let finalOntology$ = ontology$
      .then(resolveWdkReferences(entities$))
      .then(pruneUnresolvedReferences)
      .then(sortOntology);

      this._ontologies.set(name, finalOntology$);
    }
    return this._ontologies.get(name);
  }

}

function makeRecordKey(recordClassName, primaryKeyValues) {
  return recordClassName + ':' + stringify(primaryKeyValues);
}

/**
 * Adds the related WDK reference to each node. This function mutates the
 * ontology tree, which is ok since we are doing this before we cache the
 * result. It might be useful for this to return a new copy of the ontology
 * in the future, but for now this saves some performance.
 */
function resolveWdkReferences(entities$) {
  return ontology => entities$.then(({ recordClasses, questions }) => {
    for (let node of preorderSeq(ontology.tree)) {
      switch (getTargetType(node)) {
        case 'attribute': {
          let attributeName = getRefName(node);
          let recordClass = recordClasses.get(getPropertyValue('recordClassName', node));
          let wdkReference = recordClass.attributesMap.get(attributeName);
          Object.assign(node, { wdkReference });
          break;
        }

        case 'table': {
          let tableName = getRefName(node);
          let recordClass = recordClasses.get(getPropertyValue('recordClassName', node));
          let wdkReference = recordClass.tablesMap.get(tableName);
          Object.assign(node, { wdkReference });
          break;
        }

        case 'search': {
          let questionName = getRefName(node);
          let wdkReference = questions.get(questionName);
          Object.assign(node, { wdkReference });
          break;
        }
      }
    }
    return ontology;
  });
}

function isWdkReference(node) {
  let targetType = getTargetType(node);
  return targetType === 'attribute' || targetType === 'table' || targetType === 'search';
}

function isResolved(node) {
  return isWdkReference(node) ? node.wdkReference != null : true;
}

function pruneUnresolvedReferences(ontology) {
  ontology.unprunedTree = ontology.tree;
  ontology.tree = getTree(ontology, isResolved);
  return ontology;
}

/**
 * Compare nodes based on the "sort order" property. If it is undefined,
 * compare based on displayName.
 */
let compareOntologyNodes = predicate(compareOnotologyNodesBySortNumber)
  .or(compareOntologyNodesByDisplayName);

/**
 * Sort onotlogy node siblings. This function mutates the tree, so should
 * only be used before caching the ontology.
 */
function sortOntology(ontology) {
  for (let node of preorderSeq(ontology.tree)) {
    node.children.sort(compareOntologyNodes);
  }
  return ontology;
}

function compareOnotologyNodesBySortNumber(nodeA, nodeB) {
  let sortOrderA = getPropertyValue('display order', nodeA);
  let sortOrderB = getPropertyValue('display order', nodeB);

  if (sortOrderA && sortOrderB) {
    return sortOrderA - sortOrderB;
  }

  if (sortOrderA) {
    return -1;
  }

  if (sortOrderB) {
    return 1;
  }

  return 0;
}

function compareOntologyNodesByDisplayName(nodeA, nodeB) {
  // attempt to sort by displayName
  let nameA = getDisplayName(nodeA) || '';
  let nameB = getDisplayName(nodeB) || '';

  return nameA < nameB ? -1 : 1;
}

/**
 * Create a Map of `array` keyed by each element's `key` property.
 *
 * @param {Array<T>} array
 * @param {string} key
 * @return {Map<T>}
 */
function makeIndex(array, key) {
  return array.reduce((index, item) => index.set(item[key], item), new Map);
}

function fetchJson(method, url, body) {
  return new Promise(function(resolve, reject) {
    let xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function() {
      if (xhr.readyState !== 4) return;

      if (xhr.status >= 200 && xhr.status < 300) {
        let json = xhr.status === 204 ? null : JSON.parse(xhr.response);
        resolve(json, xhr.statusText, xhr);
      }
      else {
        let msg = `Cannot ${method.toUpperCase()} ${url} (${xhr.status})`;
        let error = new Error(msg);
        error.response = xhr.response;
        error.status = xhr.status;
        reject(error);
      }
    }
    xhr.open(method, url);
    xhr.setRequestHeader('Content-Type', 'application/json');
    xhr.send(body);
  });
}
