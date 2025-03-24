import { Form } from "react-bootstrap";
import type { InputFieldComponentProps } from "./dc_input_field";
import { useFormContext } from "react-hook-form";
import { useTranslation } from "../../context/translation_context";

export function DcDecimalField({
    field,
    value
}: InputFieldComponentProps) {
	
	const { register } = useFormContext();
	const { t } = useTranslation();
	
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
			/*onChange={(e) => {
				const amount = e.target.value;
				if (!amount.match(/^\d{1,}(\.\d{0,2})?$/)) {
  					e.preventDefault();
				}
			}} */
			defaultValue={(value as string)}
			aria-label={t(field.label)}
			hidden={field.hidden}
			readOnly={field.readOnly}
			required={field.required}
			{...register("inputfield-" + field.index)} />
	);
}