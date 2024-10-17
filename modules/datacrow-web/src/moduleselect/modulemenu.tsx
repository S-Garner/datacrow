import Accordion from 'react-bootstrap/Accordion';
import Modules from "./modules";

function ModuleMenu() {
  return (
    <Accordion>
      <Accordion.Item eventKey="0">
        <Accordion.Header>Module Selection</Accordion.Header>
        <Accordion.Body>
            <Modules />
        </Accordion.Body>
      </Accordion.Item>
    </Accordion>
  );
}

export default ModuleMenu;