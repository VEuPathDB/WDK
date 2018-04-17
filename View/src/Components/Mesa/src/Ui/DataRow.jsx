import React from 'react';

import DataCell from '../Ui/DataCell';
import SelectionCell from '../Ui/SelectionCell';
import { makeClassifier } from '../Utils/Utils';

const dataRowClass = makeClassifier('DataRow');

class DataRow extends React.PureComponent {
  constructor (props) {
    super(props);
    this.state = { expanded: false };
    this.toggleRow = this.toggleRow.bind(this);
    this.expandRow = this.expandRow.bind(this);
    this.collapseRow = this.collapseRow.bind(this);
    this.componentWillReceiveProps = this.componentWillReceiveProps.bind(this);
  }

  componentWillReceiveProps (newProps) {
    const { row } = this.props;
    if (newProps.row !== row) this.collapseRow();
  }

  expandRow () {
    const { options } = this.props;
    if (!options.inline) return;
    this.setState({ expanded: true });
  }

  collapseRow () {
    const { options } = this.props;
    if (!options.inline) return;
    this.setState({ expanded: false });
  }

  toggleRow () {
    const { options } = this.props;
    if (!options.inline) return;

    const { expanded } = this.state;
    this.setState({ expanded: !expanded });
  }

  render () {
    const { row, rowIndex, columns, options, actions, eventHandlers } = this.props;
    const { expanded } = this.state;
    const inline = options.inline ? !expanded : false;

    const hasSelectionColumn = typeof options.isRowSelected === 'function'
      && typeof eventHandlers.onRowSelect === 'function'
      && typeof eventHandlers.onRowDeselect === 'function';

    const rowStyle = !inline ? {} : { whiteSpace: 'nowrap', textOverflow: 'ellipsis' };
    const className = dataRowClass(null, inline ? 'inline' : '');

    const cellProps = { row, inline, options, rowIndex };

    return (
      <tr className={className} style={rowStyle} onClick={this.toggleRow}>
        {!hasSelectionColumn
          ? null
          : <SelectionCell
              row={row}
              eventHandlers={eventHandlers}
              isRowSelected={options.isRowSelected}
            />
        }
        {columns.map((column, columnIndex) => <DataCell key={column.key} column={column} columnIndex={columnIndex} {...cellProps} />)}
      </tr>
    )
  }
};

export default DataRow;
