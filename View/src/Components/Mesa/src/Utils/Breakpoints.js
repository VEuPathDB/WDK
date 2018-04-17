export const gridSizes = {
  xs: null,
  sm: 470,
  md: 750,
  lg: 1045,
  xl: 1250
};

export const widthToBreakpoint = (width = 0) => {
  if (width < 480) return 'xs';
  if (width < 768) return 'sm';
  if (width < 1200) return 'md';
  if (width < 1500) return 'lg';
  else return 'xl';
}

export const currentBreakpoint = () => {
  return widthToBreakpoint(window.innerWidth);
}

export const breakpointToGridSize = (breakpoint = 'xs') => {
  return gridSizes[breakpoint];
}

export const widthToGridSize = (width) => {
  let breakpoint = widthToBreakpoint(width);
  let gridSize = breakpointToGridSize(breakpoint);
  return gridSize == null ? width : gridSize;
}

export const factorGridSize = (factor = 1, width = 0) => {
  let gridSize = widthToGridSize(width);
  return gridSize * factor;
}

export const autoWidth = () => {
  return widthToGridSize(window.innerWidth);
}

export const colWidth = (cols = 12) => {
  let gridSize = autoWidth();
  return gridSize * (1 / (12 / cols));
}

export const autoFactor = (factor = 1) => {
  return factorGridSize(factor, window.innerWidth);
}
