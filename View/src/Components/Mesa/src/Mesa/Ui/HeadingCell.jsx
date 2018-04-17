import React from 'react';

import Templates from 'Mesa/Templates';
import ColumnSorter from 'Mesa/Ui/ColumnSorter';
import { toggleSortOrder, sortByColumn } from 'Mesa/State/Actions';

class HeadingCell extends React.PureComponent {
  constructor (props) {
    super(props);
  }

  renderContent () {
    let { column } = this.props;
    if ('renderHeading' in column) return column.renderHeading(column);
    return Templates.heading(column);
  }

  handleSortClick () {
    let { column, state, dispatch } = this.props;
    let { sort } = state.ui;
    let currentlySorting = sort.byColumn === column;
    dispatch(currentlySorting ? toggleSortOrder() : sortByColumn(column));
  }

  render () {
    let { column, state } = this.props;
    let content = this.renderContent();

    return column.hidden ? null : (
      <th
        key={column.key}
        ref={el => this.element = el}
        onClick={e => column.sortable ? this.handleSortClick() : null}
      >
        {column.sortable && (
          <ColumnSorter
            column={column}
            state={state}
          />
        )}
        {content}
      </th>
    )
  }
};

export default HeadingCell;
