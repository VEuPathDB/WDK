import React from 'react';
import PropTypes from 'prop-types';

import DataRow from '../Ui/DataRow';
import HeadingRow from '../Ui/HeadingRow';
import EmptyState from '../Ui/EmptyState';

class DataTable extends React.Component {
  constructor (props) {
    super(props);
  }

  render () {
    const { rows, options, columns, actions, uiState, eventHandlers } = this.props;
    const props = { rows, options, columns, actions, uiState, eventHandlers };
    const { emptinessCulprit, sort } = uiState;

    const hasSelectionColumn = typeof options.isRowSelected === 'function'
      && typeof eventHandlers.onRowSelect === 'function'
      && typeof eventHandlers.onRowDeselect === 'function';
    const colspan = columns.filter(column => !column.hidden).length + (hasSelectionColumn ? 1 : 0);

    return (
      <div className="DataTable">
        <table cellSpacing="0" cellPadding="0">
          <tbody>
            <HeadingRow {...props} />
          </tbody>
          <tbody>
            {rows.length
              ? rows.map((row, idx) => (
                <DataRow
                  key={key}
                  row={row}
                  rowIndex={idx}
                  {...props}
                />
              ))
              : <EmptyState colspan={colspan} culprit={emptinessCulprit} />
            }
          </tbody>
        </table>
      </div>
    );
  }
};

DataTable.propTypes = {
  rows: PropTypes.array,
  columns: PropTypes.array,
  options: PropTypes.object,
  actions: PropTypes.arrayOf(PropTypes.shape({
    element: PropTypes.oneOfType([ PropTypes.func, PropTypes.node, PropTypes.element ]),
    handler: PropTypes.func,
    callback: PropTypes.func
  })),
  uiState: PropTypes.object,
  eventHandlers: PropTypes.objectOf(PropTypes.func)
};

export default DataTable;
