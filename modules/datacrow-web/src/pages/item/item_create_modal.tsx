import { useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { fetchReferences, saveItem, type Field, type References, fetchFieldSettings, type FieldSetting } from "../../services/datacrow_api";
import { useModule } from "../../context/module_context";
import { Button, Modal } from "react-bootstrap";
import { FormProvider, useForm } from 'react-hook-form';
import { useTranslation } from "../../context/translation_context";
import { useMessage } from "../../context/message_context";

import Form from 'react-bootstrap/Form';
import InputField from "../../components/input/dc_input_field";
import { FieldType } from "../../components/component_types";

type Props = {
    show: boolean;
    moduleIdx: number;
    onCreateItem: (itemID: string | undefined) => void;
};

export function ItemCreateModal({ moduleIdx, show, onCreateItem }: Props) {

    const [fields, setFields] = useState<Field[]>();
    const [fieldSettings, setFieldSettings] = useState<FieldSetting[]>();
    const [references, setReferences] = useState<References[]>();
    const moduleContext = useModule();
    const message = useMessage();
    const navigate = useNavigate();
    const methods = useForm();
    const { t } = useTranslation();

    let module = moduleIdx ? moduleContext.getModule(moduleIdx) : undefined;

    useEffect(() => {
        moduleIdx && fetchFieldSettings(moduleIdx).
            then((data) => {
                setFieldSettings(data);
                setFields(moduleContext.getFields(moduleIdx, data));
            }).
            catch(error => {
                console.log(error);
                if (error.status === 401) {
                    navigate("/login");
                }
            });
    }, [moduleIdx]);

    useEffect(() => {
        moduleIdx && fetchReferences(moduleIdx).
            then((data) => setReferences(data)).
            catch(error => {
                console.log(error);
                if (error.status === 401) {
                    navigate("/login");
                }
            });
    }, [moduleIdx]);

    function ReferencesForField(field: Field) {
        var i = 0;
        while (i < references!.length) {
            if (references![i].moduleIdx === field.referencedModuleIdx)
                return references![i];
            i++;
        }
        return undefined;
    }
    
    const handleClose = () => {
        onCreateItem(undefined);
    }

    const onSubmit = (data: any, e: any) => {
        e.preventDefault();

        if (moduleIdx) {
            saveItem(moduleIdx, "", "", data).
                then((itemID) => {
                    onCreateItem(itemID);
                }).
                catch(error => {
                    if (error.status === 401) {
                        navigate("/login");
                    } else {
                        console.log(error);
                        message.showMessage(error.response.data);
                    }
                });
        }
    }

    return (
        <Modal centered show={show}>

            <Modal.Header style={{ height: "3em", textAlign: "center" }}>
                {t("lblCreatingNewItem", [String(t(String(module?.itemName)))])}<br />
            </Modal.Header>

            <Modal.Body>
                <FormProvider {...methods}>
                    <Form key="form-item-detail" validated={false} onSubmit={methods.handleSubmit(onSubmit)}>

                        {(references && fieldSettings && fields) && fields.map((field) => (
                            (!field.readOnly || field.hidden) && (
                                <InputField
                                    field={field}
                                    value={undefined}
                                    references={ReferencesForField(field)}
                                    viewOnly={false}
                                />
                            )
                        ))}

                        <div className="float-container" style={{ float: "right" }}>
                            <Button key="item-details-cancel-button" style={{ marginRight: "5px" }} onClick={() => handleClose()}>
                                {t("lblCancel")}
                            </Button>

                            <Button type="submit" key="item-details-submit-button">
                                {t("lblSave")}
                            </Button>
                        </div>

                    </Form>
                </FormProvider>
            </Modal.Body>
        </Modal>
    );
}
