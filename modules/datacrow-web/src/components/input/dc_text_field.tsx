import { Form } from "react-bootstrap";
import type { InputFieldComponentProps } from "./dc_input_field";
import { useFormContext } from "react-hook-form";
import { useTranslation } from "../../context/translation_context";

export function DcTextField({
    field,
    value
}: InputFieldComponentProps) {
    
    const { register } = useFormContext();
    
	return (
		<Form.Control
			id={"inputfield-" + field.index}
			key={"inputfield-" + field.index}
			defaultValue={(value as string)}
			aria-label={field.label}
			{...register("inputfield-" + field.index)}
			readOnly={field.readOnly}
			hidden={field.hidden}
			required={field.required} />
	);
}