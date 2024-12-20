import { Form } from "react-bootstrap";
import type { InputFieldProps } from "./dc_input_field";
import { useFormContext } from "react-hook-form";

export function DcLongTextField({
    field,
    value
}: InputFieldProps) {
    
    const { register } = useFormContext();
    
	return (
		<Form.Control
			id={"inputfield-" + field.index}
			key={"inputfield-" + field.index}
			defaultValue={(value as string)}
			placeholder={field.label}
			aria-label={field.label}
			readOnly={field.readOnly}
			as="textarea" 
			rows={5}
			{...register("inputfield-" + field.index)} />
	);
}