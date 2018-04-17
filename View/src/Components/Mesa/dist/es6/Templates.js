'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _Defaults = require('./Defaults');

var _OverScroll = require('./Components/OverScroll');

var _OverScroll2 = _interopRequireDefault(_OverScroll);

var _TruncatedText = require('./Components/TruncatedText');

var _TruncatedText2 = _interopRequireDefault(_TruncatedText);

var _Utils = require('./Utils/Utils');

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var Templates = {
  textCell: function textCell(_ref) {
    var key = _ref.key,
        value = _ref.value,
        row = _ref.row,
        rowIndex = _ref.rowIndex,
        column = _ref.column;
    var truncated = column.truncated;

    var className = 'Cell Cell-' + key;
    var text = (0, _Utils.stringValue)(value);

    return truncated ? _react2.default.createElement(_TruncatedText2.default, { className: className, cutoff: truncated ? _Defaults.OptionsDefaults.overflowHeight : null, text: text }) : _react2.default.createElement(
      'div',
      { className: className },
      text
    );
  },
  numberCell: function numberCell(_ref2) {
    var key = _ref2.key,
        value = _ref2.value,
        row = _ref2.row,
        rowIndex = _ref2.rowIndex,
        column = _ref2.column;

    var className = 'Cell NumberCell Cell-' + key;
    var display = typeof value === 'number' ? value.toLocaleString() : (0, _Utils.stringValue)(value);

    return _react2.default.createElement(
      'div',
      { className: className },
      display
    );
  },
  linkCell: function linkCell(_ref3) {
    var key = _ref3.key,
        value = _ref3.value,
        row = _ref3.row,
        rowIndex = _ref3.rowIndex,
        column = _ref3.column;

    var className = 'Cell LinkCell Cell-' + key;
    var defaults = { href: null, target: '_blank', text: '' };

    var _ref4 = (typeof value === 'undefined' ? 'undefined' : _typeof(value)) === 'object' ? value : defaults,
        href = _ref4.href,
        target = _ref4.target,
        text = _ref4.text;

    href = href ? href : typeof value === 'string' ? value : '#';
    text = text.length ? text : href;

    var props = { href: href, target: target, className: className, name: text };

    return _react2.default.createElement(
      'a',
      props,
      text
    );
  },
  htmlCell: function htmlCell(_ref5) {
    var key = _ref5.key,
        value = _ref5.value,
        row = _ref5.row,
        rowIndex = _ref5.rowIndex,
        column = _ref5.column;
    var truncated = column.truncated;

    var className = 'Cell HtmlCell Cell-' + key;
    var content = _react2.default.createElement('div', { dangerouslySetInnerHTML: { __html: value } });
    var size = truncated === true ? '16em' : truncated;

    return truncated ? _react2.default.createElement(
      _OverScroll2.default,
      { className: className, size: size },
      content
    ) : _react2.default.createElement(
      'div',
      { className: className },
      content
    );
  },
  heading: function heading(_ref6) {
    var key = _ref6.key,
        name = _ref6.name;

    var className = 'Cell HeadingCell HeadingCell-' + key;
    var content = _react2.default.createElement(
      'b',
      null,
      name || key
    );

    return _react2.default.createElement(
      'div',
      { className: className },
      content
    );
  }
};

exports.default = Templates;