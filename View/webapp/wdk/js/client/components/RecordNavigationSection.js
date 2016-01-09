import React from 'react';
import PureRenderMixin from 'react-addons-pure-render-mixin';
import classnames from 'classnames';
import includes from 'lodash/collection/includes';
import RecordNavigationSectionCategories from './RecordNavigationSectionCategories';
import { wrappable } from '../utils/componentUtils';

let RecordNavigationSection = React.createClass({

  propTypes: {
    collapsedCategories: React.PropTypes.array,
    onCategoryToggle: React.PropTypes.func,
    heading: React.PropTypes.node
  },

  mixins: [ PureRenderMixin ],

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

  render() {
    let { categoryWordsMap, collapsedCategories, heading } = this.props;
    let { navigationExpanded, navigationQuery } = this.state;
    let navigationQueryLower = navigationQuery.toLowerCase();

    let expandClassName = classnames({
      'wdk-RecordNavigationExpand fa': true,
      'fa-plus-square': !navigationExpanded,
      'fa-minus-square': navigationExpanded
    });

    return (
      <div className="wdk-RecordNavigationSection">
        <h2 className="wdk-RecordNavigationSectionHeader">
          <button className={expandClassName}
            onClick={() => void this.setState({ navigationExpanded: !navigationExpanded })}
          /> {heading}
        </h2>
        <div className="wdk-RecordNavigationSearch">
          <input
            className="wdk-RecordNavigationSearchInput"
            placeholder={'Search ' + heading}
            type="text"
            value={navigationQuery}
            onChange={e => {
              this.setState({
                navigationQuery: e.target.value,
                navigationExpanded: true
              });
            }}
          />
        </div>
        <div className="wdk-RecordNavigationCategories">
          <RecordNavigationSectionCategories
            record={this.props.record}
            recordClass={this.props.recordClass}
            categories={this.props.recordClass.categories}
            onCategoryToggle={this.props.onCategoryToggle}
            showChildren={navigationExpanded}
            isCollapsed={category => includes(collapsedCategories, category.name)}
            isVisible={category => includes(categoryWordsMap.get(category), navigationQueryLower)}
          />
        </div>
      </div>
    );
  }
});

export default wrappable(RecordNavigationSection);
