import stringify from 'json-stable-stringify';
import {difference} from 'lodash';
import predicate from './Predicate';
import {preorderSeq} from './TreeUtils';
import {getTree, getPropertyValue, Ontology} from './OntologyUtils';
import {getTargetType, getRefName, getDisplayName, CategoryNode} from './CategoryUtils';
import {Question, RecordClass, Record, PrimaryKey} from './WdkModel';

interface RecordRequest {
  attributes: string[];
  tables: string[];
  primaryKey: string[];
}

interface AnswerQuestionDefinition {
  questionName: string;
  parameters?: { [key: string]: string };
  legacyFilterName?: string;
  filters?: { name: string; value: string; }[];
  viewFilters?: { name: string; value: string; }[];
  wdk_weight?: number;
}

interface AnswerFormatting {
  pagination: { offset: number; numRecords: number; };
  attributes: string[] | '__ALL_ATTRIBUTES__' | '__DISPLAYABLE_ATTRIBUTES__';
  tables: string[] | '__ALL_TABLES__' | '__DISPLAYABLE_TABLES__';
  sorting: [ { attributeName: string; direction: 'ASC' | 'DESC' } ];
  contentDisposition?: 'inline' | 'attatchment';
}

interface ServiceError extends Error {
  response: string;
  status: number;
}

interface User {
  id: number;
  firstName: string;
  middleName: string;
  lastName: string;
  organization: string;
  email: string;
}

/**
 * A helper to request resources from a Wdk REST Service.
 * @class WdkService
 */
export default class WdkService {

  _questions: Promise<Question[]>;
  _recordClasses: Promise<RecordClass[]>;
  _records: Map<string, {request: RecordRequest; response: Promise<Record>}> = new Map;
  _ontologies: Map<string, Promise<Ontology<CategoryNode>>> = new Map;

  /**
   * @param {string} serviceUrl Base url for Wdk REST Service.
   */
  constructor(private _serviceUrl: string) {  }

  getConfig() {
    return this.fetchJson('get', '/');
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
    if (this._questions == null) {
      this._questions = this.fetchJson('get', '/question?expandQuestions=true');
    }
    return this._questions;
  }

  /**
   * Get the first Question that matches `test`.
   *
   * @param {Function} test Predicate function the Question must satisfy
   * @return {Promise<Object?>}
   */
  findQuestion(test: (question: Question) => boolean) {
    return this.getQuestions().then(qs => qs.find(test));
  }

  /**
   * Get all RecordClasses defined in WDK Model.
   *
   * @return {Promise<Array<Object>>}
   */
  getRecordClasses() {
    let method = 'get';
    let url = '/record?expandRecordClasses=true&' +
      'expandAttributes=true&expandTables=true&expandTableAttributes=true';

    if (this._recordClasses == null) {
      this._recordClasses = this.fetchJson(method, url).then(
        (recordClasses: RecordClass[]) => {
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
  findRecordClass(test: (recordClass: RecordClass) => boolean) {
    return this.getRecordClasses().then(rs => rs.find(test));
  }

  getRecord(recordClassName: string, primaryKey: string[], options: {attributes?: string[]; tables?: string[];} = {}) {
    let key = makeRecordKey(recordClassName, primaryKey);
    let method = 'post';
    let url = '/record/' + recordClassName + '/instance';

    let { attributes = [], tables = [] } = options;

    // if we don't have the record, fetch whatever is requested
    if (!this._records.has(key)) {
      let request = { attributes, tables, primaryKey };
      let response = this.fetchJson(method, url, stringify(request));
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
        let newResponse = this.fetchJson(method, url, stringify(newRequest)) as Promise<Record>;

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
  getAnswer(questionDefinition: AnswerQuestionDefinition, formatting: AnswerFormatting) {
    let method = 'post';
    let url = '/answer';
    let body = stringify({ questionDefinition, formatting });
    return this.fetchJson(method, url, body);
  }

  // FIXME Replace with service call, e.g. GET /user/basket/{recordId}
  getBasketStatus(record: Record) {
    let action = 'check';
    let data = JSON.stringify([ record.id.reduce((data: {[key: string]: string;}, p: {name: string; value: string;}) => (data[p.name] = p.value, data), {}) ]);
    let method = 'get';
    let url = `/../processBasket.do?action=${action}&type=${record.recordClassName}&data=${data}`;
    return this.fetchJson(method, url).then(data => data.processed > 0);
  }

  // FIXME Replace with service call, e.g. PATCH /user/basket { add: [ {recordId} ] }
  updateBasketStatus(record: Record, status: boolean) {
    let action = status ? 'add' : 'remove';
    let data = JSON.stringify([ record.id.reduce((data: {[key: string]: string;}, p: {name: string; value: string;}) => (data[p.name] = p.value, data), {}) ]);
    let method = 'get';
    let url = `/../processBasket.do?action=${action}&type=${record.recordClassName}&data=${data}`;
    return this.fetchJson(method, url).then(() => status);
  }

  // FIXME Replace with service call, e.g. GET /user/basket/{recordId}
  getFavoritesStatus(record: Record) {
    let action = 'check';
    let data = JSON.stringify([ record.id.reduce((data: {[key: string]: string;}, p: {name: string; value: string;}) => (data[p.name] = p.value, data), {}) ]);
    let method = 'get';
    let url = `/../processFavorite.do?action=${action}&type=${record.recordClassName}&data=${data}`;
    return this.fetchJson(method, url).then(data => data.countProcessed > 0);
  }

  // FIXME Replace with service call, e.g. PATCH /user/basket { add: [ {recordId} ] }
  updateFavoritesStatus(record: Record, status: boolean) {
    let action = status ? 'add' : 'remove';
    let data = JSON.stringify([ record.id.reduce((data: {[key: string]: string;}, p: {name: string; value: string;}) => (data[p.name] = p.value, data), {}) ]);
    let method = 'get';
    let url = `/../processFavorite.do?action=${action}&type=${record.recordClassName}&data=${data}`;
    return this.fetchJson(method, url).then(() => status);
  }

  getCurrentUser() {
    return this.fetchJson('get', '/user/current');
  }

  updateCurrentUser(user: User) {
    let data = JSON.stringify(user);
    let method = 'put';
    let url = '/user/current/profile';
    return this.fetchJson(method, url, data).then(() => status);
  }

  getCurrentUserPreferences() {
    return this.fetchJson('get', '/user/current/preference');
  }

  findStep(stepId: number) {
    return this.fetchJson('get', '/step/' + stepId);
  }

  getOntology(name = '__wdk_categories__') {
    if (!this._ontologies.has(name)) {
      let ontology$ = this.fetchJson('get', '/ontology/' + name);
      let recordClasses$ = this.getRecordClasses().then(r => makeIndex(r, 'name'));
      let questions$ = this.getQuestions().then(q => makeIndex(q, 'name'));
      let entities$ = Promise.all([ recordClasses$, questions$ ])
      .then(([ recordClasses, questions ]) => ({ recordClasses, questions }));

      // FIXME this should maybe be of type CategoryOntology or Ontology<CategoryNode>
      let finalOntology$: Promise<Ontology<CategoryNode>> = ontology$
      .then(resolveWdkReferences(entities$))
      .then(pruneUnresolvedReferences)
      .then(sortOntology);

      this._ontologies.set(name, finalOntology$);
    }
    return this._ontologies.get(name);
  }

  fetchJson(method: string, url: string, body?: string): Promise<any> {
    return new Promise((resolve, reject) => {
      let xhr = new XMLHttpRequest();
      xhr.onreadystatechange = function() {
        if (xhr.readyState !== 4) return;

        if (xhr.status >= 200 && xhr.status < 300) {
          let json = xhr.status === 204 ? null : JSON.parse(xhr.response);
          resolve(json);
        }
        else {
          let msg = `Cannot ${method.toUpperCase()} ${url} (${xhr.status})`;
          let error = new Error(msg) as ServiceError;
          error.response = xhr.response;
          error.status = xhr.status;
          reject(error);
        }
      }
      xhr.open(method, this._serviceUrl + url);
      xhr.setRequestHeader('Content-Type', 'application/json');
      xhr.send(body);
    });
  }

}

function makeRecordKey(recordClassName: string, primaryKeyValues: string[]) {
  return recordClassName + ':' + stringify(primaryKeyValues);
}

/**
 * Adds the related WDK reference to each node. This function mutates the
 * ontology tree, which is ok since we are doing this before we cache the
 * result. It might be useful for this to return a new copy of the ontology
 * in the future, but for now this saves some performance.
 */
function resolveWdkReferences(entities$: Promise<{ recordClasses: Map<string, RecordClass>; questions: Map<string, Question>}>) {
  return (ontology: Ontology<CategoryNode>) => entities$.then(({ recordClasses, questions }) => {
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

function isWdkReference(node: CategoryNode) {
  let targetType = getTargetType(node);
  return targetType === 'attribute' || targetType === 'table' || targetType === 'search';
}

function isResolved(node: CategoryNode) {
  return isWdkReference(node) ? node.wdkReference != null : true;
}

function pruneUnresolvedReferences(ontology: Ontology<CategoryNode>) {
  //ontology.unprunedTree = ontology.tree;
  ontology.tree = getTree(ontology, isResolved);
  return ontology;
}

/**
 * Compare nodes based on the "sort order" property. If it is undefined,
 * compare based on displayName.
 */
function compareOntologyNodes(nodeA: CategoryNode, nodeB: CategoryNode) {
  let orderBySortNum = compareOnotologyNodesBySortNumber(nodeA, nodeB);
  return orderBySortNum === 0 ? compareOntologyNodesByDisplayName(nodeA, nodeB) : orderBySortNum;
}

/**
 * Sort onotlogy node siblings. This function mutates the tree, so should
 * only be used before caching the ontology.
 */
function sortOntology(ontology: Ontology<CategoryNode>) {
  for (let node of preorderSeq(ontology.tree)) {
    node.children.sort(compareOntologyNodes);
  }
  return ontology;
}

function compareOnotologyNodesBySortNumber(nodeA: CategoryNode, nodeB: CategoryNode) {
  let sortOrderA = getPropertyValue('display order', nodeA);
  let sortOrderB = getPropertyValue('display order', nodeB);

  if (sortOrderA && sortOrderB) {
    return sortOrderA >= sortOrderB ? 1 : -1;
  }

  if (sortOrderA) {
    return -1;
  }

  if (sortOrderB) {
    return 1;
  }

  return 0;
}

function compareOntologyNodesByDisplayName(nodeA: CategoryNode, nodeB: CategoryNode) {
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
function makeIndex(array: any[], key: string) {
  return array.reduce((index, item) => index.set(item[key], item), new Map);
}

