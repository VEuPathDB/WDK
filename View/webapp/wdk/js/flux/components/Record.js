import _ from 'lodash';
import React from 'react';
import {
  formatAttributeName,
  formatAttributeValue
} from '../utils/stringUtils';


const Record = React.createClass({
  propTypes: {
    record: React.PropTypes.object.isRequired
  },

  render() {
    const { record, recordClass } = this.props;
    const recordAttributes = record.attributes;
    const recordTables = record.tables;

    const attributes = recordClass.attributes.map(attribute => {
      return {
        meta: attribute,
        model: recordAttributes[attribute.name]
      };
    });

    // const tables = this.props.tables.map(table => {
    //   return {
    //     meta: table,
    //     model: recordTables[table.name]
    //   };
    // });

    return (
      <div className="wdk-Record">
        <h1 dangerouslySetInnerHTML={{__html: record.id}}/>
        <div className="wdk-Record-attributes">
          {attributes
            .filter(attr => attr.meta.name !== 'primary_key')
            .map(this._renderAttribute)}
        </div>
        <div className="wdk-Record-tables">
          {_.map(recordTables, this._renderTable)}
        </div>
      </div>
    );
  },

  _renderAttribute(attribute) {
    const { meta, model } = attribute;
    if (model.value == null) return null;
    return (
      <div className="wdk-Record-attribute">
        <h4>{formatAttributeName(meta.name)}</h4>
        <div dangerouslySetInnerHTML={{__html: formatAttributeValue(model.value, meta.type)}}/>
      </div>
    );
  },

  _renderTable(table) {
    const { name, rows } = table;
    if (rows.length) {
      return (
        <div>
          <h4>{formatAttributeName(name)}</h4>
          <ul>
            {rows.map(this._renderTableRow)}
          </ul>
        </div>
      );
    }
    return null;
  },

  _renderTableRow(attributes) {
    return (
      <li>
        {attributes.filter(_.property('value')).map(this._renderTableRowAttribute)}
      </li>
    );
  },

  _renderTableRowAttribute(attribute) {
    return (
      <p>{formatAttributeName(attribute.name)}: {attribute.value}</p>
    );
  }
});

export default Record;
