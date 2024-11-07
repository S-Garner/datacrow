import { createContext, useContext } from "react";

export interface CurrentModuleContextType {
	index: number;
}

export const CurrentModuleContext = createContext<CurrentModuleContextType | any>(null);

export const useCurrentModule = () => {
	const currentModuleContext = useContext(CurrentModuleContext);
	return currentModuleContext;
};