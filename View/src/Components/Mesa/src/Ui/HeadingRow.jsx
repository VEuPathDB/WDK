import React from 'react';

import HeadingCell from '../Ui/HeadingCell';
import SelectionCell from '../Ui/SelectionCell';
import { ColumnDefaults } from '../Defaults';

class HeadingRow extends React.PureComponent {
  constructor (props) {
    super(props);
  }

  render () {
    const { rows, options, columns, actions, uiState, eventHandlers } = this.props;
    const { isRowSelected } = options ? options : {};
    const { sort } = uiState ? uiState : {};
    const { onRowSelect, onRowDeselect } = eventHandlers ? eventHandlers : {};
    const rowList = this.props.rows;
    const hasSelectionColumn = typeof isRowSelected === 'function'
      && typeof onRowSelect === 'function'
      && typeof onRowDeselect === 'function';

      console.log('USING ROWS!', rowList);


    return (
      <tr className="Row HeadingRow">
        {!hasSelectionColumn
          ? null
          : <SelectionCell
              heading={true}
              rows={rowList}
              eventHandlers={eventHandlers}
              isRowSelected={isRowSelected}
            />
        }
        {columns.map((column, columnIndex) => (
          <HeadingCell
            sort={sort}
            key={column.key}
            column={column}
            columnIndex={columnIndex}
            eventHandlers={eventHandlers}
          />
        ))}
      </tr>
    );
  }
};

export default HeadingRow;
