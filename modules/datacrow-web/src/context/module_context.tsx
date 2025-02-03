import { createContext, useContext, useState } from "react";
import type { Module } from "../services/datacrow_api";

export interface ModuleType {
	selectedModule: Module;
	mainModule: Module;
	switchModule: (newSelectedModule: Module, newMainModule: Module) => void;
	filter: string | undefined;
	setFilter: React.Dispatch<
        React.SetStateAction<string | undefined>
    >;
}

export const ModuleContext = createContext<ModuleType>(null!);

export function useModule() {
	return useContext(ModuleContext);
}

export function ModuleProvider({ children }: { children: React.ReactNode }) {
	let [selectedModule, setSelectedModule] = useState<any>(null);
	let [mainModule, setMainModule] = useState<any>(null);
	let [filter, setFilter] = useState<string | undefined>(undefined);
	
	let switchModule = (newSelectedModule: Module, newMainModule: Module) => {
		{	
            setFilter("");
			setSelectedModule(newSelectedModule);
		 	setMainModule(newMainModule);
		}
	};
	
	let value = { selectedModule, mainModule, switchModule, filter, setFilter};
	
	return <ModuleContext.Provider value={value}>{children}</ModuleContext.Provider>;
}