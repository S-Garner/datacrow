import { Button, Card } from 'react-bootstrap';
import { fetchItems, type Item } from '../api/datacrow_api';
import { useCurrentModule } from '../module_context';
import { useEffect, useState } from 'react';
import Dropdown from './dropdown';
import Pagination from './pagination';

export function ItemOverview() {

	const currentModule = useCurrentModule();

	const [items, setItems] = useState<Item[]>([]);
	
	useEffect(() => {
		fetchItems(currentModule!).then((data) => setItems(data));
	}, [currentModule]);


  const [currentPage, setCurrentPage] = useState(1);
  const [itemsPerPage, setItemsPerPage] = useState(10);

  const itemsPerPageOptions = [10, 25, 50];

  const totalPosts = items.length;
  const lastItemIndex = currentPage * itemsPerPage;
  const firstItemIndex = lastItemIndex - itemsPerPage;
  const currentItems = items.slice(firstItemIndex, lastItemIndex);

  const paginate = (pageNumber: number) => {
    setCurrentPage(pageNumber);
  };

  return (
    <div className="py-20 bg-slate-900 h-full text-white">
      <div className="max-w-5xl mx-auto px-4 p-10">
        <div className="flex justify-between items-center">
          <h1 className="text-5xl tracking-tight text-center mb-4">
            Pagination Demo
          </h1>
          <Dropdown
            title={`Items per page: ${itemsPerPage}`}
            options={itemsPerPageOptions}
            handleSelectOption={(option: string) => setItemsPerPage(+option)}
          />
        </div>
        <hr />
        <ul className="mt-10 space-y-6 text-xl" >
			<div style={{ display: "flex", flexWrap: "wrap" }}>
				{currentItems!.map((item) => (
					<Card style={{ width: '18rem' }}>
				      <Card.Img variant="top" src={item.scaledImageUrl} />
				      <Card.Body>
				        <Card.Title>{item.name}</Card.Title>
				        <Card.Text>
				          {item.name}
				        </Card.Text>
				        <Button variant="primary">open</Button>
				      </Card.Body>
				    </Card>
				))}
			</div>
        </ul>
        <div className="flex justify-center mt-10 ">
          <Pagination
            itemsPerPage={itemsPerPage}
            totalPosts={totalPosts}
            paginate={paginate}
          />
        </div>
      </div>
    </div>
  );
}
