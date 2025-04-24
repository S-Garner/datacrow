import axios from 'axios';
import type { Translation } from '../context/translation_context';
import type { UniqueIdentifier } from '@dnd-kit/core';

let instance = axios.create({});
let baseUrl = "";

function checkInstance() {
    if (instance.getUri() === undefined || baseUrl.length == 0) {
        
        baseUrl = (globalThis as any).apiUrl;
        
        instance = axios.create({
            baseURL: (globalThis as any).apiUrl
        });

        instance.interceptors.request.use(
            function(config) {
                const token = localStorage.getItem("token");

                if (token)
                    config.headers["authorization"] = token;

                return config;
            }
        );
    }
}

export interface LoginCallBack { (user: User | null): void }

export interface Config {
    apiUrl: string;
}

export interface Attachment {
    objectID: string;
    url: string;
    name: string;
    size: bigint;
    created: Date;
    displayName: string;
}

export interface RelatedItem {
    id: string;
    moduleIdx: number;
    name: string;
    scaledImageUrl: string;
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
	main: boolean;
	hasChild: boolean;
	child: Module;
	fields: Field[];
	itemName: string;
	itemNamePlural: string;
	isAbstract: boolean;
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
    admin: boolean;
    settings: Settings;
    canEditAttachments: boolean;
    canEditPictures: boolean;
}

export interface Item {
	id: string;
	moduleIdx: number;
	name: string;
	scaledImageUrl: string;
	imageUrl: string;
	fields: FieldValue[];
	pictures: Picture[];
	relatedItems: RelatedItem[];
	parentID: string;
}

export interface FieldValue {
	field: Field;
	value: Object;
}

export interface FieldSetting {
    id: UniqueIdentifier;
    fieldIdx: number;
    labelKey: string;
    order: number;
}

export interface OverviewFieldSetting {
    id: UniqueIdentifier;
    fieldIdx: number;
    labelKey: string;
    order: number;
    enabled: boolean;
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
    checkInstance();
    const response = await instance.get((globalThis as any).apiUrl + 'attachments/download/' + itemID + '/' + name, {responseType: 'blob'});
    return await response.data;
}

export async function deleteAttachment(itemID: string, name: string): Promise<Attachment[]> {
    checkInstance();
    const response = await instance.delete((globalThis as any).apiUrl + 'attachments/' + itemID + '/' + name);
    return response.data;
}

export async function saveAttachment(data: Object, itemID: string, fileName: string): Promise<Attachment[]> {
    checkInstance();
    const response = await instance.post((globalThis as any).apiUrl + 'attachments', data, {
        headers: {
            'Content-Type': 'multipart/form-data',
            'itemID': itemID,
            'fileName': fileName
        },
    });
    return response.data;
}

export async function deletePicture(itemID: string, number: number): Promise<Picture[]> {
    checkInstance();
    const response = await instance.delete((globalThis as any).apiUrl + 'pictures/' + itemID + '/' + number);
    return response.data;
}

export async function movePictureUp(itemID: string, number: number): Promise<Picture[]> {
    checkInstance();
    const response = await instance.get((globalThis as any).apiUrl + 'pictures/moveup/' + itemID + '/' + number);
    return response.data;
}

export async function rotatePictureRight(itemID: string, number: number): Promise<Picture[]> {
    checkInstance();
    const response = await instance.get((globalThis as any).apiUrl + 'pictures/rotateright/' + itemID + '/' + number);
    return response.data;
}

export async function movePictureDown(itemID: string, number: number): Promise<Picture[]> {
    checkInstance();
    const response = await instance.get((globalThis as any).apiUrl + 'pictures/movedown/' + itemID + '/' + number);
    return response.data;
}

export async function savePicture(data: Object, itemID: string): Promise<Picture[]> {
    checkInstance();
    const response = await instance.post((globalThis as any).apiUrl + 'pictures', data, {
        headers: {
            'Content-Type': 'multipart/form-data',
            'itemID': itemID
        }
    });
    
    return response.data;
}

export async function saveItem(moduleIdx: number, _itemID: string, _parentID: string, data: Map<string, Object>): Promise<string> {
    checkInstance();
    const result = await instance.post((globalThis as any).apiUrl + 'item', data, {
        headers: {
            'itemID': _itemID,
            'parentID': _parentID,
            'moduleIndex': moduleIdx
        }
    });
    
    return result.data;
}

export async function deleteItem(itemID: string, moduleIdx: number): Promise<Picture[]> {
    checkInstance();
    const response = await instance.delete((globalThis as any).apiUrl + 'item/' + moduleIdx + '/' + itemID);
    return response.data;
}

export async function login(username: string, password: string): Promise<User | null> {
    checkInstance();
    const response = await instance.get((globalThis as any).apiUrl + 'login/' + username, {
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

export async function saveFieldSettings(moduleIdx: number, fieldSettings: FieldSetting[]): Promise<FieldSetting[]> {
    checkInstance();
    const result = await instance.post((globalThis as any).apiUrl + 'fieldsettings', fieldSettings, {
        headers: {
            'Content-Type': 'application/json',
            'moduleIndex': moduleIdx
        }
    });
    
    return result.data;
}

export async function fetchFieldSettingsForEditing(moduleIdx: number): Promise<FieldSetting[]> {
    checkInstance();
    const response = await instance.get((globalThis as any).apiUrl + 'fieldsettings/editing/' + moduleIdx);
    return response.data;
}

export async function fetchFieldSettings(moduleIdx: number): Promise<FieldSetting[]> {
    checkInstance();
    const response = await instance.get((globalThis as any).apiUrl + 'fieldsettings/' + moduleIdx);
    return response.data;
}

export async function saveOverviewFieldSettings(moduleIdx: number, fieldSettings: OverviewFieldSetting[]): Promise<OverviewFieldSetting[]> {
    checkInstance();
    const result = await instance.post((globalThis as any).apiUrl + 'overviewfieldsettings', fieldSettings, {
        headers: {
            'Content-Type': 'application/json',
            'moduleIndex': moduleIdx
        }
    });
    
    return result.data;
}

export async function isEditingAllowed(moduleIdx: number): Promise<boolean> {
    checkInstance();
    const response = await instance.get((globalThis as any).apiUrl + 'module/editing_allowed/' + moduleIdx);
    return response.data;
}

export async function fetchOverviewFieldSettings(moduleIdx: number): Promise<OverviewFieldSetting[]> {
    checkInstance();
    const response = await instance.get((globalThis as any).apiUrl + 'overviewfieldsettings/' + moduleIdx);
    return response.data;
}

export async function fetchResources(lang: string | undefined): Promise<Translation> {
    
    checkInstance();
    
    if (!instance.getUri())
        throw new Error("Configuration not yet initialized");
    
    if (lang === undefined)
        lang = "English";
    
    const response = await instance.get((globalThis as any).apiUrl + 'resources/' + lang);

    // contruct a valid Map for easy object    
    let resources = new Map<string, string>();
    Object.keys(response.data).map((res) => {
        resources.set(res, response.data[res]);
    });
    
    return resources;
}

export async function fetchAttachments(itemID: string): Promise<Attachment[]> {
    checkInstance();
    const response = await instance.get((globalThis as any).apiUrl + 'attachments/' + itemID);
    return response.data;
}

export async function fetchPictures(itemID: string): Promise<Picture[]> {
    checkInstance();
    const response = await instance.get((globalThis as any).apiUrl + 'pictures/' + itemID);
    return response.data;
}

export async function fetchReferences(moduleIdx: number): Promise<References[]> {
    checkInstance();
    const response = await instance.get((globalThis as any).apiUrl + 'references/' + moduleIdx);
    return response.data;
}

export async function fetchReference(moduleIdx: number, itemID: string): Promise<Reference> {
    checkInstance();
    const response = await instance.get((globalThis as any).apiUrl + 'references/' + moduleIdx + '/' + itemID);
    return response.data;
}

export async function fetchModules(): Promise<Module[]> {
    checkInstance();
    const response = await instance.get('modules');
    return response.data;
}

export async function fetchItem(moduleIdx: number, itemId: string, viewMode: boolean): Promise<Item> {
    checkInstance();
	const response = await instance.get((globalThis as any).apiUrl + 'item/' + moduleIdx + '/' + itemId, {
        headers: {
            'viewMode': viewMode
        }
    });
	return response.data;
}

export async function fetchChildren(moduleIdx: number, itemId: string): Promise<Item[]> {
    checkInstance();
    const response = await instance.get((globalThis as any).apiUrl + 'children/' + moduleIdx + '/' +  itemId);
    return response.data;
}

export async function fetchItems(moduleIdx: number, searchTerm: String | undefined): Promise<Item[]> {
    checkInstance();
    let url = (globalThis as any).apiUrl + 'items/' + moduleIdx;
    
    if (searchTerm)
        url += "/" + searchTerm;
    
	const response = await instance.get(url);
	return response.data;
}

export async function searchItems(moduleIdx: number, searchTerm: String): Promise<Item[]> {
    checkInstance();
	const response = await instance.get((globalThis as any).apiUrl + 'items/' + moduleIdx + '/' + searchTerm);
	return response.data;
}