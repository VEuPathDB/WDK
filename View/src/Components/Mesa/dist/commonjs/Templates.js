'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});

var _react = require('react');

var _react2 = _interopRequireDefault(_react);

var _Defaults = require('./Defaults');

var _OverScroll = require('./Components/OverScroll');

var _OverScroll2 = _interopRequireDefault(_OverScroll);

var _TruncatedText = require('./Components/TruncatedText');

var _TruncatedText2 = _interopRequireDefault(_TruncatedText);

var _Utils = require('./Utils/Utils');

var _Utils2 = _interopRequireDefault(_Utils);

function _interopRequireDefault(obj) { return obj && obj.__esModule ? obj : { default: obj }; }

var Templates = {
  cell: function cell(column, row) {
    var key = column.key,
        truncated = column.truncated;

    if (!key) return;

    var className = 'Cell Cell-' + key;
    var value = row[key];
    var text = _Utils2.default.stringValue(value);

    return truncated ? _react2.default.createElement(_TruncatedText2.default, { className: className, cutoff: truncated ? _Defaults.OptionsDefaults.overflowHeight : null, text: text }) : _react2.default.createElement(
      'div',
      { className: className },
      text
    );
  },
  numberCell: function numberCell(column, row) {
    var key = column.key,
        truncated = column.truncated;

    if (!key) return;

    var className = 'Cell NumberCell Cell-' + key;
    var value = row[key];
    var display = typeof value === 'number' ? value.toLocaleString() : _Utils2.default.stringValue(value);

    return _react2.default.createElement(
      'div',
      { className: className },
      display
    );
  },
  htmlCell: function htmlCell(column, row) {
    var key = column.key,
        truncated = column.truncated;

    if (!key) return;

    var className = 'Cell HtmlCell Cell-' + key;
    var content = _react2.default.createElement('div', { dangerouslySetInnerHTML: { __html: row[key] } });
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
  heading: function heading(column) {
    var key = column.key,
        name = column.name;

    if (!key) return;

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