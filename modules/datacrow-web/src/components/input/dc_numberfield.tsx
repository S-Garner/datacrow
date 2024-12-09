import { Form } from "react-bootstrap";
import type { Field } from "../.././services/datacrow_api";

export function DcNumberField(field: Field, value: Object) {
	return (
		<>
			<Form.Control
				id={"field-" + field.index}
				key={"field-" + field.index}
				type="number"
				onKeyDown={(event) => {
					if (!/[0-9]/.test(event.key) && 
						event.key != "Backspace" && 
						event.key != "Delete" && 
						event.key != "ArrowLeft" &&
						event.key != "ArrowRight" &&
						event.key != "Tab") {
						event.preventDefault();
					}
				}}
				defaultValue={(value as string)}
				placeholder={field.label}
				aria-label={field.label}
				readOnly={field.readOnly} />
		</>
	);
}