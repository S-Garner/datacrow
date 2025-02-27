import { Form } from "react-bootstrap";
import type { InputFieldComponentProps } from "./dc_input_field";
import { useFormContext } from "react-hook-form";
import { useTranslation } from "../../context/translation_context";

export function DcNumberField({
    field,
    value
}: InputFieldComponentProps) {
    
    const { register } = useFormContext();
    const { t } = useTranslation();
    
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
			placeholder={t(field.label)}
			aria-label={field.label}
			readOnly={field.readOnly}
			hidden={field.hidden}
			required={field.required}
			{...register("inputfield-" + field.index)} />
	);
}