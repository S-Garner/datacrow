import { useLocation, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { fetchItem, type Item, type Module } from "../../services/datacrow_api";
import { RequireAuth, useAuth } from "../../context/authentication_context";
import { useModule } from "../../context/module_context";
import { Carousel, Tab, Tabs } from "react-bootstrap";
import { useTranslation } from "../../context/translation_context";
import AttachmentEditList from "../../components/list/attachment_edit_list";
import ItemDetailsMenu from "../../components/menu/item_details_menu_bar";
import ViewField from "../../components/view/dc_view_field";

export function ItemViewPage() {

    const [selectedTab, setSelectedTab] = useState('details');
    const [item, setItem] = useState<Item>();
    const [itemID, setItemID] = useState<string>();
    const navigate = useNavigate();
    const { state } = useLocation();
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
        setItemID(state.itemID);
    }, [state.itemID]);

    let moduleIdx = state.moduleIdx;
    
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
                                formTitle={item?.name}
                                navigateBackTo="/item_view" />)
                        }
                        {
                            item?.pictures && item?.pictures.length > 0 && (
                                <div style={{ display: "inline-block", textAlign: "left" }}>
                                    <Carousel>
                                        {item.pictures && item.pictures.map((picture) => (
                                            <Carousel.Item style={{height: "32em"}}>
                                                <img
                                                    style={{ height: "30em", width: "auto" }}
                                                    src={picture.url}
                                                    alt="First slide"
                                                />
                                            </Carousel.Item>

                                        )
                                        )}
                                    </Carousel>
                                </div>
                            )
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
