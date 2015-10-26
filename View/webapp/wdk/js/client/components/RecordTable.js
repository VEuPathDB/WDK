import React from 'react';
import DataTable from './DataTable.jquery';
import { renderAttributeValue, wrappable } from '../utils/componentUtils';

let RecordTable = React.createClass({

  mixins: [ React.addons.PureRenderMixin ],

  render() {
    let { table, tableMeta } = this.props;
    if (table.length === 0 || tableMeta.attributes.length === 0) {
      return null;
    }
    if (tableMeta.attributes.length === 1) {
      let attributeName = tableMeta.attributes[0].name;
      return (
        <ul>
          {table.map(
            (row, index) =>
            <li key={index}>{renderAttributeValue(row[attributeName])}</li>
          )}
        </ul>
      );
    }
    return <DataTable columns={this.props.tableMeta.attributes} data={this.props.table} />;
  }

});

export default wrappable(RecordTable);
