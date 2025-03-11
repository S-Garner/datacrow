import { useEffect, useState, type JSX } from 'react';
import { fetchModules, type Module } from '../../services/datacrow_api';
import { Button, ButtonGroup, Dropdown } from 'react-bootstrap';
import { useModule } from '../../context/module_context';
import { useNavigate } from 'react-router-dom';
import { useTranslation } from "../../context/translation_context";

function ModuleMenu({ children }: { children: JSX.Element }) {

	const [mainModule, setMainModule] = useState<Module>();
	const [selectedModule, setSelectedModule] = useState<Module>();
	const { t } = useTranslation();
	
	let moduleContext = useModule();
	let navigate = useNavigate();

	useEffect(() => {
		fetchModules().then((data) => setModuleData(data)).catch(error => 
		{
            console.log(error);
            if (error.status === 401) {
                navigate("/login");    
            }
        });
    }, []);

	function setModuleData(modules: Module[]) {
        
        moduleContext.setModules(modules);

		// make sure there's always a selected module
		if (modules.length > 0) {
			if (moduleContext.selectedModule === null) {
                for (var i = 0; i < modules.length; i++) {
                    // set the first main module as selected
                    if (modules[i].main) {
                        switchMainModule(modules[i])
                        break;
                    }
                }
			} else {
				// force a reload as the item might have been edited
				// switchMainModule(currentModule.module);
				setMainModule(moduleContext.mainModule);
				setSelectedModule(moduleContext.selectedModule);
			}
		}
	}

	function switchMainModule(m: Module) {
		setMainModule(m);
		setSelectedModule(m);
		moduleContext.switchModule(m, m);
	}
	
	function switchModule(m : Module) {
		setSelectedModule(m);
		moduleContext.switchModule(m, mainModule!);
	}

	function DisplayMainModules() {
		return (
			<Dropdown as={ButtonGroup}>
				<Button onClick={() => mainModule && switchModule(mainModule)} key="button-selected-module">
					<img src={"data:image/png;base64, " + mainModule?.icon} />
					&nbsp;{mainModule && t(mainModule.name)}
				</Button>

				<Dropdown.Toggle split id="module-select-dropdown" />

                {
                    moduleContext.modules && (
                        <Dropdown.Menu>
                            {moduleContext.modules.map((module) => (
                                module.main &&
                                <Dropdown.Item onClick={() => switchMainModule(module)} key={"moduleMenu" + module.index}>
                                    <img src={"data:image/png;base64, " + module.icon} />
                                    &nbsp;{t(module.name)}
                                </Dropdown.Item>
                            ))}
                        </Dropdown.Menu>
                    )
                }
			</Dropdown>			
		);
	}

	function DisplayReferenceModules() {
		return (
			<div style={{ display: "flex", flexWrap: "wrap" }} id="referencedModules">
				{mainModule && mainModule.references.map((reference) => (
					<Button
						onClick={() => switchModule(reference)}
							className={`${reference.index === selectedModule?.index ? "sub-module-button-selected" : "sub-module-button"}`}
						key={"moduleSubMenu" + reference.index}>

						<img src={"data:image/png;base64, " + reference.icon} />
						&nbsp;
						{t(reference.name)}
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
