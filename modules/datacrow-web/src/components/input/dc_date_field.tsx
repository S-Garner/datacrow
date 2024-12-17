import { Form } from "react-bootstrap";
import type { Field } from "../.././services/datacrow_api";

export function DcDateField(field : Field, value : Object) {
	
	return (
		<>
			<Form.Control
				id={"field-" + field.index}
				key={"field-" + field.index}
				type="date"
				defaultValue={(value as string)}
				placeholder={field.label}
				aria-label={field.label}
				readOnly={field.readOnly} />
		</>
	);
}