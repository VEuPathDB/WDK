import React from 'react';
import PureRenderMixin from 'react-addons-pure-render-mixin';
import chunk from 'lodash/array/chunk';
import DataTable from './DataTable';
import { renderAttributeValue, wrappable } from '../utils/componentUtils';

let maxColumns = 4;

let RecordTable = React.createClass({

  mixins: [ PureRenderMixin ],

  render() {
    let { value, table } = this.props;
    let tableAttributes = table.attributes.filter(a => a.isDisplayable);
    let classnames = [ 'wdk-RecordTable', 'wdk-RecordTable__' + table.name ].join(' ');
    if (value.length === 0 || tableAttributes.length === 0) {
      return <em>No data available</em>;
    }
    if (tableAttributes.length === 1) {
      let attributeName = tableAttributes[0].name;
      let listColumnSize = Math.max(10, value.length / maxColumns);
      return (
        <div className={classnames}>
          {chunk(value, listColumnSize).map((tableChunk, index) =>
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
          data={this.props.value}
          sorting={this.props.table.sorting}
          childRow={this.props.childRow}
          searchable={this.props.value.length > 10}
        />
      </div>
    );
  }

});

export default wrappable(RecordTable);
