import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "../context/translation_context";
import { fetchFieldSettings, saveFieldSettings, type FieldSetting } from "../services/datacrow_api";
import { useModule } from "../context/module_context";
import { Button, Card } from "react-bootstrap";
import { useForm } from "react-hook-form";

export default function FieldSettingsOverview() {
    
    const [fieldSettings, setFieldSettings] = useState<FieldSetting[]>();
    
    const currentModule = useModule();
    const navigate = useNavigate();
    const methods = useForm();
    
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

    function arrayMove(subject: FieldSetting, steps: number) {
        if (fieldSettings) {
            
            const clone = fieldSettings.slice(0);
            
            let index = clone.findIndex((fieldSetting) => fieldSetting === subject);
            let newIndex = index + steps;
            let element = clone[index];
            
            clone.splice(index, 1);
            clone.splice(newIndex, 0, element);
            
            for (let i = 0; i < clone.length; i++) {
                clone[i].order = i;
            }

            setFieldSettings(clone);
        }
    }
    
    const handleToggle = (subject: FieldSetting) => {
        if (fieldSettings) {
            
            const clone = fieldSettings.slice(0);
            
            let index = clone.findIndex((fieldSetting) => fieldSetting === subject);
            let element = clone[index];
            element.enabled = !element.enabled;
            
            setFieldSettings(clone);
        }
    }
    
    const handleMoveDown = (fieldSetting: FieldSetting) => {
        arrayMove(fieldSetting, 1);
    }
    
    const handleMoveUp = (fieldSetting: FieldSetting) => {
        arrayMove(fieldSetting, -1);
    }
    
    const { t } = useTranslation();
    
    const onSubmit = (_data: any, e: any) => {
        e.preventDefault();
        
        if (fieldSettings) {
            saveFieldSettings(currentModule.selectedModule.index, fieldSettings);    
        }
    }
    
    return (
        <div style={{ display: "flex", flexWrap: "wrap" }}>

            <form key="field-settings-form" onSubmit={methods.handleSubmit(onSubmit)}>

                {fieldSettings && fieldSettings.map((fieldSetting, counter) => (

                    <Card style={{ width: '100%' }} key={"card-field-setting-" + fieldSetting.fieldIdx}>
                        <Card.Body style={{ width: '100%' }} key={""} >

                            <div className="float-container" style={{ marginTop: "20px" }}>
                                <div className="float-child">
                                    <input 
                                        type="checkbox"
                                        id={"field-index-" + fieldSetting.fieldIdx}
                                        key={"field-index-" + fieldSetting.fieldIdx} 
                                        onChange={() => handleToggle(fieldSetting)}
                                        disabled={fieldSetting.locked}
                                        checked={fieldSetting.locked || (fieldSetting.enabled as boolean)}
                                    />
                                </div>
                                
                                <div className="float-child" style={{ marginLeft: "20px", width: "60px", marginRight: "30px" }}>
                                    {(counter < fieldSettings.length - 1) &&
                                        (<i className="bi bi-arrow-down" style={{ fontSize: "1.2rem", marginRight: "10px" }} onClick={() => handleMoveDown(fieldSetting)}></i>)
                                    }

                                    {(counter++ != 0) &&
                                        (<i className="bi bi-arrow-up" style={{ fontSize: "1.2rem", }} onClick={() => handleMoveUp(fieldSetting)}></i>)
                                    }
                                </div>

                                <div className="float-child">
                                    {t(fieldSetting.labelKey)}
                                </div>
                            </div>
                        </Card.Body>
                    </Card>
                ))}

                <Button type="submit" key="field-settings-submit-button">
                    {t("lblSave")}
                </Button>
            </form>
        </div>
    );
    
}