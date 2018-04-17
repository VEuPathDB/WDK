import React from 'react';

import Icon from 'Mesa/Components/Icon';
import TableSearch from 'Mesa/Ui/TableSearch';
import ColumnEditor from 'Mesa/Ui/ColumnEditor';

class TableToolbar extends React.PureComponent {
  constructor (props) {
    super(props);
  }

  render () {
    const { dispatch, state, filteredRows, children } = this.props;
    const { rows, columns, options } = state;
    const hiddenRowCount = rows.length - filteredRows.length;
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
        <div className="TableToolbar-Info">
          {!filteredRows.length
          ? (
            <p><span className="faded">No results.</span></p>
          )
          : (
            <p>
              Showing
              <b> {filteredRows.length} </b>
              {!hiddenRowCount ? null : (
                <span> of {rows.length} </span>
              )}
              Rows
            </p>
          )}
        </div>
        {children && (
          <div className="TableToolbar-Children">
            {children}
          </div>
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
