import { Form } from "react-bootstrap";
import type { Field } from "../,,/../../services/datacrow_api";
import { DcTextField } from "./dc_textfield";
import { DcLongTextField } from "./dc_long_textfield";
import { DcCheckBox } from "./dc_checkbox";

enum FieldType {
	CheckBox = 0,
	TextField = 1,
	LongTextField = 2,
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

function Field(field : Field, value : Object) {
	if (field.type === FieldType.CheckBox) {
		return DcCheckBox(field, value);
	} else if (field.type === FieldType.TextField) {
		return DcTextField(field, value);
	} else if (field.type === FieldType.LongTextField) {
		return DcLongTextField(field, value);
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
}

export function InputField(field: Field, value: Object) {
	return (
		<div className="row mb-3" key={"input-row-" + field.index}>
			<Form.Label
				style={{ textAlign: "left" }}
				className="text-secondary"
				key={"label-" + field.index}
				htmlFor={"field-" + field.index}>

				{field.label}
			</Form.Label>

			{Field(field, value)}
		</div>)
}
