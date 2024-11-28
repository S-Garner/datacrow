import { createContext, useContext, useState } from "react";
import type { Module } from "src/services/datacrow_api";

export interface ModuleType {
	module: Module;
	switchModule: (newModule: Module) => void;
}

export const ModuleContext = createContext<ModuleType>(null!);

export function useModule() {
	return useContext(ModuleContext);
}

export function ModuleProvider({ children }: { children: React.ReactNode }) {
	let [module, setModule] = useState<any>(null);
	
	
	let switchModule = (newModule: Module) => {
		{setModule(newModule)}
	};
	
	let value = { module, switchModule};
	
	return <ModuleContext.Provider value={value}>{children}</ModuleContext.Provider>;
}