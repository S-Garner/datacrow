import { Form, InputGroup } from "react-bootstrap";
import type { Field } from "../.././services/datacrow_api";

export function DcLongTextField(field : Field, value : Object) {
	return (
		<>
			<Form.Control
				id={"field-" + field.index}
				key={"field-" + field.index}
				defaultValue={(value as string)}
				placeholder={field.label}
				aria-label={field.label}
				readOnly={field.readOnly}
				as="textarea" 
				rows={5} />
		</>
	);
}