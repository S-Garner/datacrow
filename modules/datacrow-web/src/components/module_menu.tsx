import { useEffect, useState } from 'react';
import { fetchModules, type Module } from '../services/datacrow_api';
import { Button, ButtonGroup, Dropdown } from 'react-bootstrap';
import { CurrentModuleContext } from '../context/module_context';

function ModuleMenu({ children }: { children: JSX.Element }) {

	const [modules, setModules] = useState<Module[]>([]);
	const [mainModule, setMainModule] = useState<Module>();
	const [selectedModule, setSelectedModule] = useState<Module>();

	useEffect(() => {
		fetchModules().then((data) => setModuleData(data));
	}, []);

	function setModuleData(modules: Module[]) {
		setModules(modules);

		// make sure there's always a selected module		
		if (modules.length > 1)
			setMainModule(modules[0])	
	}

	function setSelectedMainModule(m: Module) {
		setMainModule(m);
		setSelectedModule(m);
	}

	function DisplayMainModules() {
		return (
			<Dropdown as={ButtonGroup}>
				<Button onClick={() => mainModule && setSelectedModule(mainModule)} key="button-selected-module">
					<img src={"data:image/png;base64, " + mainModule?.icon} />
					&nbsp;{mainModule?.name}
				</Button>

				<Dropdown.Toggle split id="module-select-dropdown" />

				<Dropdown.Menu>
					{modules.map((module) => (
						<Dropdown.Item onClick={() => setSelectedMainModule(module)} key={"moduleMenu" + module.index}>
							<img src={"data:image/png;base64, " + module.icon} />
							&nbsp;{module.name}
						</Dropdown.Item>
					))}
				</Dropdown.Menu>
			</Dropdown>			
		);
	}

	function DisplayReferenceModules() {
		return (
			<div style={{ display: "flex", flexWrap: "wrap" }} id="referencedModules">
				{mainModule && mainModule.children.map((child) => (
					<Button
						onClick={() => setSelectedModule(child)}
							className={`${child.index === selectedModule?.index ? "sub-module-button-selected" : "sub-module-button"}`}
						key={"moduleSubMenu" + child.index}>

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
			<nav className="navbar navbar-expand-lg bs-body-bg">
				<DisplayMainModules />
				<DisplayReferenceModules />
			</nav>

			{children}
		</CurrentModuleContext.Provider>
	);
}

export default ModuleMenu;
