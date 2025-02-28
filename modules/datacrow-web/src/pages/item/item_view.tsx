import { useLocation, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { fetchItem, type Item } from "../../services/datacrow_api";
import { RequireAuth, useAuth } from "../../context/authentication_context";
import { useModule } from "../../context/module_context";
import { Tab, Tabs } from "react-bootstrap";
import { useTranslation } from "../../context/translation_context";
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
    }, [state?.itemID]);
    
    useEffect(() => {
        (module && itemID) && fetchItem(module.index, itemID, true).
        then((data) => {
            console.log("reloading");
            setItem(data);
        }).
        catch(error => {
            console.log(error);
            if (error.status === 401) {
                navigate("/login");
            }
        });
    }, [module, itemID, state?.itemID]);
    
    return (
        <RequireAuth key={"auth-" + itemID}>
        
            <div style={{ display: "inline-block", width: "100%", textAlign: "left" }}  key={itemID}>

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
                        
                    
                        { item?.fields && item?.fields.map((fieldValue) => (
                            (fieldValue.value) && (
                                <ViewField
                                    field={fieldValue.field}
                                    value={fieldValue.value}
                                />
                            )
                        ))}
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
