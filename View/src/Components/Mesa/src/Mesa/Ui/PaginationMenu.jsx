import React from 'react';

import Icon from 'Mesa/Components/Icon';

const settings = {
  overflowPoint: 8,
  innerRadius: 2
}

class PaginationMenu extends React.PureComponent {
  constructor (props) {
    super(props);
    this.renderPageLink = this.renderPageLink.bind(this);
    this.renderEllipsis = this.renderEllipsis.bind(this);
    this.renderPageList = this.renderPageList.bind(this);
    this.renderDynamicPageLink = this.renderDynamicPageLink.bind(this);
  }

  renderEllipsis (key = '') {
    return (
      <a key={'ellipsis-' + key} className="ellipsis">
        ...
      </a>
    );
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

  renderDynamicPageLink (page, idx, list) {
    const link = this.renderPageLink(page);
    const dots = this.renderEllipsis(page);
    const { currentPage } = this.props;
    const { innerRadius } = settings;
    const activeIndex = list.indexOf(currentPage);

    if (idx === 0 || idx + 1 === list.length) return link;
    if (idx >= activeIndex - innerRadius && idx <= activeIndex + innerRadius) return link;
    if (idx === activeIndex - innerRadius - 1) return dots;
    if (idx === activeIndex + innerRadius + 1) return dots;
    return null;
  }

  renderPageList (pageList) {
    const count = pageList.length;
    const { overflowPoint } = settings;

    if (count > overflowPoint) {
      return pageList.map(this.renderDynamicPageLink).filter(el => el);
    } else {
      return pageList.map(this.renderPageLink);
    }
  }

  render () {
    const { pages, onPageChange, currentPage } = this.props;
    let pageList = new Array(pages).fill({}).map((empty, index) => index + 1);
    let pageNav = this.renderPageList(pageList);

    return (
      <div className="Pagination">
        <span className="Pagination-Nav">
          {this.renderRelativeLink('previous')}
        </span>
        <span className="Pagination-Nav">
          {pageNav}
        </span>
        <span className="Pagination-Nav">
          {this.renderRelativeLink('next')}
        </span>
      </div>
    );
  }
};

export default PaginationMenu;
