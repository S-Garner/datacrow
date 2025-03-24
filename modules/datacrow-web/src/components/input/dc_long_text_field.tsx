import { Form } from "react-bootstrap";
import type { InputFieldComponentProps } from "./dc_input_field";
import { useFormContext } from "react-hook-form";
import { useTranslation } from "../../context/translation_context";

export function DcLongTextField({
    field,
    value
}: InputFieldComponentProps) {
    
    const { register } = useFormContext();
    const { t } = useTranslation();
    
	return (
		<Form.Control
			id={"inputfield-" + field.index}
			key={"inputfield-" + field.index}
			defaultValue={(value as string)}
			aria-label={field.label}
			readOnly={field.readOnly}
			hidden={field.hidden}
			as="textarea" 
			rows={5}
			required={field.required}
			{...register("inputfield-" + field.index)} />
	);
}