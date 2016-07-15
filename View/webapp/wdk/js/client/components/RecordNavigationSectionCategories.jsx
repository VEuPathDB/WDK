import {wrappable, PureComponent} from '../utils/componentUtils';
import {getId, getLabel} from '../utils/CategoryUtils';
import * as t from '../utils/TreeUtils';
import * as i from '../utils/IterableUtils';
import RecordNavigationItem from './RecordNavigationItem';
import Tree from './Tree';

/**
 * Handle scroll events to mark the active category in the nav panel.
 */
class RecordNavigationSectionCategories extends PureComponent {

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
    if (this.props.collapsedSections !== previousProps.collapsedSections ||
        this.props.showChildren !== previousProps.showChildren ) {
      this.setActiveCategory();
    }
  }

  // If showChildren is true, iterate postorder to get the first left-most child
  // that is on-screen. Otherwise, we will only iterate top-level categories.
  setActiveCategory() {
    let categories = this.props.showChildren
      ? t.preorderSeq({ children: this.props.categories })
        .filter(node => node.children.length > 0)
      : i.seq(this.props.categories);

    let activeCategory = categories.findLast(node => {
      let id = getId(node);
      let domNode = document.getElementById(id);
      if (domNode == null) return;
      let rect = domNode.getBoundingClientRect();
      return rect.top <= 70;
    });

    this.setState({ activeCategory });
  }

  render() {
    return (
      <Tree
        tree={this.props.categories}
        id={c => getLabel(c)}
        childNodes={c => c.children}
        node={RecordNavigationItem}
        showChildren={this.props.showChildren}
        onSectionToggle={this.props.onSectionToggle}
        isCollapsed={this.props.isCollapsed}
        isVisible={this.props.isVisible}
        activeCategory={this.state.activeCategory}
      />
    );
  }

}

export default wrappable(RecordNavigationSectionCategories);
