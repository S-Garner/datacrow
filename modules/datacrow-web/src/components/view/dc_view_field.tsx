import { Col, Row, Form } from "react-bootstrap";
import { useTranslation } from "../../context/translation_context";
import type { Field, References } from "../../services/datacrow_api";
import { FieldType } from "../component_types";

export interface InputFieldProps {
    field: Field,
    value: Object | undefined,
    references?: References
}

export default function ViewField({
    field,
    value
}: InputFieldProps) {

    const {t} = useTranslation();

    return (
        <Col key={"detailsColField" + field.index}>
            <Row key={"detailsRowLabelField" + field.index}>

            </Row>

            <Row key={"detailsRowInputField" + field.index} className="mb-3">
            
                {(field.type === FieldType.TextField ||
                 field.type === FieldType.LongTextField ||
                 field.type === FieldType.LongTextField ||
                 field.type === FieldType.DateField ||
                 field.type === FieldType.NumberField ||
                 field.type === FieldType.DecimalField ||
                 field.type === FieldType.DurationField) && (
                     
                    <div>
                        {t(field.label) + "-" + value  }  
                    </div>
                 )}
                 
                 {field.type === FieldType.DropDown && (
                    <div>
                        {t(field.label) + "-" + value}  
                    </div>
                 )}

                 {(field.type === FieldType.ReferencesField ||
                   field.type === FieldType.TagField) && (
                    <div>
                        {t(field.label) + "-" + value  }  
                    </div>
                 )}

                 {(field.type === FieldType.UrlField ||
                   field.type === FieldType.FileField) && (
                    <div>
                        {t(field.label) + "-" + value  }  
                    </div>
                 )}

                 {field.type === FieldType.RatingField && (
                    <div>
                        {t(field.label) + "-" + value  }  
                    </div>
                 )}
                 
                {field.type === FieldType.CheckBox && (
                    <div>
                        {t(field.label) + "-" + value  }  
                    </div>
                 )}
            </Row>
        </Col>
    )
}