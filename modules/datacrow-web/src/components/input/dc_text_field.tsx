import { Form } from "react-bootstrap";
import type { InputFieldProps } from "./dc_input_field";
import { useFormContext } from "react-hook-form";
import { useTranslation } from "../../context/translation_context";

export function DcTextField({
    field,
    value
}: InputFieldProps) {
    
    const { register } = useFormContext();
    const { t } = useTranslation();
    
	return (
		<Form.Control
			id={"inputfield-" + field.index}
			key={"inputfield-" + field.index}
			defaultValue={(value as string)}
			placeholder={t(field.label)}
			aria-label={field.label}
			{...register("inputfield-" + field.index)}
			readOnly={field.readOnly}
			required={field.required} />
	);
}