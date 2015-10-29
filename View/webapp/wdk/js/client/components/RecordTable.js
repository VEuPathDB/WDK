import React from 'react';
import chunk from 'lodash/array/chunk';
import DataTable from './DataTable.jquery';
import { renderAttributeValue, wrappable } from '../utils/componentUtils';

let listColumnSize = 10;

let RecordTable = React.createClass({

  mixins: [ React.addons.PureRenderMixin ],

  render() {
    let { table, tableMeta } = this.props;
    if (table.length === 0 || tableMeta.attributes.length === 0) {
      return <em>No data available</em>;
    }
    if (tableMeta.attributes.length === 1) {
      let attributeName = tableMeta.attributes[0].name;
      return (
        <div>
          {chunk(table, listColumnSize).map(tableChunk =>
            <ul className="wdk-RecordTableList">
              {tableChunk.map((row, index) =>
                <li key={index}>{renderAttributeValue(row[attributeName])}</li>
              )}
            </ul>
          )}
        </div>
      );
    }
    return <DataTable columns={this.props.tableMeta.attributes} data={this.props.table} />;
  }

});

export default wrappable(RecordTable);
