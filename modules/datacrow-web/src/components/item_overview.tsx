import { Button, Card, InputGroup } from 'react-bootstrap';
import { fetchItems, searchItems, type Item } from '../services/datacrow_api';
import { useModule } from '../context/module_context';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from '../context/translation_context';

import PagesDropdown from './pages_dropdown';
import Pagination from './pagination';

export function ItemOverview() {

	const currentModule = useModule();
	const navigate = useNavigate();
	const [items, setItems] = useState<Item[]>([]);
	const { t } = useTranslation();

    useEffect(() => {
        currentModule.selectedModule && fetchItems(currentModule.selectedModule.index, currentModule.filter).
            then((data) => setItems(data)).
            catch(error => {
                console.log(error);
                if (error.status === 401) {
                    navigate("/login");
                }
            });
    }, [currentModule.selectedModule]);

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
        
        currentModule.setFilter(searchFor);
        
        filterItems(searchFor);
	}
	
	function filterItems(filter: string) {
        searchItems(currentModule!.selectedModule.index, filter).then((data) => setItems(data));
    }
	
	function openItem(itemID : string) {
		navigate('/item', { state: { itemID }});
	}
	
	return (
		<div className="py-20 bg-slate-900 h-full" style={{width: "100%"}}>
			
			<form onSubmit={handleSubmit} style={{width: "20em"}}>
				<InputGroup className="mb-3" style={{marginTop: "10px"}}>
					<input type="text" name="searchFor" className="form-control" defaultValue={currentModule.filter} />
					<Button className="search-button" type="submit">{t("lblSearch")?.toLowerCase()}</Button>
				</InputGroup>
			</form>
			
			<div style={{ right: "0px" }}>
				{currentModule && <PagesDropdown
					title={t("lblItemsPerPage")  + ` ${itemsPerPage}`}
					options={itemsPerPageOptions}
					handleSelectOption={(option: string) => setItemsPerPage(+option)}
				/>}
			</div>

			<Pagination
				itemsPerPage={itemsPerPage}
				totalItems={totalItems}
				currentPage={currentPage}
				paginate={paginate}
			/>

			<div style={{ display: "flex", flexWrap: "wrap" }}>
				{currentItems!.map((item) => (
					<Card style={{ width: '18rem' }} key={"card" + item.id} onClick={() =>  openItem(item.id)}>
						<Card.Body>
						    {item.imageUrl ? <Card.Img src={item.scaledImageUrl} /> : <div style={{ height: '300px' }} />}
						</Card.Body>
						<Card.Header style={{ height: '112px' }}>
							{item.name}
						</Card.Header>
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
