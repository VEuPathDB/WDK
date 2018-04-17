import React from 'react';

import Icon from 'Mesa/Components/Icon';
import TableSearch from 'Mesa/Ui/TableSearch';
import ColumnEditor from 'Mesa/Ui/ColumnEditor';

class TableToolbar extends React.PureComponent {
  constructor (props) {
    super(props);
  }

  render () {
    const { dispatch, state } = this.props;
    const { columns, options } = state;
    const columnsAreHideable = columns.some(column => column.hideable);

    return (
      <div className="TableToolbar">
        {options.title && (
          <h1 className="TableToolbar-Title">{options.title}</h1>
        )}
        {options.search && (
          <TableSearch
            state={state}
            dispatch={dispatch}
          />
        )}
        {columnsAreHideable && (
          <ColumnEditor columns={columns} dispatch={dispatch}>
            <button>
              <Icon fa={'columns'} />
              Add/Remove Columns
            </button>
          </ColumnEditor>
        )}
      </div>
    );
  }
};

export default TableToolbar;
