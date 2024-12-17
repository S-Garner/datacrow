import { Form } from "react-bootstrap";
import type { InputFieldProperties } from "./dc_input_field";

export function DcUrlField({
    field,
    value
}: InputFieldProperties) {
	/*function redirect(url: string) {
		window.location.href = url;
	} */
	return (
		<Form.Control
			id={"inputfield-" + field.index}
			key={"inputfield-" + field.index}
			defaultValue={(value as string)}
			placeholder={field.label}
			aria-label={field.label}
			readOnly={field.readOnly} />
	);
}