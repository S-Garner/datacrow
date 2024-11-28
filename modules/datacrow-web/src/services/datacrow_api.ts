const baseUrl = 'http://192.168.178.244:8080/datacrow/api/';

export interface Module {
	index: number;
	name: string;
	icon: string;
	children: Module[];
	fields: Field[];
	isTop: boolean;
}

export interface Item {
	id: string;
	moduleIdx: number;
	name: string;
	scaledImageUrl: string;
	imageUrl: string;
	fields: FieldValue[];
}

export interface FieldValue {
	field: Field;
	value: string;
}

export interface Field {
	type: number;
	index: number;
	moduleIdx: number;
	referencedModuleIdx: number;
	maximumLength: number;
	label: string;
}

export async function fetchModules(): Promise<Module[]> {
	const response = await fetch(baseUrl + 'modules/');
	const result = await response.json();
	return result;
}

export async function fetchItem(moduleIdx: number, itemId: string): Promise<Item> {
	const response = await fetch(baseUrl + 'item/' + moduleIdx + '/' + itemId);
	const result = await response.json();
	return result;
}

export async function fetchItems(moduleIdx: number): Promise<Item[]> {
	const response = await fetch(baseUrl + 'items/' + moduleIdx);
	const result = await response.json();
	return result;
}

export async function searchItems(moduleIdx: number, searchTerm: String): Promise<Item[]> {
	const response = await fetch(baseUrl + 'items/' + moduleIdx + '/' + searchTerm);
	const result = await response.json();
	return result;
}