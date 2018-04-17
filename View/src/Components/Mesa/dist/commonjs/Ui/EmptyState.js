'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _createClass = function () { function defineProperties(target, props) { for (var i = 0; i < props.length; i++) { var descriptor = props[i]; descriptor.enumerable = descriptor.enumerable || false; descriptor.configurable = true; if ("value" in descriptor) descriptor.writable = true; Object.defineProperty(target, descriptor.key, descriptor); } } return function (Constructor, protoProps, staticProps) { if (protoProps) defineProperties(Constructor.prototype, protoProps); if (staticProps) defineProperties(Constructor, staticProps); return Constructor; }; }();

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _Icon = require('../Components/Icon');

var _Icon2 = _interopRequireDefault(_Icon);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

function _classCallCheck(instance, Constructor) { if (!(instance instanceof Constructor)) { throw new TypeError("Cannot call a class as a function"); } }

function _possibleConstructorReturn(self, call) { if (!self) { throw new ReferenceError("this hasn't been initialised - super() hasn't been called"); } return call && (typeof call === "object" || typeof call === "function") ? call : self; }

function _inherits(subClass, superClass) { if (typeof superClass !== "function" && superClass !== null) { throw new TypeError("Super expression must either be null or a function, not " + typeof superClass); } subClass.prototype = Object.create(superClass && superClass.prototype, { constructor: { value: subClass, enumerable: false, writable: true, configurable: true } }); if (superClass) Object.setPrototypeOf ? Object.setPrototypeOf(subClass, superClass) : subClass.__proto__ = superClass; }

var EmptyState = function (_React$PureComponent) {
  _inherits(EmptyState, _React$PureComponent);

  function EmptyState(props) {
    _classCallCheck(this, EmptyState);

    var _this = _possibleConstructorReturn(this, (EmptyState.__proto__ || Object.getPrototypeOf(EmptyState)).call(this, props));

    _this.getCulprit = _this.getCulprit.bind(_this);
    return _this;
  }

  _createClass(EmptyState, [{
    key: 'getCulprit',
    value: function getCulprit() {
      var culprit = this.props.culprit;

      switch (culprit) {
        case 'search':
          return {
            icon: 'search',
            title: 'No Results',
            content: _react2.default.createElement(
              'div',
              null,
              _react2.default.createElement(
                'p',
                null,
                'Sorry, your search returned no results.'
              )
            )
          };
        case 'nocolumns':
          return {
            icon: 'columns',
            title: 'No Columns Shown',
            content: _react2.default.createElement(
              'div',
              null,
              _react2.default.createElement(
                'p',
                null,
                'Whoops, looks like you\'ve hidden all columns. Use the column editor to show some columns.'
              )
            )
          };
        case 'filters':
          return {
            icon: 'filter',
            title: 'No Filter Results',
            content: _react2.default.createElement(
              'div',
              null,
              _react2.default.createElement(
                'p',
                null,
                'No rows exist that match all of your column filter settings.'
              )
            )
          };
        case 'nodata':
        default:
          return {
            icon: 'table',
            title: 'No Data',
            content: _react2.default.createElement(
              'div',
              null,
              _react2.default.createElement(
                'p',
                null,
                'Whoops! Either no table data was provided, or the data provided could not be parsed.'
              )
            )
          };
      }
    }
  }, {
    key: 'render',
    value: function render() {
      var colspan = this.props.colspan;

      var culprit = this.getCulprit();

      return _react2.default.createElement(
        'tr',
        { className: 'EmptyState' },
        _react2.default.createElement(
          'td',
          { colSpan: colspan },
          _react2.default.createElement(
            'div',
            { className: 'EmptyState-Body-Wrapper' },
            _react2.default.createElement(
              'div',
              { className: 'EmptyState-Body' },
              _react2.default.createElement(_Icon2.default, { fa: culprit.icon, className: 'EmptyState-Icon' }),
              _react2.default.createElement(
                'h2',
                null,
                culprit.title
              ),
              culprit.content
            )
          )
        )
      );
    }
  }]);

  return EmptyState;
}(_react2.default.PureComponent);

;

exports.default = EmptyState;