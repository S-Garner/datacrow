import { fetchItems, type Item } from '../api/datacrow_api';
import { useCurrentModule } from '../module_context';
import { useEffect, useState } from 'react';

export function ItemOverview() {

	const currentModule = useCurrentModule();

	const [items, setItems] = useState<Item[]>([]);
	
	useEffect(() => {
		fetchItems(currentModule!).then((data) => setItems(data));
	}, [currentModule]);

	return (
		<div>
			{items!.map((item) => (
				<div>
					{item.name}
				</div>
			))}
		</div>)
}
