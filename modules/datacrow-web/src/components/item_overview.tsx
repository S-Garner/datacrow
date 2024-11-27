import { Button, Card, InputGroup } from 'react-bootstrap';
import { fetchItems, searchItems, type Item } from '../services/datacrow_api';
import { useCurrentModule } from '../context/module_context';
import { useEffect, useState } from 'react';
import PagesDropdown from './pages_dropdown';
import Pagination from './pagination';

export function ItemOverview() {

	const currentModule = useCurrentModule();
	const [items, setItems] = useState<Item[]>([]);

	useEffect(() => {
		currentModule && fetchItems(currentModule!.index).then((data) => setItems(data));
	}, [currentModule]);

	const [currentPage, setCurrentPage] = useState(1);
	const [itemsPerPage, setItemsPerPage] = useState(30);

	const itemsPerPageOptions = [30, 60, 90, 120, 150, 180, 210, 240, 270, 300];

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

	function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
		event.preventDefault();
		let formData = new FormData(event.currentTarget);
		let searchFor = formData.get("searchFor") as string;
		
		searchItems(currentModule!.index, searchFor).then((data) => setItems(data));
	}
	
	return (
		<div className="py-20 bg-slate-900 h-full" style={{width: "100%"}}>
			
			<form onSubmit={handleSubmit}>
				<InputGroup className="mb-3">
					<input type="text" name="searchFor" />
					<Button className="search-button" type="submit">search</Button>
				</InputGroup>
			</form>
			
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
						<Card.Body>
							{item.imageUrl ? <Card.Img src={item.scaledImageUrl} /> : <div style={{ height: '300px' }} />}
						</Card.Body>
						<Card.Header style={{ height: '112px' }} >{item.name}</Card.Header>
					</Card>
				))}
			</div>
			<Pagination
				itemsPerPage={itemsPerPage}
				totalItems={totalItems}
				currentPage={currentPage}
				paginate={paginate}
			/>

		</div>
	);
}
