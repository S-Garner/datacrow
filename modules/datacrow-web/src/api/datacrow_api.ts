const baseUrl = 'http://localhost:8080/datacrow/api/';

export interface Module {
	index: number;
	name: string;
	icon: string;
	children: Module[];
}

export interface Item {
	id: number;
	name: string;
	scaledImageUrl: string;
	fields: FieldValue[];
}

export interface FieldValue {
	field: Field;
	value: object;
}

export interface Field {
	type: number;
	index: number;
	moduleIdx: number;
	referencedModuleIdx: number;
	maximumLength: number;
}

export async function fetchModules(): Promise<Module[]> {
	const response = await fetch(baseUrl + 'modules/');
	const result = await response.json();
	return result;
}

export async function fetchItems(moduleIdx: number): Promise<Item[]> {
	const response = await fetch(baseUrl + 'items/' + moduleIdx);
	const result = await response.json();
	return result;
}
