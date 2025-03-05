import { createContext, useContext, useState } from "react";
import type { Field, FieldSetting, Module } from "../services/datacrow_api";

export interface ModuleType {
    setModules: React.Dispatch<React.SetStateAction<Module[] | undefined>>;
    modules: Module[] | undefined;
    getModule: (moduleIdx: number) => Module | undefined;
	selectedModule: Module;
	mainModule: Module;
	switchModule: (newSelectedModule: Module, newMainModule: Module) => void;
	getFields: (moduleIdx: number, settings: FieldSetting[]) => Field[] | undefined;
    getField: (moduleIdx: number, fieldIdx: number) => Field | undefined;
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
	let [modules, setModules] = useState<Module[]>();
	let [filter, setFilter] = useState<string | undefined>(undefined);
	
	let getModule = (moduleIdx: number) => {
        
        let module = undefined;
        
        if (modules) {
            for (let i = 0; i < modules.length; i++) {
                if (modules[i].index === moduleIdx)
                    module = modules[i];
            } 
        }
        
        return module;
    }
	
	let getField = (moduleIdx: number, fieldIdx: number) => {

        let field = undefined;
        let module = getModule(moduleIdx);
        
        if (module) {
            let fields = module.fields;
            for (let i = 0; i < fields.length; i++) {
                if (fields[i].index === fieldIdx)
                    field = fields[i];
            }
        }
        
        return field;
    };
    
    let getFields = (moduleIdx: number, settings: FieldSetting[]) => {

        let fields = new Array<Field>();
        
        if (selectedModule) {
            for (let idx = 0; idx < settings.length; idx++) {
                let field = getField(moduleIdx, settings[idx].fieldIdx); // get the field based on the index
                
                if (field) // check if the field is not hidden
                    fields.push(field);
            }
        }
        
        return fields;
    };    
	
	let switchModule = (newSelectedModule: Module, newMainModule: Module) => {
		{	
            setFilter("");
			setSelectedModule(newSelectedModule);
		 	setMainModule(newMainModule);
		}
	};
	
	let value = { setModules, modules, getModule, selectedModule, mainModule, switchModule, getFields, getField, filter, setFilter};
	
	return (
	   <ModuleContext.Provider value={value}>
	       {children}
	   </ModuleContext.Provider>);
}