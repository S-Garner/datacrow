import { useLocation, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { fetchItem, fetchReferences, saveItem, type Field, type Item, type References } from "../../services/datacrow_api";
import { RequireAuth, useAuth } from "../../context/authentication_context";
import { useModule } from "../../context/module_context";
import { Button, Tab, Tabs } from "react-bootstrap";
import { FormProvider, useForm } from 'react-hook-form';
import { useTranslation } from "../../context/translation_context";
import { useMessage } from "../../context/message_context";
import Form from 'react-bootstrap/Form';
import InputField from "../../components/input/dc_input_field";
import PictureEditList from "../../components/list/pictures_edit_list";
import ChildrenOverview from "../../components/overview/item_overview_children";
import AttachmentEditList from "../../components/list/attachment_edit_list";
import ItemDetailsMenu from "../../components/menu/item_details_menu_bar";
import BusyModal from "../../components/message/busy_modal";

export function ItemPage() {

    const [saving, setSaving] = useState(false);
    const [selectedTab, setSelectedTab] = useState('details');
    const [item, setItem] = useState<Item>();
    const [itemID, setItemID] = useState<string>();
    const [references, setReferences] = useState<References[]>();
    const moduleContext = useModule();
    const message = useMessage();
    const navigate = useNavigate();
    const { state } = useLocation();
    const methods = useForm();
    const { t } = useTranslation();
    
    useEffect(() => {
        if (!state) {
            navigate('/');
        }
    }, []);

    useEffect(() => {
        if (!state) {
            navigate('/login');
        }
    }, []);

    let moduleIdx = state.moduleIdx;
    let module = moduleIdx ? moduleContext.getModule(moduleIdx) : undefined;
    
    useEffect(() => {
        setItemID(state.itemID);
    }, [state.itemID]);
    
    useEffect(() => {
        (itemID) && fetchItem(state.moduleIdx, itemID, true).
        then((data) => {
            setItem(data);
        }).
        catch(error => {
            console.log(error);
            if (error.status === 401) {
                navigate("/login");
            }
        });
    }, [itemID]);

    useEffect(() => {
        state && moduleIdx && fetchReferences(moduleIdx).
        then((data) => setReferences(data)).
        catch(error => {
            console.log(error);
            if (error.status === 401) {
                navigate("/login");
            }
        });
    }, [itemID, moduleIdx]);
	
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
        
        if (itemID && moduleIdx) {
            setSaving(true);
            
            saveItem(moduleIdx, itemID, data).
            then(() => {
                    setSaving(false);
                    message.showMessage(t("msgItemHasBeenSaved"));
                }).
            catch(error => {
                setSaving(false);
                if (error.status === 401) {
                    navigate("/login");
                } else {
                    message.showMessage(error.response.data);
                }
            });
        }
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
                    
                        {itemID && moduleIdx && ( 
                            <ItemDetailsMenu
                                moduleIdx={moduleIdx}
                                editMode={true} 
                                itemID={itemID} 
                                formTitle={t("lblEditItem",  [item ? item.name : ""])} 
                                navigateBackTo="/item_edit" />)
                        }
                    
                        <FormProvider {...methods}>
                            <Form key="form-item-detail" validated={false} onSubmit={methods.handleSubmit(onSubmit)}>
                                
                                {references && item?.fields.map((fieldValue) => (
                                    (!fieldValue.field.readOnly || fieldValue.field.hidden) && (
                                        <InputField
                                            field={fieldValue.field}
                                            value={fieldValue.value}
                                            references={ReferencesForField(fieldValue.field)}
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

                    {(itemID && module && module.hasChild) &&
                        (
                            <Tab eventKey="children" title={t(module.child.name)} key="children-tab">
                                <ChildrenOverview itemID={itemID} />
                            </Tab>
                        )
                    }
    
                    {itemID &&
                        (
                            <Tab eventKey="pictures" title={t("lblPictures")} key="pictures-tab">
                                <PictureEditList itemID={itemID} />
                            </Tab>
                        )
                    }
                    
                    {itemID &&
                        (
                            <Tab eventKey="attachments" title={t("lblAttachments")}>
                                <AttachmentEditList itemID={itemID} />
                            </Tab>
                        )
                    }
                </Tabs>
            </div>
        </RequireAuth>
    );
}
