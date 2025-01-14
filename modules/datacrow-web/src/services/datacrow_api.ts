import axios from 'axios';

const baseUrl = 'http://192.168.178.244:8080/datacrow/api/';

const instance = axios.create({
    baseURL: baseUrl
});

instance.interceptors.request.use(
    function (config) {
        const token = localStorage.getItem("token");
        
        config.headers["Content-type"] = 'application/json';
        
        if (token)
            config.headers["authorization"] = token;

        return config;
    }
);

export interface LoginCallBack { (myArgument: User | null): void }

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
	value: Object;
}

export interface Field {
	type: number;
	index: number;
	readOnly: boolean;
	moduleIdx: number;
	referencedModuleIdx: number;
	maximumLength: number;
	label: string;
	required: boolean;
}

export interface Reference {
    id: string;
    name: string;
    iconUrl: string;
}

export async function login(username: string, password: string): Promise<User | null> {
    const response = await instance.get(baseUrl + 'login/' + username + "/" + password);
    
    if (response.status === 200) {
        const result = await response.data;
        return result;
    } else {
        return null;
    }
}

export async function fetchReferences(moduleIdx: number): Promise<References[]> {
    const response = await instance.get(baseUrl + 'references/' + moduleIdx);
    return response.data;
}

export async function fetchModules(): Promise<Module[]> {
    const response = await instance.get('modules/');
    return response.data;
}

export async function fetchItem(moduleIdx: number, itemId: string): Promise<Item> {
	const response = await instance.get(baseUrl + 'item/' + moduleIdx + '/' + itemId);
	return response.data;
}

export async function fetchItems(moduleIdx: number): Promise<Item[]> {
	const response = await instance.get(baseUrl + 'items/' + moduleIdx);
	return response.data;
}

export async function searchItems(moduleIdx: number, searchTerm: String): Promise<Item[]> {
	const response = await instance.get(baseUrl + 'items/' + moduleIdx + '/' + searchTerm);
	return response.data;
}