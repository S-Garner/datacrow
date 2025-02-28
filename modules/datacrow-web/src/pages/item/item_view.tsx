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
import AttachmentEditList from "../../components/list/attachment_edit_list";
import ItemDetailsMenu from "../../components/menu/item_details_menu_bar";
import ViewField from "../../components/view/dc_view_field";

export function ItemViewPage() {

    const [selectedTab, setSelectedTab] = useState('details');
    const [item, setItem] = useState<Item>();
    const [itemID, setItemID] = useState<string>();
    const moduleContext = useModule();
    const navigate = useNavigate();
    const { state } = useLocation();
    const { t } = useTranslation();
    const auth = useAuth();
    const methods = useForm();

    const module = moduleContext.selectedModule;

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
        (module && itemID) && fetchItem(module.index, itemID, true).
        then((data) => {
            setItem(data);
        }).
        catch(error => {
            console.log(error);
            if (error.status === 401) {
                navigate("/login");
            }
        });
    }, [module, itemID]);
    
    const onSubmit = (data: any, e: any) => {
        e.preventDefault();
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
                    
                        {itemID && ( 
                            <ItemDetailsMenu 
                                moduleIdx={module.index}
                                editMode={false} 
                                itemID={itemID} 
                                formTitle={item?.name} 
                                navigateBackTo="/item_view" />)
                        }
                        
                        <FormProvider {...methods}>
                            <Form key="form-item-detail" validated={false} onSubmit={methods.handleSubmit(onSubmit)}>
                    
                                { item?.fields.map((fieldValue) => (
                                    (fieldValue.value) && (
                                        <ViewField
                                            field={fieldValue.field}
                                            value={fieldValue.value}
                                        />
                                    )
                                ))}
                            </Form>
                        </FormProvider>
                    </Tab>

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
