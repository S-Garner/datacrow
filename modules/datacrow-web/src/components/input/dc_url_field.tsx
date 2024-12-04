import { Form } from "react-bootstrap";
import type { Field } from "../.././services/datacrow_api";

export function DcUrlField(field : Field, value : Object) {
	
	/*function redirect(url: string) {
		window.location.href = url;
	} */
	
	return (
		<>
			<Form.Control
				id={"field-" + field.index}
				key={"field-" + field.index}
				defaultValue={(value as string)}
				placeholder={field.label}
				aria-label={field.label}
				readOnly={field.readOnly}  />
		</>
	);
}