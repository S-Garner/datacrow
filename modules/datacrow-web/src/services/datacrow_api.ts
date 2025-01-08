const baseUrl = 'http://192.168.178.244:8080/datacrow/api/';

export interface Module {
	index: number;
	name: string;
	icon: string;
	children: Module[];
	fields: Field[];
	isTop: boolean;
}

export interface References {
    moduleIdx: number;
    items: Reference[];
}

export interface User {
    username: string;
    token: string;
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
	readOnly: boolean;
	moduleIdx: number;
	referencedModuleIdx: number;
	maximumLength: number;
	label: string;
}

export interface Reference {
    id: string;
    name: string;
    iconUrl: string;
}

export async function login(username: string, password: string): Promise<User> {
    const response = await fetch(baseUrl + 'login/' + username + "/" + password);
    const result = await response.json();
    return result;
}

export async function fetchReferences(moduleIdx: number): Promise<References[]> {
    const response = await fetch(baseUrl + 'references/' + moduleIdx);
    const result = await response.json();
    return result;
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