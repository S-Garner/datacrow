import { Form } from "react-bootstrap";
import type { InputFieldComponentProps } from "./dc_input_field";
import { useFormContext } from "react-hook-form";

export function DcDateField({
    field,
    value
}: InputFieldComponentProps) {
	
	const { register } = useFormContext();
	
	return (
		<Form.Control
			id={"inputfield-" + field.index}
			key={"inputfield-" + field.index}
			type="date"
			defaultValue={(value as string)}
			placeholder={field.label}
			aria-label={field.label}
			hidden={field.hidden}
			readOnly={field.readOnly}
			required={field.required}
			{...register("inputfield-" + field.index)} />
	);
}