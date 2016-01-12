import stringify from 'json-stable-stringify';
import difference from 'lodash/array/difference';

export default class WdkService {

  constructor(serviceUrl) {
    this._serviceUrl = serviceUrl;

    // caches
    this._questions = null;
    this._recordClasses = null;
    this._records = new Map();
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
            recordClass.categories.push(
              { name: undefined, displayName: 'Uncategorized' }
            )
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

  getRecord(recordClassName, primaryKeyValues, options = {}) {
    let primaryKeyString = primaryKeyValues.join('/');
    let key = recordClassName + ':' + primaryKeyString;
    let method = 'post';
    let url = this._serviceUrl + '/record/' + recordClassName + '/instance';

    let { attributes = [], tables = [] } = options;

    // if we don't have the record, fetch whatever is requested
    if (!this._records.has(key)) {
      let body = stringify({ primaryKeyValues, attributes, tables });
      this._records.set(key, fetchJson(method, url, body));
    }

    else {
      // determine which tables and attributes we need to retreive
      this._records.set(key, this._records.get(key).then(record => {
        let reqAttributes = difference(attributes, Object.keys(record.attributes));
        let reqTables = difference(tables, Object.keys(record.tables));

        // get addition attributes and tables
        if (reqAttributes.length > 0 || reqTables.length > 0) {
          let body = stringify({
            primaryKeyValues,
            attributes: reqAttributes,
            tables: reqTables
          });

          // merge old record attributes and tables with new record
          return fetchJson(method, url, body).then(record => {
            Object.assign(record.attributes, record.attributes);
            Object.assign(record.tables, record.tables);
            return record;
          });
        }

        return record;
      }));
    }

    return this._records.get(key);
  }

  getAnswer(questionDefinition, formatting) {
    let method = 'post';
    let url = this._serviceUrl + '/answer';
    let body = stringify({ questionDefinition, formatting });
    return fetchJson(method, url, body).then(response => {
      // we will only cache individual records
      let recordClassName = response.meta.recordClass;
      for (let record of response.records) {
        let key = recordClassName + ':' + stringify(record.id);
        this._records.set(key, Promise.resolve(record));
      }
      return response;
    });
  }

}


function fetchJson(method, url, body) {
  return new Promise(function(resolve, reject) {
    let xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function() {
      if (xhr.readyState !== 4) return;

      if (xhr.status >= 200 && xhr.status < 300) {
        var json = JSON.parse(xhr.response);
        resolve(json, xhr.statusText, xhr);
      }
      else {
        var error = new Error(xhr.statusText)
        error.response = xhr.response
        reject(error);
      }
    }
    xhr.open(method, url);
    xhr.setRequestHeader('Content-Type', 'application/json');
    xhr.send(body);
  });
}
