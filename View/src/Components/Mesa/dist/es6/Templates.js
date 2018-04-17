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
//# sourceMappingURL=data:application/json;charset=utf-8;base64,eyJ2ZXJzaW9uIjozLCJzb3VyY2VzIjpbIi4uLy4uL3NyYy9UZW1wbGF0ZXMuanN4Il0sIm5hbWVzIjpbIlRlbXBsYXRlcyIsInRleHRDZWxsIiwia2V5IiwidmFsdWUiLCJyb3ciLCJyb3dJbmRleCIsImNvbHVtbiIsInRydW5jYXRlZCIsImNsYXNzTmFtZSIsInRleHQiLCJvdmVyZmxvd0hlaWdodCIsIm51bWJlckNlbGwiLCJkaXNwbGF5IiwidG9Mb2NhbGVTdHJpbmciLCJsaW5rQ2VsbCIsImRlZmF1bHRzIiwiaHJlZiIsInRhcmdldCIsImxlbmd0aCIsInByb3BzIiwibmFtZSIsImh0bWxDZWxsIiwiY29udGVudCIsIl9faHRtbCIsInNpemUiLCJoZWFkaW5nIl0sIm1hcHBpbmdzIjoiOzs7Ozs7OztBQUFBOzs7O0FBRUE7O0FBQ0E7Ozs7QUFDQTs7OztBQUNBOzs7O0FBRUEsSUFBTUEsWUFBWTtBQUNoQkMsVUFEZ0IsMEJBQ2lDO0FBQUEsUUFBckNDLEdBQXFDLFFBQXJDQSxHQUFxQztBQUFBLFFBQWhDQyxLQUFnQyxRQUFoQ0EsS0FBZ0M7QUFBQSxRQUF6QkMsR0FBeUIsUUFBekJBLEdBQXlCO0FBQUEsUUFBcEJDLFFBQW9CLFFBQXBCQSxRQUFvQjtBQUFBLFFBQVZDLE1BQVUsUUFBVkEsTUFBVTtBQUFBLFFBQ3ZDQyxTQUR1QyxHQUN6QkQsTUFEeUIsQ0FDdkNDLFNBRHVDOztBQUUvQyxRQUFNQyxZQUFZLGVBQWVOLEdBQWpDO0FBQ0EsUUFBTU8sT0FBTyx3QkFBWU4sS0FBWixDQUFiOztBQUVBLFdBQU9JLFlBQ0gseURBQWUsV0FBV0MsU0FBMUIsRUFBcUMsUUFBUUQsWUFBWSwwQkFBZ0JHLGNBQTVCLEdBQTZDLElBQTFGLEVBQWdHLE1BQU1ELElBQXRHLEdBREcsR0FFSDtBQUFBO0FBQUEsUUFBSyxXQUFXRCxTQUFoQjtBQUE0QkM7QUFBNUIsS0FGSjtBQUdELEdBVGU7QUFXaEJFLFlBWGdCLDZCQVdtQztBQUFBLFFBQXJDVCxHQUFxQyxTQUFyQ0EsR0FBcUM7QUFBQSxRQUFoQ0MsS0FBZ0MsU0FBaENBLEtBQWdDO0FBQUEsUUFBekJDLEdBQXlCLFNBQXpCQSxHQUF5QjtBQUFBLFFBQXBCQyxRQUFvQixTQUFwQkEsUUFBb0I7QUFBQSxRQUFWQyxNQUFVLFNBQVZBLE1BQVU7O0FBQ2pELFFBQU1FLFlBQVksMEJBQTBCTixHQUE1QztBQUNBLFFBQU1VLFVBQVUsT0FBT1QsS0FBUCxLQUFpQixRQUFqQixHQUE0QkEsTUFBTVUsY0FBTixFQUE1QixHQUFxRCx3QkFBWVYsS0FBWixDQUFyRTs7QUFFQSxXQUFPO0FBQUE7QUFBQSxRQUFLLFdBQVdLLFNBQWhCO0FBQTRCSTtBQUE1QixLQUFQO0FBQ0QsR0FoQmU7QUFrQmhCRSxVQWxCZ0IsMkJBa0JpQztBQUFBLFFBQXJDWixHQUFxQyxTQUFyQ0EsR0FBcUM7QUFBQSxRQUFoQ0MsS0FBZ0MsU0FBaENBLEtBQWdDO0FBQUEsUUFBekJDLEdBQXlCLFNBQXpCQSxHQUF5QjtBQUFBLFFBQXBCQyxRQUFvQixTQUFwQkEsUUFBb0I7QUFBQSxRQUFWQyxNQUFVLFNBQVZBLE1BQVU7O0FBQy9DLFFBQU1FLFlBQVksd0JBQXdCTixHQUExQztBQUNBLFFBQU1hLFdBQVcsRUFBRUMsTUFBTSxJQUFSLEVBQWNDLFFBQVEsUUFBdEIsRUFBZ0NSLE1BQU0sRUFBdEMsRUFBakI7O0FBRitDLGdCQUdqQixRQUFPTixLQUFQLHlDQUFPQSxLQUFQLE9BQWlCLFFBQWpCLEdBQTRCQSxLQUE1QixHQUFvQ1ksUUFIbkI7QUFBQSxRQUd6Q0MsSUFIeUMsU0FHekNBLElBSHlDO0FBQUEsUUFHbkNDLE1BSG1DLFNBR25DQSxNQUhtQztBQUFBLFFBRzNCUixJQUgyQixTQUczQkEsSUFIMkI7O0FBSS9DTyxXQUFRQSxPQUFPQSxJQUFQLEdBQWUsT0FBT2IsS0FBUCxLQUFpQixRQUFqQixHQUE0QkEsS0FBNUIsR0FBb0MsR0FBM0Q7QUFDQU0sV0FBUUEsS0FBS1MsTUFBTCxHQUFjVCxJQUFkLEdBQXFCTyxJQUE3Qjs7QUFFQSxRQUFNRyxRQUFRLEVBQUVILFVBQUYsRUFBUUMsY0FBUixFQUFnQlQsb0JBQWhCLEVBQTJCWSxNQUFNWCxJQUFqQyxFQUFkOztBQUVBLFdBQU87QUFBQTtBQUFPVSxXQUFQO0FBQWVWO0FBQWYsS0FBUDtBQUNELEdBNUJlO0FBOEJoQlksVUE5QmdCLDJCQThCaUM7QUFBQSxRQUFyQ25CLEdBQXFDLFNBQXJDQSxHQUFxQztBQUFBLFFBQWhDQyxLQUFnQyxTQUFoQ0EsS0FBZ0M7QUFBQSxRQUF6QkMsR0FBeUIsU0FBekJBLEdBQXlCO0FBQUEsUUFBcEJDLFFBQW9CLFNBQXBCQSxRQUFvQjtBQUFBLFFBQVZDLE1BQVUsU0FBVkEsTUFBVTtBQUFBLFFBQ3ZDQyxTQUR1QyxHQUN6QkQsTUFEeUIsQ0FDdkNDLFNBRHVDOztBQUUvQyxRQUFNQyxZQUFZLHdCQUF3Qk4sR0FBMUM7QUFDQSxRQUFNb0IsVUFBVyx1Q0FBSyx5QkFBeUIsRUFBRUMsUUFBUXBCLEtBQVYsRUFBOUIsR0FBakI7QUFDQSxRQUFNcUIsT0FBUWpCLGNBQWMsSUFBZCxHQUFxQixNQUFyQixHQUE4QkEsU0FBNUM7O0FBRUEsV0FBT0EsWUFDSDtBQUFBO0FBQUEsUUFBWSxXQUFXQyxTQUF2QixFQUFrQyxNQUFNZ0IsSUFBeEM7QUFBK0NGO0FBQS9DLEtBREcsR0FFSDtBQUFBO0FBQUEsUUFBSyxXQUFXZCxTQUFoQjtBQUE0QmM7QUFBNUIsS0FGSjtBQUdELEdBdkNlO0FBeUNoQkcsU0F6Q2dCLDBCQXlDUTtBQUFBLFFBQWJ2QixHQUFhLFNBQWJBLEdBQWE7QUFBQSxRQUFSa0IsSUFBUSxTQUFSQSxJQUFROztBQUN0QixRQUFNWixZQUFZLGtDQUFrQ04sR0FBcEQ7QUFDQSxRQUFNb0IsVUFBVztBQUFBO0FBQUE7QUFBSUYsY0FBUWxCO0FBQVosS0FBakI7O0FBRUEsV0FDRTtBQUFBO0FBQUEsUUFBSyxXQUFXTSxTQUFoQjtBQUNHYztBQURILEtBREY7QUFLRDtBQWxEZSxDQUFsQjs7a0JBcURldEIsUyIsImZpbGUiOiJUZW1wbGF0ZXMuanMiLCJzb3VyY2VzQ29udGVudCI6WyJpbXBvcnQgUmVhY3QgZnJvbSAncmVhY3QnO1xuXG5pbXBvcnQgeyBPcHRpb25zRGVmYXVsdHMgfSBmcm9tICcuL0RlZmF1bHRzJztcbmltcG9ydCBPdmVyU2Nyb2xsIGZyb20gJy4vQ29tcG9uZW50cy9PdmVyU2Nyb2xsJztcbmltcG9ydCBUcnVuY2F0ZWRUZXh0IGZyb20gJy4vQ29tcG9uZW50cy9UcnVuY2F0ZWRUZXh0JztcbmltcG9ydCB7IHN0cmluZ1ZhbHVlIH0gZnJvbSAnLi9VdGlscy9VdGlscyc7XG5cbmNvbnN0IFRlbXBsYXRlcyA9IHtcbiAgdGV4dENlbGwgKHsga2V5LCB2YWx1ZSwgcm93LCByb3dJbmRleCwgY29sdW1uIH0pIHtcbiAgICBjb25zdCB7IHRydW5jYXRlZCB9ID0gY29sdW1uO1xuICAgIGNvbnN0IGNsYXNzTmFtZSA9ICdDZWxsIENlbGwtJyArIGtleTtcbiAgICBjb25zdCB0ZXh0ID0gc3RyaW5nVmFsdWUodmFsdWUpO1xuXG4gICAgcmV0dXJuIHRydW5jYXRlZFxuICAgICAgPyA8VHJ1bmNhdGVkVGV4dCBjbGFzc05hbWU9e2NsYXNzTmFtZX0gY3V0b2ZmPXt0cnVuY2F0ZWQgPyBPcHRpb25zRGVmYXVsdHMub3ZlcmZsb3dIZWlnaHQgOiBudWxsfSB0ZXh0PXt0ZXh0fSAvPlxuICAgICAgOiA8ZGl2IGNsYXNzTmFtZT17Y2xhc3NOYW1lfT57dGV4dH08L2Rpdj5cbiAgfSxcblxuICBudW1iZXJDZWxsICh7IGtleSwgdmFsdWUsIHJvdywgcm93SW5kZXgsIGNvbHVtbiB9KSB7XG4gICAgY29uc3QgY2xhc3NOYW1lID0gJ0NlbGwgTnVtYmVyQ2VsbCBDZWxsLScgKyBrZXk7XG4gICAgY29uc3QgZGlzcGxheSA9IHR5cGVvZiB2YWx1ZSA9PT0gJ251bWJlcicgPyB2YWx1ZS50b0xvY2FsZVN0cmluZygpIDogc3RyaW5nVmFsdWUodmFsdWUpO1xuXG4gICAgcmV0dXJuIDxkaXYgY2xhc3NOYW1lPXtjbGFzc05hbWV9PntkaXNwbGF5fTwvZGl2PlxuICB9LFxuXG4gIGxpbmtDZWxsICh7IGtleSwgdmFsdWUsIHJvdywgcm93SW5kZXgsIGNvbHVtbiB9KSB7XG4gICAgY29uc3QgY2xhc3NOYW1lID0gJ0NlbGwgTGlua0NlbGwgQ2VsbC0nICsga2V5O1xuICAgIGNvbnN0IGRlZmF1bHRzID0geyBocmVmOiBudWxsLCB0YXJnZXQ6ICdfYmxhbmsnLCB0ZXh0OiAnJyB9O1xuICAgIGxldCB7IGhyZWYsIHRhcmdldCwgdGV4dCB9ID0gKHR5cGVvZiB2YWx1ZSA9PT0gJ29iamVjdCcgPyB2YWx1ZSA6IGRlZmF1bHRzKTtcbiAgICBocmVmID0gKGhyZWYgPyBocmVmIDogKHR5cGVvZiB2YWx1ZSA9PT0gJ3N0cmluZycgPyB2YWx1ZSA6ICcjJykpO1xuICAgIHRleHQgPSAodGV4dC5sZW5ndGggPyB0ZXh0IDogaHJlZik7XG5cbiAgICBjb25zdCBwcm9wcyA9IHsgaHJlZiwgdGFyZ2V0LCBjbGFzc05hbWUsIG5hbWU6IHRleHQgfTtcblxuICAgIHJldHVybiA8YSB7Li4ucHJvcHN9Pnt0ZXh0fTwvYT5cbiAgfSxcblxuICBodG1sQ2VsbCAoeyBrZXksIHZhbHVlLCByb3csIHJvd0luZGV4LCBjb2x1bW4gfSkge1xuICAgIGNvbnN0IHsgdHJ1bmNhdGVkIH0gPSBjb2x1bW47XG4gICAgY29uc3QgY2xhc3NOYW1lID0gJ0NlbGwgSHRtbENlbGwgQ2VsbC0nICsga2V5O1xuICAgIGNvbnN0IGNvbnRlbnQgPSAoPGRpdiBkYW5nZXJvdXNseVNldElubmVySFRNTD17eyBfX2h0bWw6IHZhbHVlIH19IC8+KTtcbiAgICBjb25zdCBzaXplID0gKHRydW5jYXRlZCA9PT0gdHJ1ZSA/ICcxNmVtJyA6IHRydW5jYXRlZCk7XG5cbiAgICByZXR1cm4gdHJ1bmNhdGVkXG4gICAgICA/IDxPdmVyU2Nyb2xsIGNsYXNzTmFtZT17Y2xhc3NOYW1lfSBzaXplPXtzaXplfT57Y29udGVudH08L092ZXJTY3JvbGw+XG4gICAgICA6IDxkaXYgY2xhc3NOYW1lPXtjbGFzc05hbWV9Pntjb250ZW50fTwvZGl2PlxuICB9LFxuXG4gIGhlYWRpbmcgKHsga2V5LCBuYW1lIH0pIHtcbiAgICBjb25zdCBjbGFzc05hbWUgPSAnQ2VsbCBIZWFkaW5nQ2VsbCBIZWFkaW5nQ2VsbC0nICsga2V5O1xuICAgIGNvbnN0IGNvbnRlbnQgPSAoPGI+e25hbWUgfHwga2V5fTwvYj4pO1xuXG4gICAgcmV0dXJuIChcbiAgICAgIDxkaXYgY2xhc3NOYW1lPXtjbGFzc05hbWV9PlxuICAgICAgICB7Y29udGVudH1cbiAgICAgIDwvZGl2PlxuICAgIClcbiAgfVxufTtcblxuZXhwb3J0IGRlZmF1bHQgVGVtcGxhdGVzO1xuIl19