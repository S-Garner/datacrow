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
import PictureEditList from "../../components/pictures_edit_list";
import ChildrenOverview from "../../components/item_overview_children";
import AttachmentEditList from "../../components/attachment_edit_list";
import ItemDetailsMenu from "../../components/item_details_menu_bar";

export function ItemPage() {

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
    const auth = useAuth();

    useEffect(() => {
        if (!state) {
            navigate('/');
        }
    }, []);
    
    useEffect(() => {
        if (!auth.user || !state.itemID) {
            navigate('/login');
        }
    }, []);
    
    useEffect(() => {
        if (state.itemID) {
            setItemID(state.itemID);
        }
    }, []);
    
    useEffect(() => {
        (moduleContext.selectedModule && itemID) && fetchItem(moduleContext.selectedModule.index, itemID).
        then((data) => {
            setItem(data);
        }).
        catch(error => {
            console.log(error);
            if (error.status === 401) {
                navigate("/login");
            }
        });
    }, [moduleContext.selectedModule, itemID]);

    useEffect(() => {
        state && moduleContext.selectedModule && fetchReferences(moduleContext.selectedModule.index).
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
        
        if (state) {
            saveItem(moduleContext.selectedModule.index, state.itemID, data).
            then(() => message.showMessage(t("msgItemHasBeenSaved"))).
            catch(error => {
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
                    
                        {itemID && <ItemDetailsMenu itemID={itemID} />}
                    
                        <FormProvider {...methods}>
                            <Form key="form-item-detail" validated={false} onSubmit={methods.handleSubmit(onSubmit)}>
                                
                                {references && item?.fields.map((fieldValue) => (
                                    (!fieldValue.field.readOnly || fieldValue.field.hidden) && (
                                        <InputField
                                            field={fieldValue.field}
                                            value={fieldValue.value}
                                            references={ReferencesForField(fieldValue.field)}
                                        />
                                    )
                                ))}
                                
                                <Button type="submit" key="item-details-submit-button">
                                    {t("lblSave")}
                                </Button>
                                
                            </Form>
                        </FormProvider>
                    </Tab>

                    {(itemID && moduleContext.selectedModule.hasChild) &&
                        (
                            <Tab eventKey="children" title={t(moduleContext.selectedModule.child.name)} key="children-tab">
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
