import React from 'react';
import PureRenderMixin from 'react-addons-pure-render-mixin';
import {chunk} from 'lodash';
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
          {table.description && <p>{table.description}</p>}
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

    let columns = [{
      name: '@@defaultSort@@',
      isDisplayable: false
    }].concat(tableAttributes);
    let data = this.props.value.map((row, index) => {
      return Object.assign({}, row, { '@@defaultSort@@': index });
    });
    return (
      <div className={classnames}>
        {table.description && <p>{table.description}</p>}
        <DataTable
          columns={columns}
          data={data}
          childRow={this.props.childRow}
          searchable={this.props.value.length > 1}
        />
      </div>
    );
  }

});

export default wrappable(RecordTable);
