import { Form, InputGroup } from "react-bootstrap";
import type { InputFieldProps } from "./dc_input_field";
import { useFormContext } from "react-hook-form";
import { useTranslation } from "../../context/translation_context";

export default function DcCheckBox({
    field,
    value
}: InputFieldProps) {
    
    const { register } = useFormContext();
    const { t } = useTranslation();
    
	return (
		<InputGroup className="mb-3">
			<Form.Check
				id={"inputfield-" + field.index}
				key={"inputfield-" + field.index}
				defaultChecked={(value as boolean)}
				placeholder={t(field.label)}
				aria-label={t(field.label)}
				label={t(field.label)}
				readOnly={field.readOnly}
				required={field.required}
                {...register("inputfield-" + field.index)} />
		</InputGroup>
	);
}