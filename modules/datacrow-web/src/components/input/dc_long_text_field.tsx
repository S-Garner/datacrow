import { Form } from "react-bootstrap";
import type { Field } from "../.././services/datacrow_api";
import type { InputFieldProperties } from "./dc_input_field";

export function DcLongTextField({
    field,
    value
}: InputFieldProperties) {
	return (
		<>
			<Form.Control
				id={"inputfield-" + field.index}
				key={"inputfield-" + field.index}
				defaultValue={(value as string)}
				placeholder={field.label}
				aria-label={field.label}
				readOnly={field.readOnly}
				as="textarea" 
				rows={5} />
		</>
	);
}