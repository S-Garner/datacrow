import { createContext, useContext, useState } from "react";
import type { Field, FieldSetting, Module } from "../services/datacrow_api";

export interface ModuleType {
	selectedModule: Module;
	mainModule: Module;
	switchModule: (newSelectedModule: Module, newMainModule: Module) => void;
	getFields: (settings: FieldSetting[]) => Field[];
    getField: (fieldIdx: number) => Field;
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
	
	let getField = (fieldIdx: number) => {

        let field = undefined;
        
        if (selectedModule) {
            let fields = selectedModule.fields;
            for (let i = 0; i < fields.length; i++) {
                if (fields[i].index === fieldIdx)
                    field = fields[i];
            }
        }
        
        return field;
    };
    
    let getFields = (settings: FieldSetting[]) => {

        let fields = new Array<Field>();
        
        if (selectedModule) {
            for (let idx = 0; idx < settings.length; idx++) {
                if (settings[idx].enabled) {
                    let field = getField(settings[idx].fieldIdx); // get the field based on the index
                    
                    if (!field.hidden) // check if the field is not hidden
                        fields.push(field);
                }
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
	
	let value = { selectedModule, mainModule, switchModule, getFields, getField, filter, setFilter};
	
	return (
	   <ModuleContext.Provider value={value}>
	       {children}
	   </ModuleContext.Provider>);
}