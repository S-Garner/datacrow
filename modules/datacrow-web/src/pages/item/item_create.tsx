import { useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { fetchReferences, saveItem, type Field, type Item, type References, fetchFieldSettings, type FieldSetting } from "../../services/datacrow_api";
import { RequireAuth } from "../../context/authentication_context";
import { useModule } from "../../context/module_context";
import { Button, Modal, Tab, Tabs } from "react-bootstrap";
import { FormProvider, useForm } from 'react-hook-form';
import { useTranslation } from "../../context/translation_context";
import Form from 'react-bootstrap/Form';
import InputField from "../../components/input/dc_input_field";
import { useMessage } from "../../context/message_context";

export function ItemCreatePage() {

    const [fieldSettings, setFieldSettings] = useState<FieldSetting[]>();
    const [selectedTab, setSelectedTab] = useState('details');
    const [references, setReferences] = useState<References[]>();
    const moduleContext = useModule();
    const message = useMessage();
    const navigate = useNavigate();
    const methods = useForm();
    const { t } = useTranslation();

    useEffect(() => {
        moduleContext.selectedModule && fetchFieldSettings(moduleContext.selectedModule.index).
        then((data) => setFieldSettings(data)).
        catch(error => {
            console.log(error);
            if (error.status === 401) {
                navigate("/login");
            }
        });
    }, [moduleContext.selectedModule]);


    useEffect(() => {
        moduleContext.selectedModule && fetchReferences(moduleContext.selectedModule.index).
        then((data) => setReferences(data)).
        catch(error => {
            console.log(error);
            if (error.status === 401) {
                navigate("/login");
            }
        });
    }, [moduleContext.selectedModule]);
    
    function ReferencesForField(field: Field) {
        var i = 0;
        while (i < references!.length) {
            if (references![i].moduleIdx === field.referencedModuleIdx)
                return references![i];
            i++;
        }
        return undefined;
    }
    
    const onSubmit = (data: any, e: any) => {
        e.preventDefault();
        saveItem(moduleContext.selectedModule.index, "", data).
        then((itemID) => navigate('/item', { state: { itemID }})).
        catch(error => {
            if (error.status === 401) {
                navigate("/login");
            } else {
                console.log(error);
                message.showMessage(error.response.data);
            }
        });
    }

    return (
        <RequireAuth>
        
            <div style={{ display: "inline-block", width: "100%", textAlign: "left" }} key="div-item-details">
            
                <Tabs
                    defaultActiveKey="profile"
                    key="item-details-tabs"
                    activeKey={selectedTab}
                    onSelect={(k) => k && setSelectedTab(k)}
                    className="mb-3">

                    <Tab eventKey="details" title={t("lblDetails")} key="details-tab">
                        <FormProvider {...methods}>
                            <Form key="form-item-detail" validated={false} onSubmit={methods.handleSubmit(onSubmit)}>
                                
                                {references && fieldSettings &&  moduleContext.getFields(fieldSettings).map((field) => (
                                    (!field.readOnly || field.hidden) && (
                                        <InputField
                                            field={field}
                                            value={undefined}
                                            references={ReferencesForField(field)}
                                        />
                                    )
                                ))}
                                
                                <Button type="submit" key="item-details-submit-button">
                                    {t("lblSave")}
                                </Button>
                                
                            </Form>
                        </FormProvider>
                    </Tab>

                </Tabs>
            </div>
        </RequireAuth>
    );
}
