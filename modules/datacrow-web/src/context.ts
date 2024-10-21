import { createContext, useContext } from "react";
import type { Module } from "./api/datacrow_api";

// We explicitly allow `undefined` as a potential value here
// to tell the compiler we plan to deal with it.
export const ModuleContext = createContext<Module | undefined>(undefined)

export function useModuleContext() {
  let context = useContext(ModuleContext)
  // If context is undefined, we know we used RadioGroupItem
  // outside of our provider so we can throw a more helpful
  // error!
  if (context === undefined) {
    throw Error('Dunno yet')
  }

  // Because of TypeScript's type narrowing, if we make it past
  // the error the compiler knows that context is always defined
  // at this point, so we don't need to do any conditional
  // checking on its values when we use this hook!
  return context
}
