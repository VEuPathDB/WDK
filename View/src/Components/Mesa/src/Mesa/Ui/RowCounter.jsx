import React from 'react';

import PaginationUtils from 'Mesa/Utils/PaginationUtils';

class RowCounter extends React.PureComponent {
  constructor (props) {
    super(props);
  }

  render () {
    let { filteredRows, state } = this.props;
    let { options } = state;
    let { paginate } = options;
    let { pagination } = state.ui;

    let total = state.rows.length;
    let effective = filteredRows.length;
    let hidden = total - effective;

    let count;
    if (!paginate) count = (<span><b>{effective}</b>  Rows</span>);
    else {
      let currentPage = PaginationUtils.getCurrentPageNumber(pagination);
      let from = PaginationUtils.firstItemOnPage(currentPage, pagination);
      let to = PaginationUtils.lastItemOnPage(currentPage, pagination, filteredRows);
      count = (<span>Showing rows <b>{from}</b> to <b>{to}</b> of <b>{effective}</b></span>);
    }

    return !filteredRows.length ? null : (
      <div className="RowCounter">
        {count}
        {!hidden ? null : <span className="faded"> (<b>{hidden}</b> hidden)</span>}
      </div>
    );
  }
};

export default RowCounter;
