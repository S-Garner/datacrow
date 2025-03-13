import type { InputFieldComponentProps } from "./dc_input_field";
import { Controller, useFormContext } from "react-hook-form";
import FileUploadField from "./dc_file_upload";
import { toBase64 } from "../../utils/utilities";
import { useState } from "react";
import { Col, Container, Row } from "react-bootstrap";

export function DcIconField({
    field,
    value
}: InputFieldComponentProps) {

    const { register, setValue } = useFormContext();
    const [fileData, setFileData] = useState<string | undefined>(value as string);
    
    const handleImageFileSelect = async (file: File) => {
        const data = await toBase64(file);
        setFileData(data);
        setValue("inputfield-" + field.index, data);
        
        console.log(data);
    }

    return (

        <Container fluid>

            <FileUploadField handleFileSelect={handleImageFileSelect} accept="image/*" />

            {
                fileData && (
                    <Row >
                        <Col className="text-center" style={{ height: "100px" }}>
                            <img src={fileData.startsWith("data") ? fileData : `data:image/png;base64,${fileData}`} style={{ width: "auto", height: "64px" }} />
                        </Col>
                    </Row>
                )
            }
            
            <Row>
                <Controller
                    name={"inputfield-" + field.index}
                    key={"inputfield-" + field.index}
                    disabled={field.readOnly}
                    rules={{ required: field.required }}
                    defaultValue={fileData}
                    render={renderProps => {
                        return (
                            <input
                                type="text"
                                key={"inputfield-" + field.index}
                                {...register("inputfield-" + field.index)}
                                {...renderProps.field}
                                hidden={true}
                            />
                        );
                    }}
                />
            </Row>
            
        </Container>
    );
}