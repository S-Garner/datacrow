import { Badge, Col, Row, Stack } from "react-bootstrap";
import { useTranslation } from "../../context/translation_context";
import type { Field, Reference } from "../../services/datacrow_api";
import { FieldType } from "../component_types";
import ViewReferenceField from "./dc_view_reference_field";

export interface Props {
    field: Field,
    value: Object | undefined
}

export default function ViewField({
    field,
    value
}: Props) {

    const {t} = useTranslation();
    
    return (
        <Col key={"detailsColField" + field.index}>
            <Row key={"detailsRowLabelField" + field.index} className="text-secondary">
                <div>
                    {t(field.label)}
                </div>
            </Row>

            <Row key={"detailsRowViewField" + field.index} className="mb-3">
            
               {(field.type === FieldType.TextField ||
                 field.type === FieldType.LongTextField ||
                 field.type === FieldType.DateField ||
                 field.type === FieldType.NumberField ||
                 field.type === FieldType.DecimalField ||
                 field.type === FieldType.DurationField) && (
                    
                    <div key={"div-" + field.index}>
                        {String(value)}
                    </div>
                 )}
                 
                 {field.type === FieldType.DropDown && (
                    value && (

                    <Stack direction="horizontal" gap={1} >
                        <ViewReferenceField field={field} reference={(value as Reference)} />
                    </Stack>)
                 )}

                {(field.type === FieldType.ReferencesField ||
                    field.type === FieldType.TagField) && (

                    <Stack direction="horizontal" gap={1} >
                        {value && (value as Array<Reference>).map((ref) => (
                            <ViewReferenceField field={field} reference={ref} />
                        ))}
                    </Stack>
                 )}

                 {(field.type === FieldType.UrlField ||
                   field.type === FieldType.FileField) && (
                    
                    <div key={"div-" + field.index}>
                        {String(value)}
                    </div>
                 )}

                 {field.type === FieldType.RatingField && (
                    <div key={"div-" + field.index}>
                        {String(value)}
                    </div>
                 )}
                 
                {field.type === FieldType.CheckBox && (
                    <div key={"div-" + field.index}>
                        {String(value)}
                    </div>
                 )}
                 
            </Row>
        </Col>
    )
}