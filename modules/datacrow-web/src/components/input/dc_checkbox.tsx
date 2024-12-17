import { Form, InputGroup } from "react-bootstrap";
import type { InputFieldProperties } from "./dc_input_field";

export default function DcCheckBox({
    field,
    value
}: InputFieldProperties) {
	return (
		<InputGroup className="mb-3">
			<Form.Check
				id={"inputfield-" + field.index}
				key={"inputfield-" + field.index}
				defaultChecked={(value as boolean)}
				placeholder={field.label}
				aria-label={field.label}
				label={field.label}
				readOnly={field.readOnly} />
		</InputGroup>
	);
}