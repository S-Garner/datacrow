import Accordion from 'react-bootstrap/Accordion';
import React, { useState, useEffect, type MouseEvent} from 'react';
import { fetchModules, type Module } from '../api/datacrow_api';
import { Button } from 'react-bootstrap';

function ModuleMenu() {

	const [modules, setModules] = useState<Module[]>([]);
	useEffect(() => {
		fetchModules().then((data) => setModules(data));
	}, []);
	
	
	function setSelectedModule(m : Module) {
		const [module, setModule] = useState<Module>();
		setModule(m);
	}
	
	return (
		<Accordion>
			<Accordion.Item eventKey="0">
				<Accordion.Header>Module Selection</Accordion.Header>
				<Accordion.Body>
					<div style={{ display: "flex", flexWrap: "wrap" }} id="mainModules">
						{modules.map((module) => (
							<Button onClick={() => setSelectedModule(module)}>
								<img src={"data:image/png;base64, " + module.icon} />
								&nbsp;
								{module.name}
							</Button>
						))}
					</div>
					
					<div style={{ display: "flex", flexWrap: "wrap" }} id="referenceModules">
					
					
					</div>
					
				</Accordion.Body>
			</Accordion.Item>
		</Accordion>
	);
}

export default ModuleMenu;