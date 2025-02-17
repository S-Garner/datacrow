import { Tabs, Tab } from "react-bootstrap";
import { RequireAuth } from "../../context/authentication_context";
import { useTranslation } from "../../context/translation_context";
import FieldSettingsOverview from "../../components/field_settings_overview";

export function SettingsPage() {
    
    const { t } = useTranslation();
    
    return (
        <RequireAuth>
        
            <div style={{ display: "inline-block", width: "100%", textAlign: "left" }} key="div-settings-tabs">
            
                <Tabs className="mb-3">

                    <Tab eventKey="details" title={t("lblDetails")} key="details-tab" className="mb-3">
                        <FieldSettingsOverview />
                    </Tab>

                </Tabs>
            </div>
        </RequireAuth>
    );
    
}