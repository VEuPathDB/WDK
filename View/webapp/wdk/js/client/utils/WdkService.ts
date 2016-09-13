import stringify from 'json-stable-stringify';
import {difference, indexBy} from 'lodash';
import localforage from 'localforage';
import {preorderSeq} from './TreeUtils';
import {getTree, getPropertyValue, Ontology} from './OntologyUtils';
import {getTargetType, getRefName, getDisplayName, CategoryNode} from './CategoryUtils';
import {alert} from './Platform';
import {Answer, AnswerSpec, AnswerFormatting, Question, RecordClass, Record} from './WdkModel';
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

type Dict<T> = {
  [key: string]: T;
};

interface RecordRequest {
  attributes: string[];
  tables: string[];
  primaryKey: string[];
}

export interface ServiceError extends Error {
  response: string;
  status: number;
}

export interface ServiceConfig {
  assetsUrl: string;
  authentication: {
    oauthUrl: string;
    method: 'OAUTH2' | 'USERDB';
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

type RequestOptions = {
  /** Request method */
  method: string;
  /** Path to the resource, relative to the base url */
  path: string;
  /** Query params to include with request */
  params?: { [key: string]: any; };
  /** Request body */
  body?: string;
  /** Fetch from cache, if available */
  useCache?: boolean;
  /**
   * Optional identity for cache. If useCache is true, and this is omitted, then
   * a combination of the resource path and any provided params will be used as
   * the cache key. If this is also included, this value will be appended to the
   * generated cache key.
   */
  cacheId?: string;
}

/**
 * A helper to request resources from a Wdk REST Service.
 *
 * @class WdkService
 */
export default class WdkService {

  _store: LocalForage = localforage.createInstance({
    name: 'WdkService/' + this.serviceUrl
  });
  _cache: Map<string, Promise<any>> = new Map;
  _recordCache: Map<string, {request: RecordRequest; response: Promise<Record>}> = new Map;
  _currentUserPromise: Promise<User>;
  _initialCheck: Promise<void>;
  _version: number;
  _isInvalidating = false;

  /**
   * @param {string} serviceUrl Base url for Wdk REST Service.
   */
  constructor(public serviceUrl: string) {
  }

  /**
   * Send a request to a resource of the Wdk REST Service, and returns a Promise
   * that will fulfill with the response, or reject with a ServiceError.
   *
   * @param options Options for request.
   * @param options.method The request method to use.
   * @param options.path The path of the resource, relative to the root url.
   * @param options.params? Query params to include with request.
   * @param options.body? The request body
   * @param options.useCache? Indicate if resource should be fetched from cache.
   *    This cache is invalidated whenever the REST Service application is restarted.
   *    The resource's url and any query params are used to generate a cache key.
   * @param options.cacheId? Additional string to use for cache key. This is useful
   *    for POST requests that are semantically treated as GET requests.
   * @return {Promise<Resource>}
   */
  sendRequest<Resource>(options: RequestOptions) {
    let { method, path, params, body, useCache, cacheId } = options;
    method = method.toUpperCase();
    let url = path + (params == null ? '' : '?' + queryParams(params));
    // Technically, only GET should be cache-able, but some resources treat POST
    // as GET, so we will allow it.
    if (useCache && (method === 'GET' || method === 'POST')) {
      let cacheKey = url + (cacheId == null ? '' : '__' + cacheId);
      return this._getFromCache(cacheKey,
        () => this._fetchJson<Resource>(method, url, body));
        // () => this.sendRequest(Object.assign({}, options, { useCache: false }));
    }
    return this._fetchJson<Resource>(method, url, body);
  }

  /**
   * Get the configuration for the Wdk REST Service that resides at the given base url.
   * @return {Promise<ServiceConfig>}
   */
  getConfig() {
    return this._getFromCache('config', () => this._fetchJson<ServiceConfig>('get', '/'))
  }

  getAnswerServiceUrl() {
    return this.serviceUrl + '/answer';
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
    return this._getFromCache('recordClasses', () => this._fetchJson<RecordClass[]>('get', url)
      .then(recordClasses => {
        // create indexes by name property for attributes and tables
        // this is done after recordClasses have been retrieved from the store
        // since it cannot reliably serialize Maps
        return recordClasses.map(recordClass =>
          Object.assign(recordClass, {
            attributesMap: indexBy(recordClass.attributes, 'name'),
            tablesMap: indexBy(recordClass.tables, 'name')
          }));
    }));
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

    // Get the request and response from `_recordCache` and replace them with
    // merged request and response objects. Anything awaiting the response that
    // is currently stored will still be called when it completes, regardless of
    // the progress of the response it is replaced with.
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

  /**
   * Get basket summary for all record classes
   */
  getBasketCounts() {
    return this._fetchJson<{ [recordClassName: string]: number }>('get', '/user/current/basket');
  }

  // FIXME Replace with service call, e.g. GET /user/basket/{recordId}
  getBasketStatus(record: Record) {
    let action = 'check';
    let data = JSON.stringify([ record.id.reduce((data: {[key: string]: string;}, p: {name: string; value: string;}) => (data[p.name] = p.value, data), {}) ]);
    let method = 'get';
    let url = `/../processBasket.do?action=${action}&type=${record.recordClassName}&data=${data}`;
    return this._fetchJson<{processed: number}>(method, url).then(data => data.processed > 0);
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
    return this._fetchJson<{countProcessed: number}>(method, url).then(data => data.countProcessed > 0);
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
    if (this._currentUserPromise == null) {
      this._currentUserPromise = this._fetchJson<User>('get', '/user/current');
    }
    return this._currentUserPromise;
  }

  updateCurrentUser(user: User) {
    let url = '/user/current';
    let data = JSON.stringify(user);
    return this._fetchJson<void>('put', url, data).then(() => user);
  }

  updateCurrentUserPassword(oldPassword: string, newPassword: string) {
    let url = '/user/current/password';
    let data = JSON.stringify({ oldPassword: oldPassword, newPassword: newPassword });
    return this._fetchJson<void>('put', url, data);
  }

  getCurrentUserPreferences() {
    return this._fetchJson<UserPreferences>('get', '/user/current/preference');
  }

  updateCurrentUserPreference(entries: { [key: string]: string}) {
    let url = '/user/current/preference';
    let data = JSON.stringify(entries);
    return this._fetchJson<void>('patch', url, data);
  }

  getOauthStateToken() {
    return this._fetchJson<{oauthStateToken: string}>('get', '/oauth/stateToken');
  }

  findStep(stepId: number, userId: string = "current") {
    return this._fetchJson<Step>('get', '/user/' + userId + '/step/' + stepId);
  }

  getOntology(name = '__wdk_categories__') {
    return this._getFromCache('ontology/' + name, () => {
      let ontology$ = this._fetchJson<Ontology<CategoryNode>>('get', '/ontology/' + name);
      let recordClasses$ = this.getRecordClasses().then(rs => indexBy(rs, 'name'));
      let questions$ = this.getQuestions().then(qs => indexBy(qs, 'name'));
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
      xhr.onreadystatechange = () => {
        if (xhr.readyState !== 4 || this._isInvalidating) return;

        if (xhr.status >= 200 && xhr.status < 300) {
          let json = xhr.status === 204 ? null : JSON.parse(xhr.responseText);
          resolve(json);
        }
        else if (xhr.status === 409 && xhr.response === CLIENT_OUT_OF_SYNC_TEXT) {
          this._isInvalidating = true;
          Promise.all([
            this._store.clear(),
            alert('Reload Page', 'This page is no longer valid and will be reloaded when you click "OK"')
          ])
          .then(() => location.reload());
        }
        else {
          let msg = `Cannot ${method.toUpperCase()} ${url} (${xhr.status})`;
          let error = new Error(msg) as ServiceError;
          error.response = xhr.responseText;
          error.status = xhr.status;
          reject(error);
        }
      };
      xhr.open(method.toUpperCase(), this.serviceUrl + url);
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
        return onCacheMiss().then(item => {
          return this._store.setItem(key, item)
          .catch(err => {
            console.error('Unable to store WdkService item with key `' + key + '`.', err);
            return item;
          });
        });
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
            return this._store.setItem('config', serviceConfig)
            .catch(err => {
              console.error('Unable to store WdkService item with key `config`.', err);
              return serviceConfig;
            })
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
function resolveWdkReferences(entities$: Promise<{ recordClasses: Dict<RecordClass>; questions: Dict<Question>; }>) {
  return (ontology: Ontology<CategoryNode>) => entities$.then(({ recordClasses, questions }) => {
    for (let node of preorderSeq(ontology.tree)) {
      switch (getTargetType(node)) {
        case 'attribute': {
          let attributeName = getRefName(node);
          let recordClass = recordClasses[getPropertyValue('recordClassName', node)];
          if (recordClass == null) continue;
          let wdkReference = recordClass.attributesMap[attributeName];
          Object.assign(node, { wdkReference });
          break;
        }

        case 'table': {
          let tableName = getRefName(node);
          let recordClass = recordClasses[getPropertyValue('recordClassName', node)];
          if (recordClass == null) continue;
          let wdkReference = recordClass.tablesMap[tableName];
          Object.assign(node, { wdkReference });
          break;
        }

        case 'search': {
          let questionName = getRefName(node);
          let wdkReference = questions[questionName];
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
  if (nodeA.children.length === 0 && nodeB.children.length !== 0)
    return -1;

  if (nodeB.children.length === 0 && nodeA.children.length !== 0)
    return 1;

  let orderBySortNum = compareOnotologyNodesBySortNumber(nodeA, nodeB);
  return orderBySortNum === 0 ? compareOntologyNodesByDisplayName(nodeA, nodeB) : orderBySortNum;
}

/**
 * Sort ontology node siblings. This function mutates the tree, so should
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
 * @param {Function} getKey
 * @return {Map<T>}
 */
function makeIndex<T, U>(array: U[], getKey: (u: U) => T) {
  return array.reduce((index, item) => index.set(getKey(item), item), new Map<T, U>());
}

/**
 * Convert an object into query params by traversing top-level object
 * properties and coercing values into keys.
 * @param object
 * @return {string}
 */
function queryParams(object: { [key:string]: any}): string {
  return Object.keys(object)
    .map(key => key + '=' + object[key])
    .join('&');
}
