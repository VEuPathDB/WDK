import { Component, PropTypes } from 'react';
import classnames from 'classnames';
import get from 'lodash/object/get';
import { wrappable } from '../utils/componentUtils';
import * as t from '../utils/TreeUtils';
import shallowEqual from '../utils/shallowEqual';
import RecordNavigationItem from './RecordNavigationItem';
import Tree from './Tree';

let categoryNodeIsActive = (node) => {
  let categoryName = get(node, [ 'properties', 'label', 0 ]);
  let categoryNode = document.getElementById(categoryName);
  if (categoryNode == null) return true;
  let rect = categoryNode.getBoundingClientRect();
  return rect.top < 12 && rect.bottom > -12;
}

let activeNodeReducer = (found, node) => {
  return found == null && categoryNodeIsActive(node) ? node
  : found;
}

/**
 * Handle scroll events to mark the active category in the nav panel.
 */
class RecordNavigationSectionCategories extends Component {

  constructor() {
    super(...arguments);
    this.setActiveCategory = this.setActiveCategory.bind(this);
    this.state = { activeCategory: null };
  }

  componentDidMount() {
    window.addEventListener('scroll', this.setActiveCategory);
  }

  componentWillUnmount() {
    window.removeEventListener('scroll', this.setActiveCategory);
  }

  componentDidUpdate(previousProps) {
    if (this.props.collapsedCategories !== previousProps.collapsedCategories ||
        this.props.showChildren !== previousProps.showChildren ) {
      this.setActiveCategory();
    }
  }

  shouldComponentUpdate(nextProps, nextState) {
    return !shallowEqual(nextProps, this.props) || !shallowEqual(nextState, this.state);
  }

  // If showChildren is true, iterate postorder to get the first left-most child
  // that is on-screen. Otherwise, we will only iterate top-level categories.
  setActiveCategory() {
    let activeCategory = this.props.showChildren
      ? t.reduce(activeNodeReducer, null, { children: this.props.categories })
      : this.props.categories.reduce(activeNodeReducer, null);

    this.setState({ activeCategory });
  }

  render() {
    return (
      <Tree
        tree={this.props.categories}
        id={c => c.properties.label[0]}
        childNodes={c => c.children}
        node={RecordNavigationItem}
        showChildren={this.props.showChildren}
        onCategoryToggle={this.props.onCategoryToggle}
        isCollapsed={this.props.isCollapsed}
        isVisible={this.props.isVisible}
        activeCategory={this.state.activeCategory}
      />
    );
  }

}

export default wrappable(RecordNavigationSectionCategories);
