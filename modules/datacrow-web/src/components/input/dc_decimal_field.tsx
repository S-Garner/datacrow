import { Form } from "react-bootstrap";
import type { InputFieldProperties } from "./dc_input_field";

export function DcDecimalField({
    field,
    value
}: InputFieldProperties) {
	
	return (
		<Form.Control
			id={"inputfield-" + field.index}
			key={"inputfield-" + field.index}
			onKeyDown={(event) => {
				if (!/[0-9,\.,\,]/.test(event.key) && 
					event.key != "Backspace" && 
					event.key != "Delete" && 
					event.key != "ArrowLeft" &&
					event.key != "ArrowRight" &&
					event.key != "Tab") {
					event.preventDefault();
				}
			}}
			onChange={(e) => {
				const amount = e.target.value;
				if (!amount.match(/^\d{1,}(\.\d{0,2})?$/)) {
  					e.preventDefault();
				}
			}}
			defaultValue={(value as string)}
			placeholder={field.label}
			aria-label={field.label}
			readOnly={field.readOnly} />
	);
}