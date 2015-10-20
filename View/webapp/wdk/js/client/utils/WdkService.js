import stringify from 'json-stable-stringify';
import difference from 'lodash/array/difference';

export default class WdkService {

  constructor(serviceUrl) {
    this._serviceUrl = serviceUrl;
    this._questions = new Map();
    this._recordClasses = new Map();
    this._records = new Map();
  }

  getQuestions() {
    let method = 'get';
    let url = this._serviceUrl + '/question?expandQuestions=true';
    if (this._questions.isFull) {
      return Promise.resolve([...this._questions.values()]);
    }
    return fetchJson(method, url).then(questions => {
      for (let question of questions) {
        this._questions.set(question.name, question);
      }
      this._questions.isFull = true;
      return [...this._questions.values()];
    });
  }

  getQuestion(questionName) {
    let method = 'get';
    let url = this._serviceUrl + '/question/' + questionName;
    if (this._questions.has(questionName)) {
      return Promise.resolve(this._questions.get(questionName));
    }
    return fetchJson(method, url).then(question => {
      this._questions.set(questionName, question);
      return question;
    });
  }

  getRecordClasses() {
    let method = 'get';
    let url = this._serviceUrl + '/record?expandRecordClasses=true';
    if (this._recordClasses.isFull) {
      return Promise.resolve([...this._recordClasses.values()]);
    }
    return fetchJson(method, url).then(recordClasses => {
      for (let recordClass of recordClasses) {
        this._recordClasses.set(recordClass.fullName, recordClass);
      }
      this._recordClasses.isFull = true;
      return [...this._recordClasses.values()];
    });
  }

  getRecordClass(recordClassName) {
    let method = 'get';
    let url = this._serviceUrl + '/record/' + recordClassName;
    if (this._recordClasses.has(recordClassName)) {
      return Promise.resolve(this._recordClasses.get(recordClassName));
    }
    return fetchJson(method, url).then(recordClass => {
      this._recordClasses.set(recordClassName, recordClass);
      return recordClass;
    });
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
      return fetchJson(method, url, body).then(response => {
        this._records.set(key, response.record);
        return response.record;
      });
    }

    // determine which tables and attributes we need to retreive
    let record = this._records.get(key);
    let reqAttributes = difference(attributes, Object.keys(record.attributes));
    let reqTables = difference(tables, Object.keys(record.tables));

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
        this._records.set(key, response.record);
        return response.record;
      });
    }

    return Promise.resolve(record);
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
        this._records.set(key, record);
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
