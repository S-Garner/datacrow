import { Button, Card, InputGroup } from 'react-bootstrap';
import { fetchItems, searchItems, type Item } from '../../services/datacrow_api';
import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';

import PagesDropdown from './pages_dropdown';
import Pagination from './pagination';
import { useModule } from '../../context/module_context';
import { useTranslation } from '../../context/translation_context';

export function ItemOverview() {

	const moduleContext = useModule();
	const navigate = useNavigate();
	const [items, setItems] = useState<Item[]>([]);
	const { t } = useTranslation();

    useEffect(() => {
        moduleContext.selectedModule && fetchItems(moduleContext.selectedModule.index, moduleContext.filter).
            then((data) => setItems(data)).
            catch(error => {
                console.log(error);
                if (error.status === 401) {
                    navigate("/login");
                }
            });
    }, [moduleContext.selectedModule]);
    
    const module = moduleContext!.selectedModule;

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
        
        moduleContext.setFilter(searchFor);
        filterItems(searchFor);
	}
	
	function handleCreateNew() {
        let moduleIdx = module.index;
        navigate('/item_create', {state: { moduleIdx }});
    }
	
	function filterItems(filter: string) {
        searchItems(module!.index, filter).then((data) => setItems(data));
    }
	
	function openItem(itemID : string) {
        let moduleIdx = module.index;
		navigate('/item_view', { state: { itemID, moduleIdx }});
	}
	
	return (
		<div className="py-20 bg-slate-900 h-full" style={{width: "100%"}}>
			
			<div className="float-container" style={{marginTop: "20px"}}>
                <div className="float-child">
                    <form onSubmit={handleSubmit} >
                        <InputGroup className="mb-3">
                            <input type="text" name="searchFor" className="form-control" defaultValue={moduleContext.filter} />
                            <Button className="search-button" type="submit">{t("lblSearch")?.toLowerCase()}</Button>
                        </InputGroup>
                    </form>
                </div>
                
                <div className="float-child" style={{marginLeft: "20px"}}>
                    <i className="bi bi-plus-circle menu-icon" style={{ fontSize: "1.7rem"}} onClick={() => handleCreateNew()} ></i>
                </div>
                
			</div>
			
			<div style={{ float: "left", clear: "both", marginTop: "10px"}}>
				{module && <PagesDropdown
					title={t("lblItemsPerPage")  + ` ${itemsPerPage}`}
					options={itemsPerPageOptions}
					handleSelectOption={(option: string) => setItemsPerPage(+option)}
				/>}
			</div>

            <div style={{ float: "left", clear: "both" }}>
    			<Pagination
    				itemsPerPage={itemsPerPage}
    				totalItems={totalItems}
    				currentPage={currentPage}
    				paginate={paginate}
    			/>
			</div>

			<div style={{ display: "flex", flexWrap: "wrap", float: "left", clear: "both" }}>
				{currentItems!.map((item) => (
					<Card style={{ width: '18rem' }} key={"card" + item.id} onClick={() => openItem(item.id)}>
						<Card.Body>
						    {item.imageUrl ? <Card.Img src={item.scaledImageUrl + "?" + Date.now()} /> : <div style={{ height: '300px' }} />}
						</Card.Body>
						<Card.Header style={{ height: '112px' }}>
							{item.name}
						</Card.Header>
					</Card>
				))}
			</div>

            <div style={{ float: "left", clear: "both" }}>
    			<Pagination
    				itemsPerPage={itemsPerPage}
    				totalItems={totalItems}
    				currentPage={currentPage}
    				paginate={paginate}
    			/>
			</div>

		</div>
	);
}
