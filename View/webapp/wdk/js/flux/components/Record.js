import _ from 'lodash';
import React from 'react';
import {
  formatAttributeName
} from '../utils/stringUtils';

/**
 * Record detail component
 */

const hasValue = _.property('value');
const hasLength = _.property('length');
const notPk = attr => attr.name != 'primary_key';
const and = (...fns) => (arg) => fns.reduce((pass, fn) => pass && fn(arg), true);

/**
 * Creates a predicate function based on predicate functions `funcs`.
 *
 * `value` to each fn and
 */
const passesAll = function passesAll(...funcs) {
  return function predicate(value) {
    return funcs.reduce(function(pass, func) {
      return pass && func(value);
    }, true);
  };
}

const Record = React.createClass({
  propTypes: {
    record: React.PropTypes.object.isRequired
  },

  render() {
    const { record } = this.props;
    const { attributes, tables } = record;
    return (
      <div className="wdk-Record">
        <h3 dangerouslySetInnerHTML={{__html: record.id}}/>
        <div className="wdk-Record-attributes">
          {attributes
            .filter(passesAll(notPk, hasValue))
            .map(this._renderAttribute)}
        </div>
        <div className="wdk-Record-tables">
          {record.tables.map(this._renderTable)}
        </div>
      </div>
    );
  },

  _renderAttribute(attribute) {
    const { name, value } = attribute;
    if (typeof value === 'undefined') return null;
    return (
      <div className="wdk-Record-attribute">
        <h4>{formatAttributeName(name)}</h4>
        <div dangerouslySetInnerHTML={{__html: value}}/>
      </div>
    );
  },

  _renderTable(table) {
    const { name, rows } = table;
    return (
      <div>
        <h4>{formatAttributeName(name)}</h4>
        <ul>
          {rows.filter(hasLength).map(this._renderTableRow)}
        </ul>
      </div>
    );
  },

  _renderTableRow(attributes) {
    return (
      <li>
        {attributes.filter(hasValue).map(this._renderTableRowAttribute)}
      </li>
    );
  },

  _renderTableRowAttribute(attribute) {
    return (
      <p>{attribute.name}: {attribute.value}</p>
    );
  }
});

export default Record;
