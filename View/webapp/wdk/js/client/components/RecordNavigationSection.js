import React from 'react';
import classnames from 'classnames';
import includes from 'lodash/collection/includes';
import RecordNavigationSectionCategories from './RecordNavigationSectionCategories';
import { wrappable } from '../utils/componentUtils';
import { takeWhile } from '../utils/Categories';

let RecordNavigationSection = React.createClass({

  propTypes: {
    collapsedCategories: React.PropTypes.array,
    onCategoryToggle: React.PropTypes.func,
    heading: React.PropTypes.node
  },

  mixins: [ React.addons.PureRenderMixin ],

  getInitialState() {
    return {
      navigationExpanded: false,
      navigationQuery: ''
    };
  },

  getDefaultProps() {
    return {
      onCategoryToggle: function noop() {},
      heading: 'Categories'
    };
  },

  componentDidMount() {
    window.addEventListener('scroll', this.setActiveCategory);
  },

  componentWillUnmount() {
    window.removeEventListener('scroll', this.setActiveCategory);
  },

  componentDidUpdate(previousProps) {
    if (this.props.collapsedCategories !== previousProps.collapsedCategories) {
      this.setActiveCategory();
    }
  },

  setNavigationExpanded(navigationExpanded) {
    this.setState({ navigationExpanded }, this.setActiveCategory);
  },

  setActiveCategory() {
    let { attributeCategories } = this.props.recordClass;
    let scrolledCategories = takeWhile(attributeCategories, function(category) {
      let categoryNode = document.getElementById(category.name);
      if (categoryNode == null) return true;
      return categoryNode.getBoundingClientRect().top < 10;
    }, this.state.navigationExpanded);
    this.setState({ activeCategory: scrolledCategories.pop() });
  },

  render() {
    let { categoryWordsMap, collapsedCategories, heading } = this.props;
    let { navigationExpanded, navigationQuery, activeCategory } = this.state;
    let navigationQueryLower = navigationQuery.toLowerCase();

    let expandClassName = classnames({
      'wdk-RecordNavigationExpand fa': true,
      'fa-plus-square': !navigationExpanded,
      'fa-minus-square': navigationExpanded
    });

    return (
      <div className="wdk-RecordNavigationSection">
        <div className="wdk-RecordNavigationSearch">
          <input
            className="wdk-RecordNavigationSearchInput"
            placeholder={'Search ' + heading}
            type="text"
            value={navigationQuery}
            onChange={e => {
              this.setNavigationExpanded(true);
              this.setState({ navigationQuery: e.target.value });
            }}
          />
        </div>
        <h2 className="wdk-RecordNavigationSectionHeader">
          <button className={expandClassName}
            onClick={() => void this.setNavigationExpanded(!navigationExpanded)}
          /> {heading}
        </h2>
        <div className="wdk-RecordNavigationCategories">
          <RecordNavigationSectionCategories
            record={this.props.record}
            recordClass={this.props.recordClass}
            categories={this.props.recordClass.attributeCategories}
            onCategoryToggle={this.props.onCategoryToggle}
            showChildren={navigationExpanded}
            isCollapsed={category => includes(collapsedCategories, category.name)}
            isVisible={category => includes(categoryWordsMap.get(category), navigationQueryLower)}
            activeCategory={activeCategory}
          />
        </div>
      </div>
    );
  }
});

export default wrappable(RecordNavigationSection);
