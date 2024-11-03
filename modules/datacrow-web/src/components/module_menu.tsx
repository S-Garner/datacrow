import Accordion from 'react-bootstrap/Accordion';
import React, { useEffect, type MouseEvent } from 'react';
import { fetchModules, type Module } from '../api/datacrow_api';
import { Button, Dropdown } from 'react-bootstrap';
import { useState } from 'react';
import { CurrentModuleContext } from '../module_context';

function ModuleMenu({ children }: { children: JSX.Element }) {

	const [modules, setModules] = useState<Module[]>([]);
	const [mainModule, setMainModule] = useState<Module>();
	const [selectedModule, setSelectedModule] = useState<Module>();
	
	useEffect(() => {
		fetchModules().then((data) => setModules(data));
	}, []);

	function setSelectedMainModule(m: Module) {
		setMainModule(m);
	}

	function DisplayMainModules() {
		return (
			<div style={{ display: "flex", flexWrap: "wrap" }} id="mainModules">
				{modules.map((module) => (
					<Button onClick={() => setSelectedMainModule(module)}>
						<img src={"data:image/png;base64, " + module.icon} />
					</Button>				
				))}
			</div>
		);
	}

	function DisplayReferenceModules() {
		return (
			<div style={{ display: "flex", flexWrap: "wrap" }} id="referencedModules">
			
				{mainModule && (
					<Button onClick={() => setSelectedModule(mainModule)} className={`${mainModule.index === selectedModule?.index ? "red" : "green"}`}>
							<img src={"data:image/png;base64, " + mainModule.icon} />
							&nbsp;
							{mainModule.name}
						</Button>
				)}
			
				{mainModule && mainModule.children.map((child) => (
					<Button onClick={() => setSelectedModule(child)} className={`${child.index === selectedModule?.index ? "red" : "green"}`}>
						<img src={"data:image/png;base64, " + child.icon} />
						&nbsp;
						{child.name}
					</Button>
				))}
			</div>
		);
	}
	
	return (
		<CurrentModuleContext.Provider value={selectedModule?.index}>
			<Accordion>
				<Accordion.Item eventKey="0">
					<Accordion.Header>Modules</Accordion.Header>
					<Accordion.Body>
						<DisplayMainModules />
						<br />
						<DisplayReferenceModules />
					</Accordion.Body>
				</Accordion.Item>
			</Accordion>
			
			<br />
			
			{children}

		</CurrentModuleContext.Provider>
	);
}

export default ModuleMenu;
