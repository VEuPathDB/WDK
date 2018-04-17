import React from 'react';

import HeadingCell from 'Mesa/Ui/HeadingCell';

class HeadingRow extends React.PureComponent {
  constructor (props) {
    super(props);
  }

  render () {
    const { state, dispatch } = this.props;
    const { columns } = state;

    return (
      <tr className="HeadingRow">
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
