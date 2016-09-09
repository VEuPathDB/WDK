import {Component} from 'react';
import {findDOMNode} from 'react-dom';
import {debounce, throttle} from 'lodash';
import classnames from 'classnames';
import {wrappable} from '../utils/componentUtils';
import {postorderSeq} from '../utils/TreeUtils';
import {getId} from '../utils/CategoryUtils';
import RecordMainSection from './RecordMainSection';
import RecordHeading from './RecordHeading';
import RecordNavigationSection from './RecordNavigationSection';
import Sticky from './Sticky';

/**
 * Renders the main UI for the WDK Record page.
 */
class RecordUI extends Component {

  constructor(props) {
    super(props);
    // images whose load events we are listening to
    this._images = new Set();

    // flag to ingore calls to scroll changes
    this._ignoreActiveSectionChange = false;

    // bind event handlers
    this._updateActiveSection = debounce(this._updateActiveSection.bind(this), 100);
    this._scrollToActiveSection = throttle(this._scrollToActiveSection.bind(this), 250);
    this._unsetIgnoreActiveSectionChange = debounce(this._unsetIgnoreActiveSectionChange.bind(this), 300);
    this._handleImageLoad = this._handleImageLoad.bind(this);
    this._removeImageLoadHandlers = this._removeImageLoadHandlers.bind(this);
  }

  componentDidMount() {
    this._scrollToActiveSection();
    for (let image of findDOMNode(this).querySelectorAll('img')) {
      if (image.complete) {
        this._scrollToActiveSection();
      } else {
        this._images.add(image);
        image.addEventListener('load', this._handleImageLoad);
      }
    }
    window.addEventListener('wheel', this._removeImageLoadHandlers, { passive: true });
    window.addEventListener('scroll', this._updateActiveSection, { passive: true });
    window.addEventListener('resize', this._scrollToActiveSection, { passive: true });
  }

  componentDidUpdate(prevProps) {
    let navVisibilityChanged = prevProps.navigationVisible !== this.props.navigationVisible;
    let recordChanged = prevProps.record !== this.props.record;
    if (navVisibilityChanged) {
      this._scrollToActiveSection();
    }
    if (recordChanged) {
      this._scrollToActiveSection();
      for (let image of findDOMNode(this).querySelectorAll('img')) {
        if (!this._images.has(image)) {
          if (image.complete) {
            this._scrollToActiveSection();
          } else {
            this._images.add(image);
            image.addEventListener('load', this._handleImageLoad);
          }
        }
      }
    }
  }

  componentWillUnmount() {
    window.removeEventListener('scroll', this._updateActiveSection);
    window.removeEventListener('resize', this._scrollToActiveSection);
    window.removeEventListener('wheel', this._removeImageLoadHandlers);
  }

  _handleImageLoad(event) {
    this._images.delete(event.target);
    console.trace('SCROLLING AFTER IMG LOAD');
    this._scrollToActiveSection();
  }

  _removeImageLoadHandlers() {
    this._ignoreActiveSectionChange = false;
    for (let image of this._images) {
      image.removeEventListener('load', this._handleImageLoad);
    }
    this._images.clear();
    window.removeEventListener('wheel', this._removeImageLoadHandlers);
  }

  _updateActiveSection() {
    if (this._ignoreActiveSectionChange || this._images.size > 0) return;
    let activeElement = postorderSeq(this.props.categoryTree)
    .map(node => document.getElementById(getId(node)))
    .filter(el => el != null)
    .find(el => {
      let rect = el.getBoundingClientRect();
      return rect.top <= 50 && rect.bottom > 50;
    });
    let activeSection = activeElement && activeElement.id;
    if (activeSection != this.props.activeSection)
      this.props.updateActiveSection(activeElement == null ? null : activeElement.id);
  }

  _scrollToActiveSection() {
    this._setIgnoreActiveSectionChange();
    let domNode = document.getElementById(this.props.activeSection);
    if (domNode != null) {
      domNode.scrollIntoView(true);
    }
    this._unsetIgnoreActiveSectionChange();
  }

  _setIgnoreActiveSectionChange() {
    this._ignoreActiveSectionChange = true;
  }

  _unsetIgnoreActiveSectionChange() {
    this._ignoreActiveSectionChange = false;
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
      <div className={classNames}>
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
          <RecordMainSection
            record={this.props.record}
            recordClass={this.props.recordClass}
            categories={this.props.categoryTree.children}
            collapsedSections={this.props.collapsedSections}
            onSectionToggle={this.props.toggleSection}
          />
        </div>
      </div>
    )
  }
}

export default wrappable(RecordUI);
