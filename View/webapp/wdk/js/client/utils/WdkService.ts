import stringify from 'json-stable-stringify';
import {difference, once} from 'lodash';
import localforage from 'localforage';
import predicate from './Predicate';
import {preorderSeq} from './TreeUtils';
import {getTree, getPropertyValue, Ontology} from './OntologyUtils';
import {getTargetType, getRefName, getDisplayName, CategoryNode} from './CategoryUtils';
import {Answer, AnswerSpec, AnswerFormatting, Question, RecordClass, Record, PrimaryKey} from './WdkModel';
import {User, UserPreferences, Step} from './WdkUser';

/**
 * Header added to service requests to indicate the version of the model
 * current stored in cache.
 */
const CLIENT_WDK_VERSION_HEADER = 'X-CLIENT-WDK-TIMESTAMP';

/**
 * Response text returned by service that indicates the version of the cached
 * model is stale, based on CLIENT_WDK_VERSION_HEADER.
 */
const CLIENT_OUT_OF_SYNC_TEXT = 'WDK-TIMESTAMP-MISMATCH';

interface RecordRequest {
  attributes: string[];
  tables: string[];
  primaryKey: string[];
}

interface ServiceError extends Error {
  response: string;
  status: number;
}

interface ServiceConfig {
  assetsUrl: string;
  authentication: {
    oauthUrl: string;
    method: string;
    oauthClientId: string;
  };
  buildNumber: string;
  categoriesOntologyName: string;
  description: string;
  displayName: string;
  projectId: string;
  releaseDate: string;
  startupTime: number;
  webAppUrl: string;
  webServiceUrl: string;
}

/**
 * A helper to request resources from a Wdk REST Service.
 *
 * @class WdkService
 */
export default class WdkService {

  _store: LocalForage = localforage.createInstance({
    name: 'WdkService/' + this._serviceUrl
  });
  _cache: Map<string, Promise<any>> = new Map;
  _recordCache: Map<string, {request: RecordRequest; response: Promise<Record>}> = new Map;
  _initialCheck: Promise<void>;
  _version: number;

  /**
   * @param {string} serviceUrl Base url for Wdk REST Service.
   */
  constructor(private _serviceUrl: string) {
  }

  getConfig() {
    return this._getFromCache('config', () => this._fetchJson<ServiceConfig>('get', '/'))
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
    return this._getFromCache('questions', () => this._fetchJson<Question[]>('get', '/question?expandQuestions=true'));
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
   */
  getRecordClasses() {
    let url = '/record?expandRecordClasses=true&expandAttributes=true&expandTables=true&expandTableAttributes=true';
    return this._getFromCache('recordClasses', () => this._fetchJson<RecordClass[]>('get', url))
    .then(recordClasses => {
      // create indexes by name property for attributes and tables
      // this is done after recordClasses have been retreived from the store
      // since it cannot reliably serialize Maps
      for (let recordClass of recordClasses) {
        Object.assign(recordClass, {
          attributesMap: makeIndex(recordClass.attributes, 'name'),
          tablesMap: makeIndex(recordClass.tables, 'name')
        });
      }
      return recordClasses;
    });
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

  /**
   * Get a record instance identified by the provided record class and primary
   * key, with the configured tables and attributes.
   *
   * The record instance will be stored in memory. Any subsequent requests will
   * be merged with the in-memory request.
   */
  getRecord(recordClassName: string, primaryKey: string[], options: {attributes?: string[]; tables?: string[];} = {}) {
    let key = makeRecordKey(recordClassName, primaryKey);
    let method = 'post';
    let url = '/record/' + recordClassName + '/instance';

    let { attributes = [], tables = [] } = options;

    // if we don't have the record, fetch whatever is requested
    if (!this._recordCache.has(key)) {
      let request = { attributes, tables, primaryKey };
      let response = this._fetchJson<Record>(method, url, stringify(request));
      this._recordCache.set(key, { request, response });
    }

    else {
      let { request, response } = this._recordCache.get(key);
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
        let newResponse = this._fetchJson<Record>(method, url, stringify(newRequest));

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
        this._recordCache.set(key, { request: finalRequest, response: finalResponse });
      }
    }

    return this._recordCache.get(key).response;
  }

  /**
   * Get an answer from the answer service.
   */
  getAnswer(questionDefinition: AnswerSpec, formatting: AnswerFormatting) {
    let method = 'post';
    let url = '/answer';
    let body = stringify({ questionDefinition, formatting });
    return this._fetchJson<Answer>(method, url, body);
  }

  // FIXME Replace with service call, e.g. GET /user/basket/{recordId}
  getBasketStatus(record: Record) {
    let action = 'check';
    let data = JSON.stringify([ record.id.reduce((data: {[key: string]: string;}, p: {name: string; value: string;}) => (data[p.name] = p.value, data), {}) ]);
    let method = 'get';
    let url = `/../processBasket.do?action=${action}&type=${record.recordClassName}&data=${data}`;
    return this._fetchJson<any>(method, url).then(data => data.processed > 0);
  }

  // FIXME Replace with service call, e.g. PATCH /user/basket { add: [ {recordId} ] }
  updateBasketStatus(record: Record, status: boolean) {
    let action = status ? 'add' : 'remove';
    let data = JSON.stringify([ record.id.reduce((data: {[key: string]: string;}, p: {name: string; value: string;}) => (data[p.name] = p.value, data), {}) ]);
    let method = 'get';
    let url = `/../processBasket.do?action=${action}&type=${record.recordClassName}&data=${data}`;
    return this._fetchJson(method, url).then(() => status);
  }

  // FIXME Replace with service call, e.g. GET /user/basket/{recordId}
  getFavoritesStatus(record: Record) {
    let action = 'check';
    let data = JSON.stringify([ record.id.reduce((data: {[key: string]: string;}, p: {name: string; value: string;}) => (data[p.name] = p.value, data), {}) ]);
    let method = 'get';
    let url = `/../processFavorite.do?action=${action}&type=${record.recordClassName}&data=${data}`;
    return this._fetchJson<any>(method, url).then(data => data.countProcessed > 0);
  }

  // FIXME Replace with service call, e.g. PATCH /user/basket { add: [ {recordId} ] }
  updateFavoritesStatus(record: Record, status: boolean) {
    let action = status ? 'add' : 'remove';
    let data = JSON.stringify([ record.id.reduce((data: {[key: string]: string;}, p: {name: string; value: string;}) => (data[p.name] = p.value, data), {}) ]);
    let method = 'get';
    let url = `/../processFavorite.do?action=${action}&type=${record.recordClassName}&data=${data}`;
    return this._fetchJson(method, url).then(() => status);
  }

  getCurrentUser() {
    return this._fetchJson<User>('get', '/user/current');
  }

  updateCurrentUser(user: User) {
    let data = JSON.stringify(user);
    let method = 'put';
    let url = '/user/current/profile';
    return this._fetchJson<void>(method, url, data).then(() => user);
  }

  getCurrentUserPreferences() {
    return this._fetchJson<UserPreferences>('get', '/user/current/preference');
  }

  findStep(stepId: number) {
    return this._fetchJson<Step>('get', '/step/' + stepId);
  }

  getOntology(name = '__wdk_categories__') {
    return this._getFromCache('ontology/' + name, () => {
      let ontology$ = this._fetchJson<Ontology<CategoryNode>>('get', '/ontology/' + name);
      let recordClasses$ = this.getRecordClasses().then(r => makeIndex(r, 'name'));
      let questions$ = this.getQuestions().then(q => makeIndex(q, 'name'));
      let entities$ = Promise.all([ recordClasses$, questions$ ])
      .then(([ recordClasses, questions ]) => ({ recordClasses, questions }));

      return ontology$
      .then(resolveWdkReferences(entities$))
      .then(pruneUnresolvedReferences)
      .then(sortOntology);
    });
  }

  _fetchJson<T>(method: string, url: string, body?: string) {
    return new Promise<T>((resolve, reject) => {
      let xhr = new XMLHttpRequest();
      xhr.onreadystatechange = function() {
        if (xhr.readyState !== 4) return;

        if (xhr.status >= 200 && xhr.status < 300) {
          let json = xhr.status === 204 ? null : JSON.parse(xhr.response);
          resolve(json);
        }
        else if (xhr.status === 409 && xhr.response === CLIENT_OUT_OF_SYNC_TEXT) {
          this._store.clear();
          alert('This page is no longer valid and will be reloaded when you click "OK"');
          location.reload();
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
      if (this._version) {
        xhr.setRequestHeader(CLIENT_WDK_VERSION_HEADER, String(this._version));
      }
      xhr.send(body);
    });
  }

  /**
   * Checks cache for item associated to key. If item is not in cache, then
   * call onCacheMiss callback and set the resolved value in the cache.
   */
  _getFromCache<T>(key: string, onCacheMiss: () => Promise<T>) {
    if (!this._cache.has(key)) {
      let cacheValue$ = this._checkStoreVersion()
      .then(() => this._store.getItem<T>(key))
      .then(storeItem => {
        if (storeItem != null) return storeItem;
        return onCacheMiss().then(item => this._store.setItem(key, item));
      });
      this._cache.set(key, cacheValue$);
    }
    return <Promise<T>>this._cache.get(key);
  }

  _checkStoreVersion() {
    if (this._initialCheck == null) {
      let serviceConfig$ = this._fetchJson<ServiceConfig>('get', '/');
      let storeConfig$ = this._store.getItem<ServiceConfig>('config');
      this._initialCheck = Promise.all([ serviceConfig$, storeConfig$ ])
      .then(([ serviceConfig, storeConfig ]) => {
        if (storeConfig == null || storeConfig.startupTime != serviceConfig.startupTime) {
          return this._store.clear().then(() => {
            return this._store.setItem('config', serviceConfig);
          });
        }
        return serviceConfig;
      })
      .then(serviceConfig => {
        this._version = serviceConfig.startupTime;
        return undefined;
      })
    }
    return this._initialCheck;
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
function resolveWdkReferences(entities$: Promise<{ recordClasses: Map<string, RecordClass>; questions: Map<string, Question>; }>) {
  return (ontology: Ontology<CategoryNode>) => entities$.then(({ recordClasses, questions }) => {
    loop: for (let node of preorderSeq(ontology.tree)) {
      switch (getTargetType(node)) {
        case 'attribute': {
          let attributeName = getRefName(node);
          let recordClass = recordClasses.get(getPropertyValue('recordClassName', node));
          if (recordClass == null) continue loop;
          let wdkReference = recordClass.attributesMap.get(attributeName);
          Object.assign(node, { wdkReference });
          break;
        }

        case 'table': {
          let tableName = getRefName(node);
          let recordClass = recordClasses.get(getPropertyValue('recordClassName', node));
          if (recordClass == null) continue loop;
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
  if (nodeA.children.length === 0)
    return -1;

  if (nodeB.children.length === 0)
    return 1;

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
    return Number(sortOrderA) >= Number(sortOrderB) ? 1 : -1;
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
