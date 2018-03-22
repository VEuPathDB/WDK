import { mapValues } from 'lodash';
import { parse } from 'querystring';
import React from 'react';
import { RouteComponentProps } from 'react-router';

import Page from 'Components/Layout/Page';
import { ViewControllerProps } from 'Core/CommonTypes';
import AbstractViewController from 'Core/Controllers/AbstractViewController';
import WdkStore, { BaseState } from 'Core/State/Stores/WdkStore';
import { Action, ActionCreatorRecord } from 'Utils/ActionCreatorUtils';

export type PageControllerProps<Store> = ViewControllerProps<Store> & RouteComponentProps<any>;

/**
 * A ViewController that is intended to render a UI on an entire screen.
 */
export default abstract class AbstractPageController <
  State extends {} = BaseState,
  Store extends WdkStore = WdkStore,
  ActionCreators extends ActionCreatorRecord<Action> = {}
> extends AbstractViewController<State, Store, ActionCreators> {

  props: PageControllerProps<Store>;

  /*--------------- Methods to override to display content ---------------*/

  /**
   * Returns the title of this page
   */
  getTitle(): string {
    return "WDK";
  }

  getQueryParams() {
    return mapValues(parse(this.props.location.search.slice(1)), String);
  }

  setDocumentTitle(): void {
    if (this.isRenderDataLoadError()) {
      document.title = "Error";
    }
    else if (this.isRenderDataNotFound()) {
      document.title = "Page not found";
    }
    else if (this.isRenderDataPermissionDenied()) {
      document.title = "Permission denied";
    }
    else if (!this.isRenderDataLoaded()) {
      document.title = "Loading...";
    }
    else {
      document.title = this.getTitle();
    }
  }

  componentDidMount(): void {
    super.componentDidMount()
    this.setDocumentTitle();
  }

  componentDidUpdate(): void {
    this.setDocumentTitle();
  }

  render() {
    return (
      <Page>{super.render()}</Page>
    );
  }

}
