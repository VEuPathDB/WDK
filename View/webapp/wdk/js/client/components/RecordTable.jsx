import { chunk } from 'lodash';
import { Component, PropTypes } from 'react';
import DataTable from './DataTable';
import { renderAttributeValue, pure, wrappable } from '../utils/componentUtils';

// max columns for list mode
const maxColumns = 4;

const defaultSortId = '@@defaultSortIndex@@';
const defaultSortColumn = [{ name: defaultSortId, isDisplayable: false }];
const getSortIndex = (rowData) => rowData[defaultSortId];
const addDefaultSortId = (row, index) => Object.assign({}, row, { [defaultSortId]: index });

/**
 * Renders a record table
 */
class RecordTable extends Component {
  constructor(props) {
    super(props);
    this.setColumns(props);
    this.setData(props);
  }

  componentWillReceiveProps(nextProps) {
    // Only update columns and data if props change -- to prevent unneeded render

    if (nextProps.table !== this.props.table)
      this.setColumns(nextProps);

    if (nextProps.value !== this.props.value)
      this.setData(nextProps);
  }

  setColumns(props) {
    this.displayableAttributes = props.table.attributes.filter(attr => attr.isDisplayable);
    this.columns = defaultSortColumn.concat(this.displayableAttributes);
  }

  setData(props) {
    this.data = props.value.map(addDefaultSortId);
  }

  render() {
    let { value, table, childRow, expandedRows, onExpandedRowsChange, className } = this.props;

    if (this.displayableAttributes.length === 1) {
      let listColumnSize = Math.max(10, value.length / maxColumns);
      let attributeName = this.displayableAttributes[0].name;
      return (
        <div className={className}>
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

    return (
      <div className={className}>
        {table.description && <p>{table.description}</p>}
        <DataTable
          getRowId={getSortIndex}
          expandedRows={expandedRows}
          onExpandedRowsChange={onExpandedRowsChange}
          columns={this.columns}
          data={this.data}
          childRow={childRow}
          searchable={value.length > 1}
        />
      </div>
    );
  }
}

RecordTable.propTypes = {
  value: PropTypes.array.isRequired,
  table: PropTypes.object.isRequired,
  childRow: PropTypes.oneOfType([
    PropTypes.string,
    PropTypes.func
  ]),
  expandedRows: PropTypes.arrayOf(PropTypes.number),
  onExpandedRowsChange: PropTypes.func,
  className: PropTypes.string
};

export default wrappable(pure(RecordTable));
