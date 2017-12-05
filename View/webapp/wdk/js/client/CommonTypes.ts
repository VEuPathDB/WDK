import WdkStore from "./stores/WdkStore";
import WdkDispatcher, { Action } from "./dispatcher/Dispatcher";
import GlobalDataStore from "./stores/GlobalDataStore";
import { ActionCreator, ActionCreatorResult, ActionCreatorServices } from './utils/ActionCreatorUtils';
import { RouteComponentProps } from "react-router";
import { History } from 'history';
import AbstractViewController from './controllers/AbstractViewController';
import { ComponentType } from "react";


export interface StoreConstructor<T extends WdkStore> {
  new(dispatcher: WdkDispatcher, channel: string, globalDataStore: GlobalDataStore, services: ActionCreatorServices): T;
}

export interface DispatchAction {
  (action: ActionCreatorResult<Action>): any;
}

export interface MakeDispatchAction {
  (channel: string): DispatchAction
}

export interface Constructor<T> {
  new(...args: any[]): T;
}

export interface Container<T> {
  get(Class: Constructor<T>): T;
}

export interface ViewControllerProps<Store> {
  stores: Container<Store>;
  makeDispatchAction: MakeDispatchAction;
}

export type AbstractViewControllerClass = typeof AbstractViewController;

export interface RouteSpec {
  path: string;
  component: ComponentType<ViewControllerProps<WdkStore>>
}
