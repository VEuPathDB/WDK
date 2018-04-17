'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _propTypes = require('prop-types');

var _propTypes2 = _interopRequireDefault(_propTypes);

var _Icon = require('../Components/Icon');

var _Icon2 = _interopRequireDefault(_Icon);

var _RowsPerPageMenu = require('../Ui/RowsPerPageMenu');

var _RowsPerPageMenu2 = _interopRequireDefault(_RowsPerPageMenu);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var settings = {
  overflowPoint: 8,
  innerRadius: 2
};

var PaginationMenu = function (_React$PureComponent) {
  _inherits(PaginationMenu, _React$PureComponent);

  function PaginationMenu(props) {
    _classCallCheck(this, PaginationMenu);

    var _this = _possibleConstructorReturn(this, (PaginationMenu.__proto__ || Object.getPrototypeOf(PaginationMenu)).call(this, props));

    _this.renderPageLink = _this.renderPageLink.bind(_this);
    _this.renderEllipsis = _this.renderEllipsis.bind(_this);
    _this.renderPageList = _this.renderPageList.bind(_this);
    _this.renderDynamicPageLink = _this.renderDynamicPageLink.bind(_this);
    return _this;
  }

  _createClass(PaginationMenu, [{
    key: 'renderEllipsis',
    value: function renderEllipsis() {
      var key = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : '';

      return _react2.default.createElement(
        'a',
        { key: 'ellipsis-' + key, className: 'ellipsis' },
        '...'
      );
    }
  }, {
    key: 'renderPageLink',
    value: function renderPageLink(page, current) {
      var _this2 = this;

      var handler = function handler() {
        return _this2.goToPage(page);
      };
      return _react2.default.createElement(
        'a',
        { onClick: handler, key: page, className: current === page ? 'active' : 'inactive' },
        page
      );
    }
  }, {
    key: 'getRelativePageNumber',
    value: function getRelativePageNumber(relative) {
      var _props = this.props,
          currentPage = _props.currentPage,
          totalPages = _props.totalPages;

      switch (relative.toLowerCase()) {
        case 'first':
        case 'start':
          return 1;
        case 'last':
        case 'end':
          return totalPages;
        case 'next':
          return currentPage < totalPages ? currentPage + 1 : 1;
        case 'prev':
        case 'previous':
          return currentPage > 1 ? currentPage - 1 : totalPages;
        default:
          return null;
      }
    }
  }, {
    key: 'getRelativeIcon',
    value: function getRelativeIcon(relative) {
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
  }, {
    key: 'goToPage',
    value: function goToPage(page) {
      var onPageChange = this.props.onPageChange;

      if (onPageChange) onPageChange(page);
    }
  }, {
    key: 'renderRelativeLink',
    value: function renderRelativeLink(_ref) {
      var _this3 = this;

      var relative = _ref.relative;

      var page = this.getRelativePageNumber(relative);
      var icon = this.getRelativeIcon(relative);

      return !page || !icon ? null : _react2.default.createElement(
        'span',
        { className: 'Pagination-Nav' },
        _react2.default.createElement(
          'a',
          { onClick: function onClick() {
              return _this3.goToPage(page);
            }, title: 'Jump to the ' + relative + ' page' },
          _react2.default.createElement(_Icon2.default, { fa: icon })
        )
      );
    }
  }, {
    key: 'renderDynamicPageLink',
    value: function renderDynamicPageLink(page, current, total) {
      var link = this.renderPageLink(page, current);
      var dots = this.renderEllipsis(page);
      var innerRadius = settings.innerRadius;


      if (page === 1 || page === total) return link;
      if (page >= current - innerRadius && page <= current + innerRadius) return link;
      if (page === current - innerRadius - 1) return dots;
      if (page === current + innerRadius + 1) return dots;
      return null;
    }
  }, {
    key: 'renderPageList',
    value: function renderPageList() {
      var overflowPoint = settings.overflowPoint;
      var _props2 = this.props,
          totalPages = _props2.totalPages,
          currentPage = _props2.currentPage;


      var pageList = new Array(totalPages).fill({}).map(function (empty, index) {
        return index + 1;
      });

      var renderer = totalPages > overflowPoint ? this.renderDynamicPageLink : this.renderPageLink;
      return _react2.default.createElement(
        'span',
        { className: 'Pagination-Nav' },
        pageList.map(function (page) {
          return renderer(page, currentPage, totalPages);
        }).filter(function (e) {
          return e;
        })
      );
    }
  }, {
    key: 'renderPerPageMenu',
    value: function renderPerPageMenu() {
      var _props3 = this.props,
          rowsPerPage = _props3.rowsPerPage,
          rowsPerPageOptions = _props3.rowsPerPageOptions,
          onRowsPerPageChange = _props3.onRowsPerPageChange;

      if (!onRowsPerPageChange) return null;
      return _react2.default.createElement(
        'span',
        { className: 'Pagination-Editor' },
        _react2.default.createElement(_RowsPerPageMenu2.default, {
          rowsPerPage: rowsPerPage,
          rowsPerPageOptions: rowsPerPageOptions,
          onRowsPerPageChange: onRowsPerPageChange
        })
      );
    }
  }, {
    key: 'render',
    value: function render() {
      var _props4 = this.props,
          totalPages = _props4.totalPages,
          currentPage = _props4.currentPage;


      var PageList = this.renderPageList;
      var PerPageMenu = this.renderPerPageMenu;
      var RelativeLink = this.renderRelativeLink;

      return !totalPages || !currentPage ? null : _react2.default.createElement(
        'div',
        { className: 'PaginationMenu' },
        _react2.default.createElement('span', { className: 'Pagination-Spacer' }),
        _react2.default.createElement(RelativeLink, { relative: 'previous' }),
        _react2.default.createElement(PageList, null),
        _react2.default.createElement(RelativeLink, { relative: 'next' }),
        _react2.default.createElement(PerPageMenu, null)
      );
    }
  }]);

  return PaginationMenu;
}(_react2.default.PureComponent);

;

PaginationMenu.propTypes = {
  totalPages: _propTypes2.default.number,
  currentPage: _propTypes2.default.number,
  rowsPerPage: _propTypes2.default.number,
  onPageChange: _propTypes2.default.func,
  rowsPerPageOptions: _propTypes2.default.array,
  onRowsPerPageChange: _propTypes2.default.func
};

exports.default = PaginationMenu;