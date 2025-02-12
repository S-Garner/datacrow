import axios from 'axios';
import type { Translation } from '../context/translation_context';

const baseUrl = 'http://192.168.178.244:8081/datacrow-api/api/';

const instance = axios.create({
    baseURL: baseUrl
});

instance.interceptors.request.use(
    function (config) {
        const token = localStorage.getItem("token");
        
        if (token)
            config.headers["authorization"] = token;

        return config;
    }
);

export interface LoginCallBack { (myArgument: User | null): void }

export interface Attachment {
    objectID: string;
    url: string;
    name: string;
    size: bigint;
    created: Date;
    displayName: string;
}

export interface Picture {
    url: string;
    thumbUrl: string;
    objectID: string;
    filename: string;
    order: number;
}

export interface Module {
	index: number;
	name: string;
	icon: string;
	references: Module[];
	isTop: boolean;
	hasChild: boolean;
	child: Module;
}

export interface References {
    moduleIdx: number;
    items: Reference[];
}

export interface Settings {
    maxUploadAttachmentSize: number;
}

export interface User {
    username: string;
    token: string;
    settings: Settings;
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
	hidden: boolean;
}

export interface Reference {
    id: string;
    name: string;
    iconUrl: string;
}

export async function downloadAttachment(itemID: string, name: string): Promise<Blob> {
    const response = await instance.get(baseUrl + 'attachments/download/' + itemID + '/' + name, {responseType: 'blob'});
    return await response.data;
}

export async function deleteAttachment(itemID: string, name: string): Promise<Attachment[]> {
    const response = await instance.delete(baseUrl + 'attachments/' + itemID + '/' + name);
    return response.data;
}

export async function saveAttachment(data: Object, itemID: string, fileName: string): Promise<Attachment[]> {
    const response = await instance.post(baseUrl + 'attachments', data, {
        headers: {
            'Content-Type': 'multipart/form-data',
            'itemID': itemID,
            'fileName': fileName
        },
    });
    return response.data;
}

export async function deletePicture(itemID: string, number: number): Promise<Picture[]> {
    const response = await instance.delete(baseUrl + 'pictures/' + itemID + '/' + number);
    return response.data;
}

export async function movePictureUp(itemID: string, number: number): Promise<Picture[]> {
    const response = await instance.get(baseUrl + 'pictures/moveup/' + itemID + '/' + number);
    return response.data;
}

export async function movePictureDown(itemID: string, number: number): Promise<Picture[]> {
    const response = await instance.get(baseUrl + 'pictures/movedown/' + itemID + '/' + number);
    return response.data;
}

export async function savePicture(data: Object, itemID: string): Promise<Picture[]> {
    const response = await instance.post(baseUrl + 'pictures', data, {
        headers: {
            'Content-Type': 'multipart/form-data',
            'itemID': itemID
        }
    });
    return response.data;
}

export async function saveItem(moduleIdx: number, data: Map<string, Object>): Promise<Response> {
    return await instance.post(baseUrl + 'item', {payload: data, module: moduleIdx});
}

export async function login(username: string, password: string): Promise<User | null> {
    const response = await instance.get(baseUrl + 'login/' + username, {
        headers: {
            'password': password
        }
    });
    
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

export async function fetchAttachments(itemID: string): Promise<Attachment[]> {
    const response = await instance.get(baseUrl + 'attachments/' + itemID);
    return response.data;
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