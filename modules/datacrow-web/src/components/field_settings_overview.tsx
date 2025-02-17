import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "../context/translation_context";
import { fetchFieldSettings, type FieldSetting } from "../services/datacrow_api";
import { useModule } from "../context/module_context";

export default function FieldSettingsOverview() {
    
    const [fieldSettings, setFieldSettings] = useState<FieldSetting[]>();
    
    const currentModule = useModule();
    const navigate = useNavigate();
    
    useEffect(() => {
        currentModule.selectedModule && fetchFieldSettings(currentModule.selectedModule.index).
        then((data) => setFieldSettings(data)).
        catch(error => {
            console.log(error);
            if (error.status === 401) {
                navigate("/login");
            }
        });
    }, [currentModule.selectedModule]);
    
    
    const { t } = useTranslation();
    
    return (
        <div style={{width: "100%", display: "table"}}>
            {fieldSettings && fieldSettings.map((fieldSetting) => (
                <div className="row mb-3" style={{display: "table-row"}}>
                    {t(fieldSetting.labelKey)}
                </div>
            ))
            }
        </div>
    );
    
}