import React from 'react';

import Icon from 'Mesa/Components/Icon';

class ColumnSorter extends React.PureComponent {
  constructor (props) {
    super(props);
  }

  render () {
    let { column, state, dispatch } = this.props;
    let { sort } = state.ui;
    let currentlySorting = sort.byColumn === column;
    let sortIcon = !currentlySorting
      ? 'sort-amount-asc inactive'
      : sort.ascending
        ? 'sort-amount-asc active'
        : 'sort-amount-desc active';

    return (<Icon fa={sortIcon + ' Trigger SortTrigger'} />);
  }
};

export default ColumnSorter;
