import {Component} from 'react';
import {debounce, throttle} from 'lodash';
import classnames from 'classnames';
import {wrappable} from '../utils/componentUtils';
import {postorderSeq} from '../utils/TreeUtils';
import {getId} from '../utils/CategoryUtils';
import Main from './Main';
import Record from './Record';
import RecordHeading from './RecordHeading';
import RecordNavigationSection from './RecordNavigationSection';
import Sticky from './Sticky';

/**
 * Renders the main UI for the WDK Record page.
 */
class RecordUI extends Component {

  constructor(props) {
    super(props);
    this._ignoreScrollEvent = false;
    this._updateActiveSection = throttle(this._updateActiveSection.bind(this), 250);
    this._scrollToActiveSection = throttle(this._scrollToActiveSection.bind(this), 250);
    this._unsetIgnoreScrollEvent = debounce(this._unsetIgnoreScrollEvent.bind(this), 300);
  }

  componentDidMount() {
    let { hash } = window.location;
    let target = document.getElementById(hash.slice(1));
    if (target != null) {
      target.scrollIntoView();
    }
    window.addEventListener('scroll', this._updateActiveSection);
    window.addEventListener('resize', this._scrollToActiveSection);
  }

  componentDidUpdate(prevProps) {
    if (prevProps.navigationVisible !== this.props.navigationVisible) {
      this._scrollToActiveSection();
    }
  }

  componentWillUnmount() {
    window.removeEventListener('scroll', this._updateActiveSection);
    window.removeEventListener('resize', this._scrollToActiveSection);
  }

  _updateActiveSection() {
    if (this._ignoreScrollEvent) return;
    let activeElement = postorderSeq(this.props.categoryTree)
    .map(node => document.getElementById(getId(node)))
    .filter(el => el != null)
    .find(el => {
      let rect = el.getBoundingClientRect();
      return rect.top <= 50 && rect.bottom > 50;
    });
    if (activeElement != null) {
      this.props.updateActiveSection(activeElement.id);
    }
  }

  _scrollToActiveSection() {
    this._setIgnoreScrollEvent();
    let domNode = document.getElementById(this.props.activeSection);
    if (domNode != null) {
      domNode.scrollIntoView(true);
    }
    this._unsetIgnoreScrollEvent();
  }

  _setIgnoreScrollEvent() {
    this._ignoreScrollEvent = true;
  }

  _unsetIgnoreScrollEvent() {
    this._ignoreScrollEvent = false;
  }

  render() {
    let classNames = classnames(
      'wdk-RecordContainer',
      'wdk-RecordContainer__' + this.props.recordClass.name,
      {
        'wdk-RecordContainer__withSidebar': this.props.navigationVisible      }
    );

    let sidebarIconClass = classnames({
      'fa fa-lg': true,
      'fa-angle-double-down': !this.props.navigationVisible,
      'fa-angle-double-up': this.props.navigationVisible
    });

    return (
      <Main className={classNames}>
        <RecordHeading
          record={this.props.record}
          recordClass={this.props.recordClass}
          headerActions={this.props.headerActions}
        />
        <Sticky className="wdk-RecordSidebar" fixedClassName="wdk-RecordSidebar__fixed">
          <button type="button" className="wdk-RecordSidebarToggle"
            onClick={() => {
              if (!this.props.navigationVisible) window.scrollTo(0, window.scrollY);
              this.props.updateNavigationVisibility(!this.props.navigationVisible);
            }}
          >
            {this.props.navigationVisible ? '' : 'Show Contents '}
            <i className={sidebarIconClass}
              title={this.props.navigationVisible ? 'Close sidebar' : 'Open sidebar'}/>
          </button>
          <RecordNavigationSection
            record={this.props.record}
            recordClass={this.props.recordClass}
            categoryTree={this.props.categoryTree}
            collapsedSections={this.props.collapsedSections}
            activeSection={this.props.activeSection}
            navigationQuery={this.props.navigationQuery}
            navigationExpanded={this.props.navigationExpanded}
            navigationSubcategoriesExpanded={this.props.navigationSubcategoriesExpanded}
            onSectionToggle={this.props.toggleSection}
            onNavigationVisibilityChange={this.props.updateNavigationVisibility}
            onNavigationSubcategoryVisibilityChange={this.props.updateNavigationSubcategoryVisibility}
            onNavigationQueryChange={this.props.updateNavigationQuery}
          />
        </Sticky>
        <div className="wdk-RecordMain">
          <div className="wdk-RecordMainSectionFieldToggles">
            <button type="button" title="Expand all content" className="wdk-Link"
              onClick={this.props.updateAllFieldVisibility.bind(null, true)}>Expand All</button>
            {' | '}
            <button type="button" title="Collapse all content" className="wdk-Link"
              onClick={this.props.updateAllFieldVisibility.bind(null, false)}>Collapse All</button>
          </div>
          <Record
            record={this.props.record}
            recordClass={this.props.recordClass}
            categoryTree={this.props.categoryTree}
            collapsedSections={this.props.collapsedSections}
            onSectionToggle={this.props.toggleSection}
          />
        </div>
      </Main>
    )
  }
}

export default wrappable(RecordUI);
