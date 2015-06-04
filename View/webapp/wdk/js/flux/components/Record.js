import _ from 'lodash';
import React from 'react';
import {
  formatAttributeName,
  formatAttributeValue
} from '../utils/stringUtils';


let Record = React.createClass({
  propTypes: {
    record: React.PropTypes.object.isRequired
  },

  render() {
    let { meta, record } = this.props;
    let { attributes: recordAttributes, tables: recordTables } = record;
    let attributes = meta.attributes.map(attribute => {
      return {
        meta: attribute,
        value: recordAttributes[attribute.name]
      };
    });

    let tables = meta.tables.map(table => {
      return {
        meta: table,
        values: recordTables[table.name]
      };
    });

    let displayName = recordAttributes.primary_key;
    return (
      <div className="wdk-Record">
        <h1 dangerouslySetInnerHTML={{__html: displayName}}/>
        <div className="wdk-Record-attributes">
          {attributes
            .filter(attr => attr.meta.name !== 'primary_key')
            .map(this._renderAttribute)}
        </div>
        <div className="wdk-Record-tables">
          {_.map(tables, this._renderTable)}
        </div>
      </div>
    );
  },

  _renderAttribute(attribute) {
    let { meta, value } = attribute;
    if (value == null) return null;
    return (
      <div className="wdk-Record-attribute">
        <h4>{formatAttributeName(meta.name)}</h4>
        <div dangerouslySetInnerHTML={{__html: formatAttributeValue(value, meta.type)}}/>
      </div>
    );
  },

  _renderTable(table) {
    let { meta, values } = table;
    if (values.length) {
      return (
        <div>
          <h4>{formatAttributeName(meta.name)}</h4>
          <ul>
            {values.map(this._renderTableRow)}
          </ul>
        </div>
      );
    }
    return null;
  },

  _renderTableRow(attributes) {
    return (
      <li>
        {_.pairs(attributes).filter(([name, value]) => value != null).map(this._renderTableRowAttribute)}
      </li>
    );
  },

  _renderTableRowAttribute([ name, value ]) {
    return (
      <p>{formatAttributeName(name)}: {value}</p>
    );
  }
});

export default Record;
