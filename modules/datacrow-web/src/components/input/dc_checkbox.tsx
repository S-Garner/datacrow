import { Form, InputGroup } from "react-bootstrap";
import type { Field } from "../.././services/datacrow_api";

export function DcCheckBox(field : Field, value : Object) {
	return (
		<>
			<Form.Check
				id={"field-" + field.index}
				key={"field-" + field.index}
				defaultChecked={(value as boolean)}
				placeholder={field.label}
				aria-label={field.label}
				readOnly={field.readOnly} />
		</>
	);
}