import { useLocation, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { fetchItem, isEditingAllowed, type Item } from "../../services/datacrow_api";
import { RequireAuth, useAuth } from "../../context/authentication_context";
import { useModule } from "../../context/module_context";
import { Carousel, Tab, Tabs } from "react-bootstrap";
import { useTranslation } from "../../context/translation_context";
import AttachmentEditList from "../../components/list/attachment_edit_list";
import ItemDetailsMenu from "../../components/menu/item_details_menu_bar";
import ViewField from "../../components/view/dc_view_field";
import ChildrenOverview from "../../components/overview/item_overview_children";
import RelatedItemList from "../../components/list/related_items_list";

export function ItemViewPage() {
    
    const [item, setItem] = useState<Item>();
    const [itemID, setItemID] = useState<string>();
    const navigate = useNavigate();
    const { state } = useLocation();
    const [selectedTab, setSelectedTab] = useState(state?.tab ? state.tab : 'details');
    const { t } = useTranslation();
    const auth = useAuth();
    const moduleContext = useModule();

    useEffect(() => {
        if (!state) {
            navigate('/');
        }
    }, []);

    useEffect(() => {
        if (!auth.user || !state?.itemID) {
            navigate('/login');
        }
    }, []);

    useEffect(() => {
        if (state?.tab && state?.tab.length > 0)
            setSelectedTab(state.tab);
    }, [state?.tab]);
    
    useEffect(() => {
        setItemID(state?.itemID);
    }, [state?.itemID]);

    let moduleIdx = state?.moduleIdx;
    let module = moduleIdx ? moduleContext.getModule(moduleIdx) : undefined;
    
    useEffect(() => {
        (itemID) && fetchItem(state.moduleIdx, itemID, true).
        then((data) => {
            setItem(data);
            
            if (data.relatedItems.length === 0 && selectedTab === "references")
                setSelectedTab("details");
        }).
        catch(error => {
            console.log(error);
            if (error.status === 401) {
                navigate("/login");
            }
        });
    }, [itemID]);
    
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
                
                        {moduleIdx && (
                            <ItemDetailsMenu
                                moduleIdx={moduleIdx}
                                editMode={false}
                                itemID={itemID}
                                parentID={item?.parentID}
                                formTitle={item?.name}
                                navigateBackTo="/item_view" />)
                        }
                        {
                            item?.pictures && item?.pictures.length > 1 && (
                                <div style={{ display: "inline-block", textAlign: "left" }}>
                                    <Carousel>
                                        {item.pictures && item.pictures.map((picture) => (
                                            <Carousel.Item style={{height: "32em"}}>
                                                <img
                                                    style={{ height: "30em", width: "auto" }}
                                                    src={picture.url + "?" + Date.now()}
                                                />
                                            </Carousel.Item>
                                        )
                                        )}
                                    </Carousel>
                                </div>
                            )
                        }
                                                {
                        item?.pictures && item?.pictures.length === 1 && (
                            <div style={{ display: "inline-block", textAlign: "left" }}>
                                {item.pictures && item.pictures.map((picture) => (
                                    <img
                                        style={{ height: "30em", width: "auto", marginBottom: "2em" }}
                                        src={picture.url}
                                    />
                                ))}
                            </div>
                        )}           
                    
                        { item?.fields && item?.fields.map((fieldValue) => (
                            (fieldValue.value) && (
                                <ViewField
                                    field={fieldValue.field}
                                    value={fieldValue.value}
                                />
                            )
                        ))}
                        
                        {(itemID && module && module.hasChild && !module.child.isAbstract) && (
                            <div id="children" style={{ marginTop: "40px" }}>
                                <ChildrenOverview
                                    title={String(t(module.child.itemNamePlural))}
                                    itemID={itemID}
                                    editMode={false}
                                    moduleIdx={module.child.index}
                                    parentModuleIdx={module.index}
                                    navigateBackTo="/item_view" />
                            </div>
                        )}
                        
                    </Tab>

                    {item && item.relatedItems?.length > 0 && (
                        <Tab eventKey="references" title={t("lblRelatedItems")}>
                            <RelatedItemList relatedItems={item.relatedItems} />
                        </Tab>
                    )}

                    {itemID && (
                        <Tab eventKey="attachments" title={t("lblAttachments")}>
                            <AttachmentEditList itemID={itemID} />
                        </Tab>
                    )}
                    
                </Tabs>
            </div>
        </RequireAuth>
    );
}
