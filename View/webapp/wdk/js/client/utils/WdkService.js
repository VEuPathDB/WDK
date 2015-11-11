import stringify from 'json-stable-stringify';
import difference from 'lodash/array/difference';

export default class WdkService {

  constructor(serviceUrl) {
    this._serviceUrl = serviceUrl;

    // caches
    this._questions = new Map();
    this._recordClasses = new Map();
    this._records = new Map();
  }

  getQuestions() {
    let method = 'get';
    let url = this._serviceUrl + '/question?expandQuestions=true';

    if (!this._questions.isFull) {
      this._questions.isFull = true;
      fetchJson(method, url).then(
        questions => {
          for (let question of questions) {
            this._questions.set(question.name, Promise.resolve(question));
          }
        },
        reason => {
          this._questions.isFull = false;
          throw reason;
        }
      );
    }

    return Promise.all(this._questions.values());
  }

  getQuestion(questionName) {
    let method = 'get';
    let url = this._serviceUrl + '/question/' + questionName;

    if (!this._questions.has(questionName)) {
      this._questions.set(questionName, fetchJson(method, url));
    }

    return this._questions.get(questionName);
  }

  getRecordClasses() {
    let method = 'get';
    let url = this._serviceUrl + '/record?expandRecordClasses=true';

    if (!this._recordClasses.isFull) {
      fetchJson(method, url).then(
        recordClasses => {
          this._recordClasses.isFull = true;
          for (let recordClass of recordClasses) {
            recordClass.attributeCategories.push(
              { name: undefined, displayName: 'Uncategorized' }
            );
            this._recordClasses.set(recordClass.fullName, Promise.resolve(recordClass));
          }
        },
        reason => {
          this._recordClasses.isFull = false;
          throw reason;
        }
      );
    }

    return Promise.all(this._recordClasses.values());
  }

  getRecordClass(recordClassName) {
    let method = 'get';
    let url = this._serviceUrl + '/record/' + recordClassName;

    if (!this._recordClasses.has(recordClassName)) {
      this._recordClasses.set(recordClassName, fetchJson(method, url).then(recordClass => {
        recordClass.attributeCategories.push(
          { name: undefined, displayName: 'Uncategorized' }
        );
        return recordClass;
      }));
    }

    return this._recordClasses.get(recordClassName);
  }

  getRecord(recordClassName, primaryKey, options = {}) {
    let primaryKeyString = stringify(primaryKey);
    let key = recordClassName + ':' + primaryKeyString;
    let method = 'post';
    let url = this._serviceUrl + '/record/' + recordClassName + '/instance';

    let { attributes = [], tables = [] } = options;

    // if we don't have the record, fetch whatever is requested
    if (!this._records.has(key)) {
      let body = stringify({ primaryKey, attributes, tables });
      this._records.set(key, fetchJson(method, url, body).then(response => response.record));
    }

    else {
      // determine which tables and attributes we need to retreive
      this._records.set(key, this._records.get(key).then(record => {
        let reqAttributes = difference(attributes, Object.keys(record.attributes));
        let reqTables = difference(tables, Object.keys(record.tables));

        // get addition attributes and tables
        if (reqAttributes.length > 0 || reqTables.length > 0) {
          let body = stringify({
            primaryKey,
            attributes: reqAttributes,
            tables: reqTables
          });

          // merge old record attributes and tables with new record
          return fetchJson(method, url, body).then(response => {
            Object.assign(response.record.attributes, record.attributes);
            Object.assign(response.record.tables, record.tables);
            return response.record;
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
      let recordClassName = response.meta.class;
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
