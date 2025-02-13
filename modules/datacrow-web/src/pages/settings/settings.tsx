import { Tabs, Tab } from "react-bootstrap";
import { RequireAuth } from "../../context/authentication_context";
import { useTranslation } from "../../context/translation_context";

export function SettingsPage() {
    
    const { t } = useTranslation();
    
    return (
        <RequireAuth>
        
            <div style={{ display: "inline-block", width: "100%", textAlign: "left" }} key="div-item-details">
            
                <Tabs
                    defaultActiveKey="profile"
                    key="field_settings"
                    className="mb-3">

                    <Tab eventKey="details" title={t("lblDetails")} key="details-tab">
                        
                    </Tab>

                </Tabs>
            </div>
        </RequireAuth>
    );
    
}