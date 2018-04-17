import React from 'react';

import HeadingCell from '../Ui/HeadingCell';
import SelectionCell from '../Ui/SelectionCell';
import { ColumnDefaults } from '../Defaults';

class HeadingRow extends React.PureComponent {
  constructor (props) {
    super(props);
  }

  render () {
    const { options, columns, actions, uiState, eventHandlers } = this.props;
    const hasSelectionColumn = typeof options.isRowSelected === 'function'
      && typeof eventHandlers.onRowSelect === 'function'
      && typeof eventHandlers.onRowDeselect === 'function';

    return (
      <tr className="Row HeadingRow">
        {!hasSelectionColumn
          ? null
          : <SelectionCell
              heading={true}
              rows={rows}
              eventHandlers={eventHandlers}
              isRowSelected={options.isRowSelected}
            />
        }
        {columns.map(column => (
          <HeadingCell
            key={column.key}
            column={column}
            sort={uiState.sort}
            eventHandlers={eventHandlers}
          />
        ))}
      </tr>
    );
  }
};

export default HeadingRow;
