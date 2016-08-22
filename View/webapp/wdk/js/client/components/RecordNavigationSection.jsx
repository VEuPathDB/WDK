import React from 'react';
import classnames from 'classnames';
import {includes, memoize, throttle} from 'lodash';
import { seq } from '../utils/IterableUtils';
import { preorderSeq, postorderSeq } from '../utils/TreeUtils';
import { wrappable, PureComponent } from '../utils/componentUtils';
import { getPropertyValues, nodeHasProperty } from '../utils/OntologyUtils';
import { getId, getDisplayName } from '../utils/CategoryUtils';
import { parseSearchQueryString, areTermsInString } from '../utils/SearchUtils';
import RecordNavigationItem from './RecordNavigationItem';
import Tree from './Tree';
import RealTimeSearchBox from './RealTimeSearchBox';

/** Navigation panel for record page */
class RecordNavigationSection extends PureComponent {

  constructor(props) {
    super(props);
    this.handleSearchTermChange = this.handleSearchTermChange.bind(this);
    this.setActiveCategory = throttle(this.setActiveCategory.bind(this), 300);
    this.state = { activeCategory: null };
  }

  componentDidMount() {
    window.addEventListener('scroll', this.setActiveCategory, { passive: true });
  }

  componentWillUnmount() {
    window.removeEventListener('scroll', this.setActiveCategory);
  }

  componentDidUpdate(previousProps) {
    if (this.props.collapsedSections !== previousProps.collapsedSections ||
        this.props.showChildren !== previousProps.showChildren ) {
      this.setActiveCategory();
    }
  }

  // If showChildren is true, iterate postorder to get the first left-most child
  // that is on-screen. Otherwise, we will only iterate top-level categories.
  setActiveCategory() {
    let categories = this.props.showChildren
      ? preorderSeq(this.props.categoryTree)
        .filter(node => node.children.length > 0)
      : seq(this.props.categoryTree.children);

    let activeCategory = categories.findLast(node => {
      let id = getId(node);
      let domNode = document.getElementById(id);
      if (domNode == null) return;
      let rect = domNode.getBoundingClientRect();
      return rect.top <= 70;
    });

    this.setState({ activeCategory });
  }

  handleSearchTermChange(term) {
    this.props.onNavigationQueryChange(term);
    this.props.onNavigationSubcategoryVisibilityChange(true);
  }

  render() {
    let { collapsedSections, heading, navigationQuery, navigationSubcategoriesExpanded } = this.props;
    let searchQueryTerms = parseSearchQueryString(navigationQuery);
    let categoryWordsMap = makeCategoryWordsMap(this.props.categoryTree);
    let expandClassName = classnames({
      'wdk-RecordNavigationExpand fa': true,
      'fa-plus-square': !navigationSubcategoriesExpanded,
      'fa-minus-square': navigationSubcategoriesExpanded
    });

    return (
      <div className="wdk-RecordNavigationSection">
        <h2 className="wdk-RecordNavigationSectionHeader">
          <button type="button" className={expandClassName}
            onClick={() => {
              this.props.onNavigationSubcategoryVisibilityChange(
                !this.props.navigationSubcategoriesExpanded);
            }}
          /> {heading}
        </h2>
        <RealTimeSearchBox
          placeholderText={'Search section names...'}
          initialSearchTerm={navigationQuery}
          onSearchTermChange={this.handleSearchTermChange}
          delayMs={100}
        />
        <div className="wdk-RecordNavigationCategories">
          <Tree
            tree={this.props.categoryTree.children}
            id={c => getId(c)}
            childNodes={c => c.children}
            node={RecordNavigationItem}
            showChildren={navigationSubcategoriesExpanded}
            onSectionToggle={this.props.onSectionToggle}
            isCollapsed={category => includes(collapsedSections, getId(category))}
            isVisible={category => areTermsInString(searchQueryTerms, categoryWordsMap.get(category.properties))}
            activeCategory={this.state.activeCategory}
          />
        </div>
      </div>
    );
  }
}

RecordNavigationSection.propTypes = {
  collapsedSections: React.PropTypes.array,
  onSectionToggle: React.PropTypes.func,
  heading: React.PropTypes.node
};

RecordNavigationSection.defaultProps = {
  onSectionToggle: function noop() {},
  heading: 'Contents'
};

export default wrappable(RecordNavigationSection);

let makeCategoryWordsMap = memoize((root) =>
  postorderSeq(root).reduce((map, node) => {
    let words = [];

    // add current node's displayName and description
    words.push(
      getDisplayName(node),
      ...getPropertyValues('hasDefinition', node),
      ...getPropertyValues('hasExactSynonym', node),
      ...getPropertyValues('hasNarrowSynonym', node)
    );

    // add displayName and desription of attribute or table
    if (nodeHasProperty('targetType', 'attribute', node) || nodeHasProperty('targetType', 'table', node)) {
      words.push(node.wdkReference.displayName, node.wdkReference.description);
    }

    // add words from any children
    for (let child of node.children) {
      words.push(map.get(child.properties));
    }

    return map.set(node.properties, words.join('\0').toLowerCase());
  }, new Map));
