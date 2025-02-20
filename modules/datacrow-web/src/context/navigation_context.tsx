import { createContext, useContext, useState } from "react";

export interface NavigationType {
    addPage: (address: string, label: string, parameters: object) => void;
    reset: () => void;
    pages: NavigationPage[] | undefined;
    previousPage: () => NavigationPage | undefined;
}

export interface NavigationPage {
    address: string;
    label: string;
    parameters: object
}

export interface NavigationParameters {
    itemID: string;
}

export const NavigationContext = createContext<NavigationType>(null!);

export function useItemNavigation() {
	return useContext(NavigationContext);
}

export function NavigationProvider({ children }: { children: React.ReactNode }) {
	
	let [pages, setPages] = useState<NavigationPage[] | undefined>();

    let previousPage = () => {
        if (pages && pages.length > 2)
            return pages[pages.length - 2];
        else
            return undefined;
    }

    let addPage = (address: string, label: string, parameters: object) => {
        let currentPages: NavigationPage[] = [];
        currentPages = pages ? pages.splice(0) : currentPages;

        let page: NavigationPage = {
            address, label, parameters
        }
        
        currentPages.push(page);
        setPages(currentPages);
    }

    let reset = () => {
        if (pages) {
            setPages(undefined);
        }
    }
	
	let value = { addPage, reset, pages, previousPage};
	
	return (
	   <NavigationContext.Provider value={value}>
	       {children}
	   </NavigationContext.Provider>);
}