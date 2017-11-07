import React from 'react';
import { parse } from 'querystring';
import AbstractViewController from "./AbstractViewController";
import Page from '../components/Page';
import { ViewControllerProps } from "../CommonTypes";
import WdkStore, { BaseState } from "../stores/WdkStore";
import { RouteComponentProps } from "react-router";
import { ActionCreator } from "../ActionCreator";
import { Action } from "../dispatcher/Dispatcher";

export type PageControllerProps<Store> = ViewControllerProps<Store> & RouteComponentProps<any>;

/**
 * A ViewController that is intended to render a UI on an entire screen.
 */
export default abstract class AbstractPageController<
  State extends {} = BaseState,
  Store extends WdkStore = WdkStore,
  ActionCreators extends Record<any,ActionCreator<Action>> = {}
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
    return parse(this.props.location.search.slice(1));
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
