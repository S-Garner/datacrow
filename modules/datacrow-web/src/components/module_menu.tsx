import { useEffect, useState } from 'react';
import { fetchModules, type Module } from '../services/datacrow_api';
import { Button } from 'react-bootstrap';
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
			<div className="nav-item dropdown">
				<a className="nav-link dropdown-toggle" href="#" role="button" data-bs-toggle="dropdown" aria-expanded="false"  onClick={() => mainModule && setSelectedModule(mainModule)}>
								<img src={"data:image/png;base64, " + mainModule?.icon} />
								&nbsp;{mainModule?.name}
				</a>
				
				<div className="dropdown-menu">
					{modules.map((module) => (
						<li>
							<Button onClick={() => setSelectedMainModule(module)} key={"moduleMenu" + module.index} className="dropdown-item">
								<img src={"data:image/png;base64, " + module.icon} />
								&nbsp;{module.name}
							</Button>
						</li>
					))}
				</div>
			</div>
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
