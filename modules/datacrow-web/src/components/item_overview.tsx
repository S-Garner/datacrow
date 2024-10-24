import { Button, Card } from 'react-bootstrap';
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
		</div>)
}
