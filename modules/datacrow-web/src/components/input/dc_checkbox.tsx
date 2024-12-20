import { Form, InputGroup } from "react-bootstrap";
import type { InputFieldProps } from "./dc_input_field";
import { useFormContext } from "react-hook-form";

export default function DcCheckBox({
    field,
    value
}: InputFieldProps) {
    
    const { register } = useFormContext();
    
	return (
		<InputGroup className="mb-3">
			<Form.Check
				id={"inputfield-" + field.index}
				key={"inputfield-" + field.index}
				defaultChecked={(value as boolean)}
				placeholder={field.label}
				aria-label={field.label}
				label={field.label}
				readOnly={field.readOnly}
                {...register("inputfield-" + field.index)} />
		</InputGroup>
	);
}