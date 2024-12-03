import { Form } from "react-bootstrap";
import type { Field } from "../.././services/datacrow_api";

export function DcCheckBox(field : Field, value : Object) {
	return (
		
		//style={{width: "50em"}}>
		
		<div key={`inline-${field.index}`}>    
			<Form.Check
				id={"field-" + field.index}
				key={"field-" + field.index}
				defaultChecked={(value as boolean)}
				placeholder={field.label}
				aria-label={field.label}
				label={field.label}
				readOnly={field.readOnly} />
		</div>
	);
}