import { useCurrentModule } from '../module_context';

export function ItemOverview() {

	const currentModule = useCurrentModule();

	if (!currentModule) {
		return <div />
	} else {
		return (
		<div>
			{currentModule}
		</div>)
	}
}
