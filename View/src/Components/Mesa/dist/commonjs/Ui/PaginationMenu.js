'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _Icon = require('../Components/Icon');

var _Icon2 = _interopRequireDefault(_Icon);

var _PaginationUtils = require('../Utils/PaginationUtils');

var _PaginationUtils2 = _interopRequireDefault(_PaginationUtils);

var _PaginationEditor = require('../Ui/PaginationEditor');

var _PaginationEditor2 = _interopRequireDefault(_PaginationEditor);

var _Actions = require('../State/Actions');

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
          list = _props.list,
          paginationState = _props.paginationState;


      switch (relative.toLowerCase()) {
        case 'first':
        case 'start':
          return 1;
        case 'last':
        case 'end':
          return _PaginationUtils2.default.totalPages(list, paginationState);
        case 'next':
          return _PaginationUtils2.default.nextPageNumber(list, paginationState);
        case 'prev':
        case 'previous':
          return _PaginationUtils2.default.prevPageNumber(list, paginationState);
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
      var _props2 = this.props,
          paginationState = _props2.paginationState,
          dispatch = _props2.dispatch;

      var anchorIndex = _PaginationUtils2.default.firstItemOnPage(page, paginationState);
      dispatch((0, _Actions.setPaginationAnchor)(anchorIndex));
    }
  }, {
    key: 'renderRelativeLink',
    value: function renderRelativeLink(relative) {
      var _this3 = this;

      var page = this.getRelativePageNumber(relative);
      var icon = this.getRelativeIcon(relative);

      return !page || !icon ? null : _react2.default.createElement(
        'a',
        {
          onClick: function onClick() {
            return _this3.goToPage(page);
          },
          title: 'Jump to the ' + relative + ' page'
        },
        _react2.default.createElement(_Icon2.default, { fa: icon })
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
      var _this4 = this;

      var _props3 = this.props,
          paginationState = _props3.paginationState,
          list = _props3.list;
      var overflowPoint = settings.overflowPoint;

      var current = _PaginationUtils2.default.getCurrentPageNumber(paginationState);
      var total = _PaginationUtils2.default.totalPages(list, paginationState);
      var pageList = _PaginationUtils2.default.generatePageList(total);

      if (total > overflowPoint) {
        return pageList.map(function (page) {
          return _this4.renderDynamicPageLink(page, current, total);
        }).filter(function (el) {
          return el;
        });
      } else {
        return pageList.map(function (page) {
          return _this4.renderPageLink(page, current);
        });
      }
    }
  }, {
    key: 'render',
    value: function render() {
      var _props4 = this.props,
          list = _props4.list,
          paginationState = _props4.paginationState,
          dispatch = _props4.dispatch;

      return !list.length ? null : _react2.default.createElement(
        'div',
        { className: 'PaginationMenu' },
        _react2.default.createElement('span', { className: 'Pagination-Spacer' }),
        _react2.default.createElement(
          'span',
          { className: 'Pagination-Nav' },
          this.renderRelativeLink('previous')
        ),
        _react2.default.createElement(
          'span',
          { className: 'Pagination-Nav' },
          this.renderPageList()
        ),
        _react2.default.createElement(
          'span',
          { className: 'Pagination-Nav' },
          this.renderRelativeLink('next')
        ),
        _react2.default.createElement(
          'span',
          { className: 'Pagination-Editor' },
          _react2.default.createElement(_PaginationEditor2.default, { paginationState: paginationState, dispatch: dispatch })
        )
      );
    }
  }]);

  return PaginationMenu;
}(_react2.default.PureComponent);

;

exports.default = PaginationMenu;