import Accordion from 'react-bootstrap/Accordion';

import React, { useState, useEffect } from 'react';
import { fetchModules, type Module } from '../api/datacrow_api';

function ModuleMenu() {

	const [modules, setModules] = useState<Module[]>([]);
	useEffect(() => {
		fetchModules().then((data) => setModules(data));
	}, []);


	return (
		<Accordion>
			<Accordion.Item eventKey="0">
				<Accordion.Header>Module Selection</Accordion.Header>
				<Accordion.Body>
					<div>
						<ul>
							{modules.map((item) => (
								<li key={item.index}>{item.name}</li>
							))}
						</ul>
					</div>
				</Accordion.Body>
			</Accordion.Item>
		</Accordion>
	);
}

export default ModuleMenu;