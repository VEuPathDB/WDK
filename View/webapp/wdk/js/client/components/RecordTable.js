import React from 'react';
import PureRenderMixin from 'react-addons-pure-render-mixin';
import chunk from 'lodash/array/chunk';
import DataTable from './DataTable';
import { renderAttributeValue, wrappable } from '../utils/componentUtils';

let maxColumns = 4;

let RecordTable = React.createClass({

  mixins: [ PureRenderMixin ],

  render() {
    let { table, tableMeta } = this.props;
    let tableAttributes = tableMeta.attributes.filter(a => a.isDisplayable);
    let classnames = [ 'wdk-RecordTable', 'wdk-RecordTable__' + tableMeta.name ].join(' ');
    if (table.length === 0 || tableAttributes.length === 0) {
      return <em>No data available</em>;
    }
    if (tableAttributes.length === 1) {
      let attributeName = tableAttributes[0].name;
      let listColumnSize = Math.max(10, table.length / maxColumns);
      return (
        <div className={classnames}>
          {chunk(table, listColumnSize).map((tableChunk, index) =>
            <ul key={index} className="wdk-RecordTableList">
              {tableChunk.map((row, index) =>
                <li key={index}>{renderAttributeValue(row[attributeName])}</li>
              )}
            </ul>
          )}
        </div>
      );
    }
    return (
      <div className={classnames}>
        <DataTable
          columns={tableAttributes}
          data={this.props.table}
          sorting={this.props.tableMeta.sorting}
          childRow={this.props.childRow}
          searchable={this.props.table.length > 10}
        />
      </div>
    );
  }

});

export default wrappable(RecordTable);
