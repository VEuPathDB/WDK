import React from 'react';

import HeadingCell from 'Mesa/Ui/HeadingCell';
import SelectionCell from 'Mesa/Ui/SelectionCell';

class HeadingRow extends React.PureComponent {
  constructor (props) {
    super(props);
  }

  render () {
    const { state, dispatch, filteredRows } = this.props;
    const { columns, actions } = state;

    return (
      <tr className="HeadingRow">
        {actions.length
          ? <SelectionCell
              heading={true}
              state={state}
              dispatch={dispatch}
              filteredRows={filteredRows}
            />
          : null
        }
        {columns.map(column => (
          <HeadingCell
            key={column.key}
            column={column}
            state={state}
            dispatch={dispatch}
          />
        ))}
      </tr>
    );
  }
};

export default HeadingRow;
