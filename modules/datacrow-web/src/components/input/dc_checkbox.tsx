import { Form } from "react-bootstrap";
import type { InputFieldComponentProps } from "./dc_input_field";
import { useFormContext } from "react-hook-form";
import { useTranslation } from "../../context/translation_context";

export default function DcCheckBox({
    field,
    value
}: InputFieldComponentProps) {
    
    const { register } = useFormContext();
    const { t } = useTranslation();
    
	return (
		<Form.Check
			id={"inputfield-" + field.index}
			key={"inputfield-" + field.index}
			defaultChecked={(value as boolean)}
			placeholder={t(field.label)}
			aria-label={t(field.label)}
			label={t(field.label)}
			hidden={field.hidden}
			readOnly={field.readOnly}
			required={field.required}
            {...register("inputfield-" + field.index)} />
	);
}