import axios from 'axios';
import type { Translation } from '../context/translation_context';

const baseUrl = 'http://192.168.178.244:8081/datacrow-api/api/';

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

export interface Picture {
    url: string;
    thumbUrl: string;
    objectID: string;
    filename: string;
}

export interface Module {
	index: number;
	name: string;
	icon: string;
	references: Module[];
	fields: Field[];
	isTop: boolean;
	hasChild: boolean;
	child: Module;
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

export async function fetchResources(lang: string | undefined): Promise<Translation> {
    
    if (lang === undefined)
        lang = "English";
    
    const response = await instance.get(baseUrl + 'resources/' + lang);

    // contruct a valid Map for easy object    
    let resources = new Map<string, string>();
    Object.keys(response.data).map((res) => {
        resources.set(res, response.data[res]);
    });
    
    return resources;
}

export async function fetchPictures(itemID: string): Promise<Picture[]> {
    const response = await instance.get(baseUrl + 'pictures/' + itemID);
    return response.data;
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

export async function fetchChildren(moduleIdx: number, itemId: string): Promise<Item[]> {
    const response = await instance.get(baseUrl + 'children/' + moduleIdx + '/' +  itemId);
    return response.data;
}

export async function fetchItems(moduleIdx: number, searchTerm: String | undefined): Promise<Item[]> {
    let url = baseUrl + 'items/' + moduleIdx;
    
    if (searchTerm)
        url += "/" + searchTerm;
    
	const response = await instance.get(url);
	return response.data;
}

export async function searchItems(moduleIdx: number, searchTerm: String): Promise<Item[]> {
	const response = await instance.get(baseUrl + 'items/' + moduleIdx + '/' + searchTerm);
	return response.data;
}