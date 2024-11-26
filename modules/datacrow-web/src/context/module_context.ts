import { createContext, useContext } from "react";
import type { Module } from "src/services/datacrow_api";

export interface CurrentModuleContextType {
	module: Module;
}

export const CurrentModuleContext = createContext<CurrentModuleContextType | any>(null);

export const useCurrentModule = () => {
	const currentModuleContext = useContext(CurrentModuleContext);
	return currentModuleContext;
};