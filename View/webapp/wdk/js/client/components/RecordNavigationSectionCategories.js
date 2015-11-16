import { Component, PropTypes } from 'react';
import classnames from 'classnames';
import { wrappable } from '../utils/componentUtils';
import { takeWhile } from '../utils/Categories';
import shallowEqual from '../utils/shallowEqual';
import RecordNavigationSectionCategoryTree from './RecordNavigationSectionCategoryTree';

/**
 * Handle scroll events to mark the active category in the nav panel.
 */
class RecordNavigationSectionCategories extends Component {

  constructor() {
    super(...arguments);
    this.setActiveCategory = this.setActiveCategory.bind(this);
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

  setActiveCategory() {
    let scrolledCategories = takeWhile(this.props.categories, function(category) {
      let categoryNode = document.getElementById(category.name);
      if (categoryNode == null) return true;
      return categoryNode.getBoundingClientRect().top < 10;
    }, this.props.showChildren);
    this.setState({ activeCategory: scrolledCategories.pop() });
  }

  render() {
    return (
      <RecordNavigationSectionCategoryTree {...this.props} {...this.state} />
    );
  }

}

export default wrappable(RecordNavigationSectionCategories);
