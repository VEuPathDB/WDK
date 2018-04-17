import React from 'react';

import Icon from 'Mesa/Components/Icon';

class Pagination extends React.PureComponent {
  constructor (props) {
    super(props);
  }

  renderPageLink (page) {
    const { onPageChange, currentPage } = this.props;
    let handler = () => this.goToPage(page);
    return (
      <a onClick={handler} key={page} className={currentPage === page ? 'active' : 'inactive'}>
        {page}
      </a>
    );
  }

  getRelativePageNumber (relative) {
    let { currentPage, pages } = this.props;
    switch (relative.toLowerCase()) {
      case 'first':
      case 'start':
        return 1;
      case 'last':
      case 'end':
        return pages;
      case 'next':
        return (++currentPage <= pages ? currentPage : 1);
      case 'prev':
      case 'previous':
        return (--currentPage >= 1 ? currentPage : pages);
      default:
        return null;
    }
  }

  getRelativeIcon (relative) {
    switch (relative.toLowerCase()) {
      case 'first':
      case 'start':
        return 'angle-double-left';
      case 'last':
      case 'end':
        return 'angle-double-right';
      case 'next':
        return 'caret-right';
      case 'prev':
      case 'previous':
        return 'caret-left';
      default:
        return null;
    }
  }

  goToPage (page) {
    const { onPageChange } = this.props;
    if (onPageChange && page) onPageChange(page);
  }

  renderRelativeLink (relative) {
    const { currentPage, pages, onCh } = this.props;
    const page = this.getRelativePageNumber(relative);
    const icon = this.getRelativeIcon(relative);

    return (!page || !icon) ? null : (
      <a
        onClick={() => this.goToPage(page)}
        title={'Jump to the ' + relative + ' page'}
      >
        <Icon fa={icon} />
      </a>
    )
  }

  render () {
    const { pages, onPageChange, currentPage } = this.props;
    let list = new Array(pages).fill({}).map((empty, index) => this.renderPageLink(++index));

    return (
      <div className="Pagination">
        <span className="Pagination-Nav">
          {this.renderRelativeLink('previous')}
        </span>
        {list}
        <span className="Pagination-Nav">
          {this.renderRelativeLink('next')}
        </span>
      </div>
    );
  }
};

export default Pagination;
