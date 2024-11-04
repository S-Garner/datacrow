import { Button, Card, Figure } from 'react-bootstrap';
import { fetchItems, type Item } from '../api/datacrow_api';
import { useCurrentModule } from '../module_context';
import { useEffect, useState } from 'react';
import PagesDropdown from './pages_dropdown';
import Pagination from './pagination';

export function ItemOverview() {

	const currentModule = useCurrentModule();

	const [items, setItems] = useState<Item[]>([]);

	useEffect(() => {currentModule &&
		fetchItems(currentModule!).then((data) => setItems(data));
	}, [currentModule]);

	const [currentPage, setCurrentPage] = useState(1);
	const [itemsPerPage, setItemsPerPage] = useState(10);

	const itemsPerPageOptions = [10, 30, 60, 90, 120, 150, 180, 210, 240, 270, 300];

	const totalItems = items.length;
	const lastItemIndex = currentPage * itemsPerPage;
	const firstItemIndex = lastItemIndex - itemsPerPage;
	const currentItems = items.slice(firstItemIndex, lastItemIndex);
	
	const paginate = (pageNumber: number) => {
		setCurrentPage(pageNumber);
	};

	if (currentItems.length === 0 && totalItems > 0) {
		setCurrentPage(1);
	};

	return (
		<div className="py-20 bg-slate-900 h-full">
			<div className="max-w-5xl mx-auto px-4 p-10">
				
				<div style={{ right: "0px" }}>
			
				{currentModule && <PagesDropdown
					title={`Items per page: ${itemsPerPage}`}
					options={itemsPerPageOptions}
					handleSelectOption={(option: string) => setItemsPerPage(+option)}
				/>}
				
				</div>
				
				<div style={{ display: "flex", flexWrap: "wrap" }}>
					{currentItems!.map((item) => (
						<Card style={{ width: '18rem' }} key={"card" + item.id}>
							<Card.Header style={{ height: '92px' }} >{item.name}</Card.Header>
							<Card.Body>
								{item.imageUrl ? <Card.Img src={ item.imageUrl} /> : <div style={{ height: '300px' }} />}
							</Card.Body>
							<Card.Footer>
								<Button variant="primary">open</Button>
							</Card.Footer>
						</Card>
					))}
				</div>

				<Pagination
					itemsPerPage={itemsPerPage}
					totalItems={totalItems}
					paginate={paginate}
				/>
			</div>
		</div>
	);
}
