import { useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { fetchReferences, saveItem, type Field, type Item, type References, fetchFieldSettings, type FieldSetting } from "../../services/datacrow_api";
import { RequireAuth } from "../../context/authentication_context";
import { useModule } from "../../context/module_context";
import { Button, Tab, Tabs } from "react-bootstrap";
import { FormProvider, useForm } from 'react-hook-form';
import { useTranslation } from "../../context/translation_context";
import Form from 'react-bootstrap/Form';
import InputField from "../../components/input/dc_input_field";
import { useMessage } from "../../context/message_context";
import ItemDetailsMenu from "../../components/menu/item_details_menu_bar";
import BusyModal from "../../components/message/busy_modal";

export function ItemCreatePage() {

    const [saving, setSaving] = useState(false);
    const [fields, setFields] = useState<Field[]>();
    const [fieldSettings, setFieldSettings] = useState<FieldSetting[]>();
    const [selectedTab, setSelectedTab] = useState('details');
    const [references, setReferences] = useState<References[]>();
    const moduleContext = useModule();
    const message = useMessage();
    const navigate = useNavigate();
    const methods = useForm();
    const { t } = useTranslation();
    
    const module = moduleContext.selectedModule;
    const itemName = t(module.itemName);
    
    useEffect(() => {
        module && fetchFieldSettings(module.index).
        then((data) => {
            setFieldSettings(data);
            setFields(moduleContext.getFields(module.index, data));    
        }).
        catch(error => {
            console.log(error);
            if (error.status === 401) {
                navigate("/login");
            }
        });
    }, [module]);

    useEffect(() => {
        module && fetchReferences(module.index).
        then((data) => setReferences(data)).
        catch(error => {
            console.log(error);
            if (error.status === 401) {
                navigate("/login");
            }
        });
    }, [module]);
    
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
        
        setSaving(true);
        
        saveItem(module.index, "", data).
        then((itemID) => {
            setSaving(false);
            navigate('/item_edit', { replace: true, state: { itemID }});
        }).
        catch(error => {
            setSaving(false);
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
                    
                        <BusyModal show={saving} message={t("msgBusySavingItem")} />
                    
                        <ItemDetailsMenu
                            moduleIdx={module.index}
                            editMode={true} 
                            itemID={undefined} 
                            formTitle={t("lblCreatingNewItem", [String(itemName)])}                             
                            navigateBackTo="/item_create" />
                    
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
