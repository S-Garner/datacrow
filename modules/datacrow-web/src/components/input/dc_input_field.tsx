import type { Field, References } from "../,,/../../services/datacrow_api";
import { Col, Form, Row } from "react-bootstrap";
import DcReferenceField from "./dc_reference_field";
import { DcLongTextField } from "./dc_long_text_field";
import { DcTextField } from "./dc_text_field";
import DcCheckBox from "./dc_checkbox";
import { DcDateField } from "./dc_date_field";
import { DcNumberField } from "./dc_number_field";
import { DcDecimalField } from "./dc_decimal_field";
import DcMultiReferenceField from "./dc_multi_reference_field";
import DcTagField from "./dc_tag_field";
import DcRatingField from "./dc_rating_field";
import { useTranslation } from "../../context/translation_context";
import DcRuntimeField from "./dc_runtime_field";
import { FieldType } from "../component_types";
import { DcIconField } from "./dc_icon_field";

export interface InputFieldProps {
    field: Field,
    value: Object | undefined,
    references?: References,
    viewOnly : boolean
}

export interface InputFieldComponentProps {
    field: Field,
    value: Object | undefined,
    references?: References
}

export default function InputField({
    field,
    value,
    references,
    viewOnly
}: InputFieldProps) {

    const {t} = useTranslation();

    return (
        <Col key={"detailsColField" + field.index}>
            {((field.type != FieldType.CheckBox && !field.hidden) || viewOnly && value)  && (
                <Row key={"detailsRowLabelField" + field.index}>
                    <Form.Label
                        style={{ textAlign: "left" }}
                        className="text-secondary"
                        key={"label-" + field.index}
                        htmlFor={"field-" + field.index}>
                        {t(field.label)}
                    </Form.Label>
                </Row>
                )}

            <Row key={"detailsRowInputField" + field.index} className="mb-3">
                <Form.Group key="form-group-item-details">
                    {field.type === FieldType.CheckBox && (
                        <DcCheckBox
                            key={"field-" + field.index}
                            field={field}
                            value={value} />)}

                    {field.type === FieldType.TextField && (
                        <DcTextField
                            key={"field-" + field.index}
                            field={field}
                            value={value} />)}

                    {field.type === FieldType.LongTextField && (
                        <DcLongTextField
                            key={"field-" + field.index}
                            field={field}
                            value={value} />)}

                    {field.type === FieldType.DropDown && (
                        <DcReferenceField
                            key={"field-" + field.index}
                            field={field}
                            value={value}
                            references={references} />)}
                            
                    {field.type === FieldType.ReferencesField && (
                        <DcMultiReferenceField
                            key={"field-" + field.index}
                            field={field}
                            value={value}
                            references={references} />)}
                            
                    {field.type === FieldType.UrlField && (
                        <DcTextField
                            key={"field-" + field.index}
                            field={field}
                            value={value} />)}
                            
                    {field.type === FieldType.DateField && (
                        <DcDateField
                            key={"field-" + field.index}
                            field={field}
                            value={value} />)}

                    {field.type === FieldType.TagField && (
                        <DcTagField
                            key={"field-" + field.index}
                            field={field}
                            value={value}
                            references={references} />)}
                            
                    {field.type === FieldType.FileField && (
                        <DcTextField
                            key={"field-" + field.index}
                            field={field}
                            value={value} />)}
                            
                    {field.type === FieldType.RatingField && (
                        <DcRatingField
                            key={"field-" + field.index}
                            field={field}
                            value={value} />)
                    }
                    
                    {field.type === FieldType.NumberField && (
                        <DcNumberField
                            key={"field-" + field.index}
                            field={field}
                            value={value} />)}
                            
                    {field.type === FieldType.DecimalField && (
                        <DcDecimalField
                            key={"field-" + field.index}
                            field={field}
                            value={value} />)}
                            
                    {field.type === FieldType.DurationField && (
                        <DcRuntimeField
                            key={"field-" + field.index}
                            field={field}
                            value={value} />)}
                            
                    {field.type === FieldType.IconField && (
                        <DcIconField
                            key={"field-" + field.index}
                            field={field}
                            value={value} />)}                            
                            
                    <Form.Control.Feedback>ok</Form.Control.Feedback>
                </Form.Group>
            </Row>
        </Col>
    )
}