export interface Module {
  index: number;
  name: string;
  icon: string;
}

const baseUrl = 'http://localhost:8080/datacrow/api/';

export async function fetchModules(): Promise<Module[]> {
    const response = await fetch(baseUrl + 'modules/');
    const result = await response.json();
  	return result;
}
