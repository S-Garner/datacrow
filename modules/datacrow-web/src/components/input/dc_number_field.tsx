import { Form } from "react-bootstrap";
import type { InputFieldProps } from "./dc_input_field";
import { useFormContext } from "react-hook-form";

export function DcNumberField({
    field,
    value
}: InputFieldProps) {
    
    const { register } = useFormContext();
    
	return (
		<Form.Control
			id={"inputfield-" + field.index}
			key={"inputfield-" + field.index}
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
			readOnly={field.readOnly}
			required={field.required}
			{...register("inputfield-" + field.index)} />
	);
}