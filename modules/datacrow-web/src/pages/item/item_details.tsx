import { useLocation, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { fetchItem, fetchReferences, saveItem, type Field, type Item, type References } from "../../services/datacrow_api";
import { RequireAuth, useAuth } from "../../context/authentication_context";
import { useModule } from "../../context/module_context";
import { Button, Tab, Tabs } from "react-bootstrap";
import { FormProvider, useForm } from 'react-hook-form';
import { useTranslation } from "../../context/translation_context";
import Form from 'react-bootstrap/Form';
import InputField from "../../components/input/dc_input_field";
import PictureEditList from "../../components/pictures_edit_list";
import ChildrenOverview from "../../components/item_overview_children";
import { useMessage } from "../../context/message_context";
import AttachmentEditList from "../../components/attachment_edit_list";

export function ItemPage() {

    const [selectedTab, setSelectedTab] = useState('details');
    const [item, setItem] = useState<Item>();
    const [references, setReferences] = useState<References[]>();
    const currentModule = useModule();
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
        if (!auth.user) {
            navigate('/login');
        }
    }, []);

    useEffect(() => {
        (state && currentModule.selectedModule && state.itemID) && fetchItem(currentModule.selectedModule.index, state.itemID).
        then((data) => setItem(data)).
        catch(error => {
            console.log(error);
            if (error.status === 401) {
                navigate("/login");
            }
        });
    }, [currentModule.selectedModule]);

    useEffect(() => {
        state && currentModule.selectedModule && fetchReferences(currentModule.selectedModule.index).
        then((data) => setReferences(data)).
        catch(error => {
            console.log(error);
            if (error.status === 401) {
                navigate("/login");
            }
        });
    }, [currentModule.selectedModule]);
	
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
            saveItem(currentModule.selectedModule.index, state.itemID, data).
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
    
    function handleShowSettings() {
        navigate('/fieldsettings', 
            { state: { navFrom: "/item", itemID: state.itemID }});
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
                    
                        <div className="float-container" style={{ float: "right", width: "100%", margin: "0px" }}>
                            <div className="float-child" style={{ float: "right" }} >
                                {auth.user && auth.user.admin &&
                                    (<i className="bi bi-tools menu-icon" style={{ fontSize: "1.4rem" }} onClick={() => handleShowSettings()} ></i>)}
                            </div>
                        </div>
                    
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

                    {(item?.id && currentModule.selectedModule.hasChild) &&
                        (
                            <Tab eventKey="children" title={t(currentModule.selectedModule.child.name)} key="children-tab">
                                <ChildrenOverview itemID={item?.id} />
                            </Tab>
                        )
                    }
    
                    {item?.id &&
                        (
                            <Tab eventKey="pictures" title={t("lblPictures")} key="pictures-tab">
                                <PictureEditList itemID={item?.id} />
                            </Tab>
                        )
                    }
                    
                    {item?.id &&
                        (
                            <Tab eventKey="attachments" title={t("lblAttachments")}>
                                <AttachmentEditList itemID={item?.id} />
                            </Tab>
                        )
                    }

                </Tabs>
            </div>
        </RequireAuth>
    );
}
