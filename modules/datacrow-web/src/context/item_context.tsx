import { createContext, useContext } from "react";

export interface ItemContextType {
	itemID: string;
}

export const ItemContext = createContext<ItemContextType | any>(null);

export const useItem = () => {
	const itemContext = useContext(ItemContext);
	return itemContext;
};