import stringify from 'json-stable-stringify';
import { difference, keyBy, memoize } from 'lodash';
import localforage from 'localforage';
import { Ontology } from 'Utils/OntologyUtils';
import {
  CategoryTreeNode,
  pruneUnknownPaths,
  sortOntology,
  resolveWdkReferences
} from './CategoryUtils';
import { alert } from 'Utils/Platform';
import {
  Answer,
  AnswerSpec,
  AnswerFormatting,
  NewStepSpec,
  PrimaryKey,
  Question,
  Parameter,
  ParameterValue,
  ParameterValues,
  RecordClass,
  RecordInstance,
  UserDataset,
  UserDatasetMeta,
  OntologyTermSummary,
  Favorite
} from './WdkModel';
import { User, PreferenceScope, UserPreferences, Step, UserWithPrefs } from 'Utils/WdkUser';
import { pendingPromise, synchronized } from 'Utils/PromiseUtils';
import { submitAsForm } from 'Utils/FormSubmitter';

/**
 * Header added to service requests to indicate the version of the model
 * current stored in cache.
 */
const CLIENT_WDK_VERSION_HEADER = 'x-client-wdk-timestamp';

/**
 * Response text returned by service that indicates the version of the cached
 * model is stale, based on CLIENT_WDK_VERSION_HEADER.
 */
const CLIENT_OUT_OF_SYNC_TEXT = 'WDK-TIMESTAMP-MISMATCH';

interface RecordRequest {
  attributes: string[];
  tables: string[];
  primaryKey: PrimaryKey;
}

export interface AnswerRequest {
  answerSpec: AnswerSpec;
  formatting?: {
    format?: string;
    formatConfig?: any;
  }
}

export class ServiceError extends Error {
  constructor(
    message: string,
    public response: string,
    public status: number
  ) {
    super(message);
  }
}

export interface ClientError {
  name: string;
  message: string;
  stack?: string;
  componentStack?: string;
}

export interface ServiceConfig {
  authentication: {
    method: 'OAUTH2' | 'USERDB';
    oauthUrl: string;
    oauthClientUrl: string;
    oauthClientId: string;
  };
  buildNumber: string;
  categoriesOntologyName: string;
  description: string;
  displayName: string;
  projectId: string;
  releaseDate: string;
  startupTime: number;
}

export type TryLoginResponse = {
  success: boolean;
  message: string;
  redirectUrl: string;
}

type BasketStatusResponse = Array<boolean>;

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

  private static _instances: Map<string, WdkService> = new Map;

  static getInstance(serviceUrl: string): WdkService {
    if (!WdkService._instances.has(serviceUrl)) {
      WdkService._instances.set(serviceUrl, new WdkService(serviceUrl));
    }
    return WdkService._instances.get(serviceUrl) as WdkService;
  }

  private _store: LocalForage = localforage.createInstance({
    name: 'WdkService/' + this.serviceUrl
  });
  private _cache: Map<string, Promise<any>> = new Map;
  private _recordCache: Map<string, {request: RecordRequest; response: Promise<RecordInstance>}> = new Map;
  private _preferences: Promise<UserPreferences> | undefined;
  private _currentUserPromise: Promise<User> | undefined;
  private _initialCheck: Promise<void> | undefined;
  private _version: number | undefined;
  private _isInvalidating = false;

  /**
   * @param {string} serviceUrl Base url for Wdk REST Service.
   */
  private constructor(private serviceUrl: string) {
    this.getOntology = memoize(this.getOntology.bind(this));
    this.updateCurrentUserPreference = synchronized(this.updateCurrentUserPreference);
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
  private sendRequest<Resource>(options: RequestOptions) {
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

  getAnswerServiceEndpoint() {
    return this.serviceUrl + '/answer';
  }

  tryLogin(email: string, password: string, redirectUrl: string) {
    return this._fetchJson<TryLoginResponse>('post', '/login',
      JSON.stringify({ email, password, redirectUrl }));
  }

  submitError(error: ClientError) {
    return this._fetchJson<never>('post', '/client-errors', JSON.stringify(error));
  }

  /**
   * Get all Questions defined in WDK Model.
   *
   * @return {Promise<Array<Object>>}
   */
  getQuestions() {
    return this._getFromCache('questions', () => this._fetchJson<Question[]>('get', '/questions?expandQuestions=true'));
  }

  /**
   * Get the first Question that matches `test`.
   *
   * @param {Function} test Predicate function the Question must satisfy
   * @return {Promise<Object?>}
   */
  findQuestion(test: (question: Question) => boolean) {
    return this.getQuestions().then(qs => {
      let question = qs.find(test)
      if (question == null) {
        throw new ServiceError("Could not find question.", "Not found", 404);
      }
      return question;
    });
  }

  /**
   * Fetch question with default param values/vocabularies (may get from cache if already present)
   */
  getQuestionAndParameters(identifier: string) {
    let url = `/questions/${identifier}?expandParams=true`;
    return this._getFromCache(url, () => this._fetchJson<Question>('get', url));
  }

  /**
   * Fetch question information (e.g. vocabularies) given the passed param values; never cached
   */
  getQuestionGivenParameters(identifier: string, paramValues: ParameterValues) {
    return this._fetchJson<Question>('post',`/questions/${identifier}`,
      JSON.stringify({ contextParamValues: paramValues }));
  }

  getQuestionParamValues(identifier: string, paramName: string, paramValue: ParameterValue, paramValues: ParameterValues) {
    return this._fetchJson<Parameter[]>(
      'post',
      `/questions/${identifier}/refreshed-dependent-params`,
      JSON.stringify({
        changedParam: { name: paramName, value: paramValue },
        contextParamValues: paramValues
      })
    );
  }

  getOntologyTermSummary(identifier: string, paramName: string, filters: any, ontologyId: string, paramValues: ParameterValues) {
    return this._fetchJson<OntologyTermSummary>(
      'post',
      `/questions/${identifier}/${paramName}/ontology-term-summary`,
      JSON.stringify({
        ontologyId,
        filters,
        contextParamValues: paramValues
      })
    );
  }

  getFilterParamSummaryCounts(identifier: string, paramName: string, filters: any, paramValues: ParameterValues) {
    return this._fetchJson<{filtered: number, unfiltered: number}>(
      'post',
      `/questions/${identifier}/${paramName}/summary-counts`,
      JSON.stringify({
        filters,
        contextParamValues: paramValues
      })
    );
  }

  /**
   * Get all RecordClasses defined in WDK Model.
   */
  getRecordClasses() {
    let url = '/records?expandRecordClasses=true&expandAttributes=true&expandTables=true&expandTableAttributes=true';
    return this._getFromCache('recordClasses', () => this._fetchJson<RecordClass[]>('get', url)
      .then(recordClasses => {
        // create indexes by name property for attributes and tables
        // this is done after recordClasses have been retrieved from the store
        // since it cannot reliably serialize Maps
        return recordClasses.map(recordClass =>
          Object.assign(recordClass, {
            attributesMap: keyBy(recordClass.attributes, 'name'),
            tablesMap: keyBy(recordClass.tables, 'name')
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
    return this.getRecordClasses().then(rs => {
      let record = rs.find(test);
      if (record == null) {
        throw new ServiceError("Could not find record.", "Not found", 404);
      }
      return record;
    });
  }

  /**
   * Get a record instance identified by the provided record class and primary
   * key, with the configured tables and attributes.
   *
   * The record instance will be stored in memory. Any subsequent requests will
   * be merged with the in-memory request.
   *
   * XXX Use _getFromCache with key of "recordInstance" so the most recent record is saved??
   */
  getRecord(recordClassName: string, primaryKey: PrimaryKey, options: {attributes?: string[]; tables?: string[];} = {}) {
    let cacheKey = recordClassName + ':' + stringify(primaryKey);
    let method = 'post';
    let url = '/records/' + recordClassName + '/instance';

    let { attributes = [], tables = [] } = options;
    let cacheEntry = this._recordCache.get(cacheKey);

    // if we don't have the record, fetch whatever is requested
    if (cacheEntry == null) {
      let request = { attributes, tables, primaryKey };
      let response = this._fetchJson<RecordInstance>(method, url, stringify(request));
      cacheEntry = { request, response };
      this._recordCache.set(cacheKey, cacheEntry);
    }

    // Get the request and response from `_recordCache` and replace them with
    // merged request and response objects. Anything awaiting the response that
    // is currently stored will still be called when it completes, regardless of
    // the progress of the response it is replaced with.
    else {
      let { request, response } = cacheEntry;
      // determine which tables and attributes we need to retrieve
      let reqAttributes = difference(attributes, request.attributes);
      let reqTables = difference(tables, request.tables);

      // get addition attributes and tables
      if (reqAttributes.length > 0 || reqTables.length > 0) {
        let newRequest = {
          primaryKey,
          attributes: reqAttributes,
          tables: reqTables
        };
        let newResponse = this._fetchJson<RecordInstance>(method, url, stringify(newRequest));

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
            tables: Object.assign({}, record.tables, newRecord.tables),
            tableErrors: difference(record.tableErrors, reqTables).concat(newRecord.tableErrors)
          });
        });
        cacheEntry = { request: finalRequest, response: finalResponse };
        this._recordCache.set(cacheKey, cacheEntry);
      }
    }

    return cacheEntry.response;
  }

  /**
   * Get an answer from the answer service.
   */
  getAnswer(answerSpec: AnswerSpec, formatting: AnswerFormatting) {
    let method = 'post';
    let url = '/answer';
    let body: AnswerRequest = { answerSpec, formatting };
    return this._fetchJson<Answer>(method, url, stringify(body));
  }

  /**
   * Get basket summary for all record classes
   */
  getBasketCounts() {
    return this._fetchJson<{ [recordClassName: string]: number }>('get', '/users/current/baskets');
  }

  getBasketStatus(recordClassName: string, records: Array<RecordInstance>): Promise<BasketStatusResponse> {
    let data = JSON.stringify(records.map(record => record.id));
    let url = `/users/current/baskets/${recordClassName}/query`;
    return this._fetchJson<BasketStatusResponse>('post', url, data);
  }

  updateBasketStatus(status: boolean, recordClassName: string, records: Array<RecordInstance>): Promise<never> {
    let action = status ? 'add' : 'remove';
    let data = JSON.stringify({ [action]: records.map(record => record.id) });
    let url = `/users/current/baskets/${recordClassName}`;
    return this._fetchJson<never>('patch', url, data);
  }

  /**
   * Gets favorite ID of a single record, or undefined if record is not a
   * favorite of the current user.  Thus can be used to check whether a record
   * is a favorite of the current user.
   *
   * @param record Record instance to search for
   */
  getFavoriteId (record: RecordInstance) {
      let data = [{
        recordClassName: record.recordClassName,
        primaryKey: record.id
      }];
      let url = '/users/current/favorites/query';
      return this
        ._fetchJson<Array<number>>('post', url, JSON.stringify(data))
        .then(data => data.length ? data[0] : undefined);
    }

  /**
   * Adds the passed record as a favorite of the current user and returns ID
   * of the new favorite.
   *
   * @param record Record to add as a favorite
   */
  addFavorite (record: RecordInstance) {
    const { recordClassName, id } = record;
    const favorite = { recordClassName, primaryKey: id };
    const url = '/users/current/favorites';
    return this
      ._fetchJson<Favorite>('post', url, JSON.stringify(favorite))
      .then(data => data.id);
  }

  /**
   * Deletes the favorite with the passed ID and returns a promise with the
   * "new ID" i.e. undefined since favorite no longer exists
   *
   * @param id id of favorite to delete
   */
  deleteFavorite (id: number) {
    let url = '/users/current/favorites/' + id;
    return this
      ._fetchJson<void>('delete', url)
      .then(() => undefined);
  }

  /**
   * Returns an array of the current user's favorites
   */
  getCurrentFavorites () {
    return this._fetchJson<Favorite[]>('get', '/users/current/favorites');
  }

  /**
   * Saves the note and group on the passed favorite to the server
   *
   * @param favorite
   */
  saveFavorite (favorite: Favorite) {
    let url = '/users/current/favorites/' + favorite.id;
    favorite.group = favorite.group ? favorite.group : '';
    favorite.description = favorite.description ? favorite.description : '';
    return this._fetchJson<void>('put', url, JSON.stringify(favorite));
  }

  deleteFavorites (ids: Array<number>) {
    return this.runBulkFavoritesAction('delete', ids);
  }

  undeleteFavorites (ids: Array<number>) {
    return this.runBulkFavoritesAction('undelete', ids);
  }

  private runBulkFavoritesAction (operation: string, ids: Array<number>) {
    let url = '/users/current/favorites';
    let base = { delete: [], undelete: [] };
    let data = Object.assign({}, base, { [operation]: ids });
    return this._fetchJson<void>('patch', url, JSON.stringify(data));
  }

  getCurrentUser() {
    if (this._currentUserPromise == null) {
      this._currentUserPromise = this._fetchJson<User>('get', '/users/current');
    }
    return this._currentUserPromise;
  }

  createNewUser(userWithPrefs: UserWithPrefs) {
    return this._fetchJson<User>('post', '/users', JSON.stringify(userWithPrefs));
  }

  updateCurrentUser(user: User) {
    let url = '/users/current';
    let data = JSON.stringify(user);
    return this._currentUserPromise = this._fetchJson<void>('put', url, data).then(() => user);
  }

  updateCurrentUserPassword(oldPassword: string, newPassword: string) {
    let url = '/users/current/password';
    let data = JSON.stringify({ oldPassword: oldPassword, newPassword: newPassword });
    return this._fetchJson<void>('put', url, data);
  }

  resetUserPassword(email: string) {
    let url = '/user-password-reset';
    let data = JSON.stringify({ email });
    return this._fetchJson<void>('post', url, data);
  }

  getCurrentUserPreferences() : Promise<UserPreferences> {
    if (!this._preferences) {
      this._preferences = this._fetchJson<UserPreferences>('get', '/users/current/preferences');
    }
    return this._preferences;
  }

  updateCurrentUserPreference(scope: PreferenceScope, key: string, value: string) : Promise<UserPreferences> {
    let entries = { [scope]: { [key]: value }};
    let url = '/users/current/preferences';
    let data = JSON.stringify(entries);
    return this._fetchJson<void>('patch', url, data)
      .then(() => this.getCurrentUserPreferences())
      .then(preferences => {
        // merge with cached preferences only if patch succeeds
        return this._preferences = Promise.resolve({ ...preferences, ...entries });
      });
  }

  updateCurrentUserPreferences(entries: UserPreferences) : Promise<UserPreferences> {
    let url = '/users/current/preferences';
    let data = JSON.stringify(entries);
    return this._fetchJson<void>('put', url, data).then(() => {
      // merge with cached preferences only if patch succeeds
      return this._preferences = Promise.resolve(entries);
    });
  }

  getCurrentUserDatasets() {
    return this._fetchJson<UserDataset[]>('get', '/users/current/user-datasets?expandDetails=true');
  }

  getUserDataset(id: number) {
    return this._fetchJson<UserDataset>('get', `/users/current/user-datasets/${id}`)
  }

  updateUserDataset(id: number, meta: UserDatasetMeta) {
    return this._fetchJson<void>('put', `/users/current/user-datasets/${id}/meta`, JSON.stringify(meta));
  }

  getOauthStateToken() {
    return this._fetchJson<{oauthStateToken: string}>('get', '/oauth/state-token');
  }

  findStep(stepId: number, userId: string = "current") {
    return this._fetchJson<Step>('get', `/users/${userId}/steps/${stepId}`);
  }

  createStep(newStepSpec: NewStepSpec, userId: string = "current") {
    return this._fetchJson<Step>('post', `/users/${userId}/steps`, JSON.stringify(newStepSpec));
  }

  getOntology(name = '__wdk_categories__') {
    let recordClasses$ = this.getRecordClasses().then(rs => keyBy(rs, 'name'));
    let questions$ = this.getQuestions().then(qs => keyBy(qs, 'name'));
    let ontology$ = this._getFromCache('ontologies/' + name, () => {
      let rawOntology$ = this._fetchJson<Ontology<CategoryTreeNode>>('get', `/ontologies/${name}`);
      return Promise.all([ recordClasses$, questions$, rawOntology$ ])
      .then(([ recordClasses, questions, rawOntology ]) => {
        return sortOntology(recordClasses, questions,
          pruneUnknownPaths(recordClasses, questions, rawOntology));
      })
    });
    return Promise.all([ recordClasses$, questions$, ontology$ ])
      .then(([ recordClasses, questions, ontology ]) => {
        return resolveWdkReferences(recordClasses, questions, ontology);
      });
  }

  downloadAnswer(answerRequest: AnswerRequest, target = '_blank') {
    // a submission must trigger a form download, meaning we must POST the form
    submitAsForm({
      method: 'post',
      action: this.getAnswerServiceEndpoint(),
      target: target,
      inputs: {
        data: JSON.stringify(answerRequest)
      }
    });
  }

  private _fetchJson<T>(method: string, url: string, body?: string) {
    return fetch(this.serviceUrl + url, {
      method: method.toUpperCase(),
      body: body,
      credentials: 'include',
      headers: new Headers(Object.assign({
        'Content-Type': 'application/json'
      }, this._version && {
        [CLIENT_WDK_VERSION_HEADER]: this._version
      }))
    }).then(response => {
      if (this._isInvalidating) {
        return pendingPromise as Promise<T>;
      }

      if (response.ok) {
        return response.status === 204 ? undefined : response.json();
      }

      return response.text().then(text => {
        if (response.status === 409 && text === CLIENT_OUT_OF_SYNC_TEXT) {
          this._isInvalidating = true;
          Promise.all([
            this._store.clear(),
            alert('Reload Page', 'This page is no longer valid and will be reloaded when you click "OK"')
          ])
          .then(() => location.reload(true));
          return pendingPromise as Promise<T>;
        }

        throw new ServiceError(
          `Cannot ${method.toUpperCase()} ${url} (${response.status})`,
          text,
          response.status
        );
      });
    }) as Promise<T>
  }

  /**
   * Checks cache for item associated to key. If item is not in cache, then
   * call onCacheMiss callback and set the resolved value in the cache.
   */
  private _getFromCache<T>(key: string, onCacheMiss: () => Promise<T>) {
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

  private _checkStoreVersion() {
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
