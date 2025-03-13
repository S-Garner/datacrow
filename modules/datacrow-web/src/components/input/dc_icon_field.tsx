import type { InputFieldComponentProps } from "./dc_input_field";
import { Controller, useFormContext } from "react-hook-form";
import FileUploadField from "./dc_file_upload";
import { toBase64 } from "../../utils/utilities";
import { useState } from "react";
import { Col, Container, Row } from "react-bootstrap";
import { Icon } from "../icon";

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
    }

    function handleDelete() {
        setFileData("");
        setValue("inputfield-" + field.index, "");
    }

    return (
        <div>
            <FileUploadField handleFileSelect={handleImageFileSelect} accept="image/*" />

            {
                fileData && (
                    <Row>
                        <Col className="text-center" style={{ height: "100px"}}>
                            <Icon value={fileData} />
                            <div className="bd-theme" style={{ display: "flex", flexWrap: "wrap", float: "right", top: "0"}} >
                                <i className="bi bi-trash" onClick={() => handleDelete()} style={{fontSize:"1.2rem"}}></i>
                            </div>
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
        </div>
    );
}