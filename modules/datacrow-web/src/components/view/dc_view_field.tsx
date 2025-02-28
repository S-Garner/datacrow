import { Badge, Col, Row, Stack } from "react-bootstrap";
import { useTranslation } from "../../context/translation_context";
import type { Field, Reference } from "../../services/datacrow_api";
import { FieldType } from "../component_types";

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

            <Row key={"detailsRowInputField" + field.index} className="mb-3">
            
               {(field.type === FieldType.TextField ||
                 field.type === FieldType.LongTextField ||
                 field.type === FieldType.DateField ||
                 field.type === FieldType.NumberField ||
                 field.type === FieldType.DecimalField ||
                 field.type === FieldType.DurationField) && (
                    
                    <div>
                        {String(value)}
                    </div>
                 )}
                 
                 {field.type === FieldType.DropDown && (
                    value && (

                    <Stack direction="horizontal" gap={1} >
                        <Badge bg="secondary" style={{ height: "2.5em", alignItems: "center", display: "flex" }}>
                            {(value as Reference).iconUrl && (<img
                                src={(value as Reference).iconUrl}
                                style={{ width: "24px", paddingRight: "8px" }} />)}
    
                            {(value as Reference).name}
                        </Badge>
                    </Stack>)
                 )}

                {(field.type === FieldType.ReferencesField ||
                    field.type === FieldType.TagField) && (

                    <Stack direction="horizontal" gap={1} >
                        {value && (value as Array<Reference>).map((ref) => (
                            <Badge bg="secondary" style={{ height: "2.5em", alignItems: "center", display: "flex" }}>
                                {ref.iconUrl && (<img
                                    src={ref.iconUrl}
                                    style={{ width: "24px", paddingRight: "8px" }} />)}

                                {ref.name}
                            </Badge>
                        ))}
                    </Stack>
                 )}

                 {(field.type === FieldType.UrlField ||
                   field.type === FieldType.FileField) && (
                    
                    <div>
                        {String(value)}
                    </div>
                 )}

                 {field.type === FieldType.RatingField && (
                    <div>
                        {String(value)}
                    </div>
                 )}
                 
                {field.type === FieldType.CheckBox && (
                    <div>
                        {String(value)}
                    </div>
                 )}
                 
            </Row>
        </Col>
    )
}