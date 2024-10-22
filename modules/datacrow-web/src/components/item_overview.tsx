import { useCurrentModule } from '../module_context';

export function ItemOverview() {

	const currentModule = useCurrentModule();

	if (!currentModule) {
		
	} else {
		return <div>
		{currentModule}
		</div>		
	}
}

export default ItemOverview;