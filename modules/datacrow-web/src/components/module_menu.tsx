import { useEffect, useState, type JSX } from 'react';
import { fetchModules, type Module } from '../services/datacrow_api';
import { Button, ButtonGroup, Dropdown } from 'react-bootstrap';
import { useModule } from '../context/module_context';

function ModuleMenu({ children }: { children: JSX.Element }) {

	const [modules, setModules] = useState<Module[]>([]);
	const [mainModule, setMainModule] = useState<Module>();
	const [selectedModule, setSelectedModule] = useState<Module>();
	
	let currentModule = useModule();

	useEffect(() => {
		fetchModules().then((data) => setModuleData(data));
	}, []);

	function setModuleData(modules: Module[]) {
		setModules(modules);

		// make sure there's always a selected module
		if (modules.length > 0) {
			if (currentModule.selectedModule === null) {
				// set the first module as selected
				switchMainModule(modules[0])
			} else {
				// force a reload as the item might have been edited
				// switchMainModule(currentModule.module);
				setMainModule(currentModule.mainModule);
				setSelectedModule(currentModule.selectedModule);
			}
		}
	}

	function switchMainModule(m: Module) {
		setMainModule(m);
		setSelectedModule(m);
		currentModule.switchModule(m, m);
	}
	
	function switchModule(m : Module) {
		setSelectedModule(m);
		currentModule.switchModule(m, mainModule!);
	}

	function DisplayMainModules() {
		return (
			<Dropdown as={ButtonGroup}>
				<Button onClick={() => mainModule && switchModule(mainModule)} key="button-selected-module">
					<img src={"data:image/png;base64, " + mainModule?.icon} />
					&nbsp;{mainModule?.name}
				</Button>

				<Dropdown.Toggle split id="module-select-dropdown" />

				<Dropdown.Menu>
					{modules.map((module) => (
						<Dropdown.Item onClick={() => switchMainModule(module)} key={"moduleMenu" + module.index}>
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
						onClick={() => switchModule(child)}
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
		<div>
			<nav className="navbar navbar-expand-lg bs-body-bg">
				<DisplayMainModules />
				<DisplayReferenceModules />
			</nav>

			{children}
		</div>
	);
}

export default ModuleMenu;
