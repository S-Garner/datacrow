import { Form } from "react-bootstrap";
import type { InputFieldProperties } from "./dc_input_field";

export function DcDateField({
    field,
    value
}: InputFieldProperties) {
	
	return (
		<Form.Control
			id={"inputfield-" + field.index}
			key={"inputfield-" + field.index}
			type="date"
			defaultValue={(value as string)}
			placeholder={field.label}
			aria-label={field.label}
			readOnly={field.readOnly} />
	);
}