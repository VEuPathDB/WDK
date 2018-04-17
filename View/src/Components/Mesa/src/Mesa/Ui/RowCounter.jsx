import React from 'react';

import PaginationUtils from 'Mesa/Utils/PaginationUtils';

class RowCounter extends React.PureComponent {
  constructor (props) {
    super(props);
    this.getPageString = this.getPageString.bind(this);
    this.getFilteredString = this.getFilteredString.bind(this);
    this.getStatistics = this.getStatistics.bind(this);
  }

  getFilteredString () {
    let { filtered } = this.getStatistics();
    let { searchQuery } = this.props.state.ui;
    return filtered && !searchQuery ? <span className="faded"> (<b>{filtered}</b> filtered)</span> : null
  }

  getStatistics () {
    let { state, filteredRows } = this.props;
    let total = state.rows.length;
    let effective = filteredRows.length;
    let filtered = total - effective;
    return { total, effective, filtered };
  }

  getPageString () {
    let { filteredRows, state } = this.props;
    let { paginate } = state.options;
    let { pagination, searchQuery } = state.ui;

    let { total, effective, filtered } = this.getStatistics();

    let noun = searchQuery ? 'Result' : 'Row';
    let plural = noun + (effective !== 1 ? 's' : '');

    let simple = (<span><b>{effective}</b>  {plural}</span>);

    if (!paginate) return simple;

    let currentPage = PaginationUtils.getCurrentPageNumber(pagination);
    let firstOnPage = PaginationUtils.firstItemOnPage(currentPage, pagination);
    let lastOnPage = PaginationUtils.lastItemOnPage(currentPage, pagination, filteredRows);

    if (effective === lastOnPage && firstOnPage === 1) return simple;

    return (
      <span>
        {effective !== firstOnPage ? plural : noun} <b>{firstOnPage}</b>
        {firstOnPage !== lastOnPage ? <span> - <b>{lastOnPage}</b></span> : null}
        {effective !== firstOnPage ? <span> of <b>{effective}</b></span> : null}
      </span>
    );
  }

  render () {
    let { filteredRows } = this.props;
    let filteredString = this.getFilteredString();
    let pageString = this.getPageString();

    return !filteredRows.length ? null : (
      <div className="RowCounter">
        {pageString}
        {filteredString}
      </div>
    );
  }
};

export default RowCounter;
