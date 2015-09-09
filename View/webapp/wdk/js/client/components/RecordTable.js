import React from 'react';
import { wrappable } from '../utils/componentUtils';
import {
  formatAttributeValue
} from '../utils/stringUtils';

let RecordTable = React.createClass({

  mixins: [ React.addons.PureRenderMixin ],

  render() {
    let { tableMeta, table } = this.props;

    if (table.length && tableMeta.attributes.length) {
      // ordered set of attribute names for all table rows
      let headings = Array.from(new Set(...table.map(Object.keys)));
      return (
        <table className="wdk-RecordTable">
          <thead>
            {tableMeta.attributes.map(function(attribute) {
              return (
                <th key={attribute.name}>{attribute.displayName}</th>
              );
            })}
          </thead>
          <tbody>
            {table.map(function(attributes, index) {
              return renderTableRow(attributes, tableMeta, index);
            })}
          </tbody>
        </table>
      );
    }

    return null;
  }
});

function renderTableRow(attributes, tableMeta, index) {
  return (
    <tr key={index}>
      {tableMeta.attributes.map(function(attribute) {
        let { name } = attribute;
        return (
          <td
            key={name}
            dangerouslySetInnerHTML={{__html: formatAttributeValue(attributes[name])}}
          />
          );
      })}
    </tr>
  );
}

export default wrappable(RecordTable);
