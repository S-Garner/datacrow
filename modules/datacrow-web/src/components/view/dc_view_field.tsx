import { Col, Row, Form } from "react-bootstrap";
import { useTranslation } from "../../context/translation_context";
import type { Field, References } from "../../services/datacrow_api";

export interface InputFieldProps {
    field: Field,
    value: Object | undefined,
    references?: References,
    viewOnly : boolean
}

export default function InputField({
    field,
    value,
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
                
                            
            </Row>
        </Col>
    )
}