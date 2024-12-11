import { Form } from "react-bootstrap";
import { type Field, type Reference, type References } from "../.././services/datacrow_api";

export function DcDropDown(field: Field, value: Object, references?: References) {
    
	return (
        <Form.Select aria-label="" key={"field-select-" + field.index}>
            {references!.items!.map((item) => (
                   <option id={item.id} key={item.id}> {item.name} </option>
                ))}
        </Form.Select>				
	);
}