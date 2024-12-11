import type { Field, Reference, References } from "../,,/../../services/datacrow_api";

import { Col, Form, Row } from "react-bootstrap";
import { DcTextField } from "./dc_textfield";
import { DcLongTextField } from "./dc_long_textfield";
import { DcCheckBox } from "./dc_checkbox";
import { DcUrlField } from "./dc_url_field";
import { DcDateField } from "./dc_datefield";
import { DcNumberField } from "./dc_numberfield";
import { DcDecimalField } from "./dc_decimalfield";
import { DcDropDown } from "./dc_dropdown";

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

function Field(field : Field, value : Object, references?: References) {
	if (field.type === FieldType.CheckBox) {
		return DcCheckBox(field, value);
	} else if (field.type === FieldType.TextField) {
		return DcTextField(field, value);
	} else if (field.type === FieldType.LongTextField) {
		return DcLongTextField(field, value);
	} else if (field.type === FieldType.DropDown) {
        return DcDropDown(field, value, references);
	} else if (field.type === FieldType.UrlField) {
		return DcUrlField(field, value);
	} else if (field.type === FieldType.ReferencesField) {
		
	} else if (field.type === FieldType.DateField) {
		return DcDateField(field, value);
	} else if (field.type === FieldType.FileField) {
		
	} else if (field.type === FieldType.TagField) {
	
	} else if (field.type === FieldType.RatingField) {
		
	} else if (field.type === FieldType.IconField) {

	} else if (field.type === FieldType.NumberField) {
		return DcNumberField(field, value);
	} else if (field.type === FieldType.DecimalField) {
		return DcDecimalField(field, value);
	} else if (field.type === FieldType.DurationField) {
		
	}
}

export function InputField(field: Field, value: Object, references?: References) {
	return (
		<Col key={"detailsColField" + field.index}>
			{field.type != FieldType.CheckBox ?
				<Row key={"detailsRowLabelField" + field.index}>
					<Form.Label
						style={{ textAlign: "left" }}
						className="text-secondary"
						key={"label-" + field.index}
						htmlFor={"field-" + field.index}>
						{field.label}
					</Form.Label>
				</Row>
				:
				""}

			<Row key={"detailsRowInputField" + field.index} className="mb-3">
				<Form.Group>
					{Field(field, value, references)}
					<Form.Control.Feedback>ok</Form.Control.Feedback>
				</Form.Group>
			</Row>
		</Col>
	)
}
