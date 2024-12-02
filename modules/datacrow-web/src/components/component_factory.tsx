import Form from 'react-bootstrap/Form';
import type { Field } from "../,,/../services/datacrow_api";
import { InputGroup } from 'react-bootstrap';

enum FieldType {
	CheckBox = 1,
	TextField = 2,
	LongTextField = 3,
	DropDown = 3,
	UrlField = 4,
	ReferencesField = 5,
	DateField = 6,
	FileField = 7,
	TagField = 8,
	RatingField = 9,
	IconField = 10,
	NumberField = 11,
	DecimalField = 12,
	DurationField = 13
}

export function InputField(field : Field, value : Object) {
	
	if (field.type === FieldType.CheckBox) {

	} else if (field.type === FieldType.TextField) {

	} else if (field.type === FieldType.LongTextField) {
		
	} else if (field.type === FieldType.DropDown) {
		
	} else if (field.type === FieldType.UrlField) {
		
	} else if (field.type === FieldType.ReferencesField) {
		
	} else if (field.type === FieldType.DateField) {
		
	} else if (field.type === FieldType.FileField) {
		
	} else if (field.type === FieldType.TagField) {
	
	} else if (field.type === FieldType.RatingField) {
		
	} else if (field.type === FieldType.IconField) {

	} else if (field.type === FieldType.NumberField) {

	} else if (field.type === FieldType.DecimalField) {

	} else if (field.type === FieldType.DurationField) {
		
	}
	
	return (
		<div>
		 	<label for="email" className="form-label">Email:</label>
    		<input type="email" className="form-control" id="email" placeholder="Enter email" name="email" />
		</div>
		
	)
	
	
/*	return 	(<InputGroup className="mb-3">
				<InputGroup.Text>{field.label}</InputGroup.Text>
				<Form.Control 
					id={"field-" + field.index} 
					value={(value as string)} 
					placeholder={field.label}
          			aria-label={field.label} />
			 </InputGroup>); */
}
