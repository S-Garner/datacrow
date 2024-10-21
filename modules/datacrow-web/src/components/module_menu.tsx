import Accordion from 'react-bootstrap/Accordion';
import React, { useEffect, type MouseEvent } from 'react';
import { fetchModules, type Module } from '../api/datacrow_api';
import { Button } from 'react-bootstrap';
import { createContext, useContext, useState } from 'react';
import { ModuleContext, useModuleContext } from "../context";

function ModuleMenu() {

	const [modules, setModules] = useState<Module[]>([]);
	const [currentModule, setCurrentModule] = useState<Module>();
	
	useEffect(() => {
		fetchModules().then((data) => setModules(data));
	}, []);

	function setSelectedModule(m: Module) {
		setCurrentModule(m);
	}

	function DisplayMainModules() {
		return (
			<div style={{ display: "flex", flexWrap: "wrap" }} id="mainModules">
				{modules.map((module) => (
					<Button onClick={() => setSelectedModule(module)}>
						<img src={"data:image/png;base64, " + module.icon} />
						&nbsp;
						{module.name}
					</Button>
				))}
			</div>
		);
	}

	function DisplayReferenceModules() {
		return (
			<ModuleContext.Provider value={currentModule}>
				<div style={{ display: "flex", flexWrap: "wrap" }} id="referencedModules">
					{currentModule?.children.map((child) => (
						<Button>
							<img src={"data:image/png;base64, " + child.icon} />
							&nbsp;
							{child.name}
						</Button>
					))}
				</div>
			</ModuleContext.Provider>
		);
	}
	
	return (
		<Accordion>
			<Accordion.Item eventKey="0">
				<Accordion.Header>Module Menu</Accordion.Header>
				<Accordion.Body>
					<DisplayMainModules />
					<br />
					<DisplayReferenceModules />
				</Accordion.Body>
			</Accordion.Item>
		</Accordion>
	);
}

export default ModuleMenu;
