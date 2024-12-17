import type { Field, References } from "../,,/../../services/datacrow_api";

import { Col, Form, Row } from "react-bootstrap";
import DcReferenceField from "./dc_reference_field";

export interface InputFieldProperties {
field: Field,
    value: Object,
    references?: References
}

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

export default function InputField({
    field,
    value,
    references
}: InputFieldProperties) {
    return (
        <Col key={"detailsColField" + field.index}>
            {field.type != FieldType.CheckBox && (
                <Row key={"detailsRowLabelField" + field.index}>
                    <Form.Label
                        style={{ textAlign: "left" }}
                        className="text-secondary"
                        key={"label-" + field.index}
                        htmlFor={"field-" + field.index}>
                        {field.label}
                    </Form.Label>
                </Row>
                )}

            <Row key={"detailsRowInputField" + field.index} className="mb-3">
                <Form.Group>

                    {field.type === FieldType.DropDown && (
                        <DcReferenceField
                            key={"field-" + field.index}
                            field={field}
                            value={value}
                            references={references} />)}
                            
                    <Form.Control.Feedback>ok</Form.Control.Feedback>
                </Form.Group>
            </Row>
        </Col>
    )
}