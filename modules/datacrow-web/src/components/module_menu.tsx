import Accordion from 'react-bootstrap/Accordion';
import { useEffect, useState} from 'react';
import { fetchModules, type Module } from '../services/datacrow_api';
import { Button } from 'react-bootstrap';
import { CurrentModuleContext } from '../context/module_context';

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
					<Button onClick={() => setSelectedMainModule(module)} key={"moduleMenu" + module.index} className="main-module-button">
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
					<Button
						onClick={() => setSelectedModule(mainModule)} 
						className={`${mainModule.index === selectedModule?.index ? "sub-module-button-selected" : "sub-module-button"}`} 
						key={"moduleSubMenu" + mainModule.index}>
							<img src={"data:image/png;base64, " + mainModule.icon} />
							&nbsp;
							{mainModule.name}
						</Button>
				)}
			
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
