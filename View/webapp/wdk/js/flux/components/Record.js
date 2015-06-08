import map from 'lodash/collection/map';
import union from 'lodash/array/union';
import pairs from 'lodash/object/pairs';
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
        <table className="wdk-Record-attributes">
          <tbody>
            {attributes
              .filter(attr => attr.meta.name !== 'primary_key')
              .map(this._renderAttribute)}
          </tbody>
        </table>
        <div className="wdk-Record-tables">
          {map(tables, this._renderTable)}
        </div>
      </div>
    );
  },

  _renderAttribute(attribute) {
    let { meta, value } = attribute;
    if (value == null) return null;
    return (
      <tr className="wdk-Record-attribute">
        <th>{formatAttributeName(meta.name)}</th>
        <td dangerouslySetInnerHTML={{__html: formatAttributeValue(value, meta.type)}}/>
      </tr>
    );
  },

  _renderTable(table) {
    let { meta, values } = table;
    if (values.length) {
      let headings = union(...values.map(Object.keys));
      return (
        <div>
          <h4>{formatAttributeName(meta.name)}</h4>
          <table>
            <thead>
              {headings.map(heading => <th>{heading}</th>)}
            </thead>
            <tbody>
              {values.map(value => this._renderTableRow(value, headings))}
            </tbody>
          </table>
        </div>
      );
    }
    return null;
  },

  _renderTableRow(attributes, headings) {
    return (
      <tr>
        {headings.map(heading => <td dangerouslySetInnerHTML={{__html: attributes[heading]}}/>)}
      </tr>
    );
  }
});

export default Record;
