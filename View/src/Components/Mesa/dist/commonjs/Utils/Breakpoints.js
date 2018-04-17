'use strict';

Object.defineProperty(exports, "__esModule", {
  value: true
});
var gridSizes = exports.gridSizes = {
  xs: null,
  sm: 470,
  md: 750,
  lg: 1045,
  xl: 1250
};

var widthToBreakpoint = exports.widthToBreakpoint = function widthToBreakpoint() {
  var width = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 0;

  if (width < 480) return 'xs';
  if (width < 768) return 'sm';
  if (width < 1200) return 'md';
  if (width < 1500) return 'lg';else return 'xl';
};

var currentBreakpoint = exports.currentBreakpoint = function currentBreakpoint() {
  return widthToBreakpoint(window.innerWidth);
};

var breakpointToGridSize = exports.breakpointToGridSize = function breakpointToGridSize() {
  var breakpoint = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 'xs';

  return gridSizes[breakpoint];
};

var widthToGridSize = exports.widthToGridSize = function widthToGridSize(width) {
  var breakpoint = widthToBreakpoint(width);
  var gridSize = breakpointToGridSize(breakpoint);
  return gridSize == null ? width : gridSize;
};

var factorGridSize = exports.factorGridSize = function factorGridSize() {
  var factor = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 1;
  var width = arguments.length > 1 && arguments[1] !== undefined ? arguments[1] : 0;

  var gridSize = widthToGridSize(width);
  return gridSize * factor;
};

var autoWidth = exports.autoWidth = function autoWidth() {
  return widthToGridSize(window.innerWidth);
};

var colWidth = exports.colWidth = function colWidth() {
  var cols = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 12;

  var gridSize = autoWidth();
  return gridSize * (1 / (12 / cols));
};

var autoFactor = exports.autoFactor = function autoFactor() {
  var factor = arguments.length > 0 && arguments[0] !== undefined ? arguments[0] : 1;

  return factorGridSize(factor, window.innerWidth);
};