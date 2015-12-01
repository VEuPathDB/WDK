import { Component, PropTypes } from 'react';
import classnames from 'classnames';
import { wrappable } from '../utils/componentUtils';
import * as i from '../utils/Iterable';
import { postorder as postorderCategories } from '../utils/CategoryTreeIterators';
import shallowEqual from '../utils/shallowEqual';
import RecordNavigationItem from './RecordNavigationItem';
import Tree from './Tree';

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
    let categories = this.props.showChildren
      ? postorderCategories(this.props.categories)
      : this.props.categories;

    let activeCategory = i.find(function(category) {
      let categoryNode = document.getElementById(category.name);
      if (categoryNode == null) return true;
      let rect = categoryNode.parentElement.getBoundingClientRect();
      return rect.top < 12 && rect.bottom > -12;
    }, categories);

    this.setState({ activeCategory });
  }

  render() {
    return (
      <Tree
        tree={this.props.categories}
        id={c => c.name}
        childNodes={c => c.subCategories}
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
