import { RequireAuth } from "../../context/authentication_context";
import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useTranslation } from "../../context/translation_context";
import { type OverviewFieldSetting, fetchOverviewFieldSettings, saveOverviewFieldSettings } from "../../services/datacrow_api";
import { Button, Card, Tab, Tabs } from "react-bootstrap";
import { useForm } from "react-hook-form";
import { useMessage } from "../../context/message_context";

export function OverviewFieldSettingsPage() {

    const [fieldSettings, setFieldSettings] = useState<OverviewFieldSetting[]>();

    const navigate = useNavigate();
    const methods = useForm();
    const message = useMessage();
    const {state} = useLocation();
    
    const navigateBackTo = state.navFrom;
    
    useEffect(() => {
        if (!state) {
            navigate('/login');
        }
    }, []);
    
    let itemID = state?.itemID;
    let moduleIdx = state?.moduleIdx;
    let parentModuleIdx = state?.parentModuleIdx;
    
    useEffect(() => {
        moduleIdx && fetchOverviewFieldSettings(moduleIdx).
            then((data) => setFieldSettings(data)).
            catch(error => {
                console.log(error);
                if (error.status === 401) {
                    navigate("/login");
                }
            });
    }, [moduleIdx]);

    function arrayMove(subject: OverviewFieldSetting, steps: number) {
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
    
    const handleToggle = (subject: OverviewFieldSetting) => {
        if (fieldSettings) {

            const clone = fieldSettings.slice(0);

            let index = clone.findIndex((fieldSetting) => fieldSetting === subject);
            let element = clone[index];
            element.enabled = !element.enabled;

            setFieldSettings(clone);
        }
    }

    const handleMoveDown = (fieldSetting: OverviewFieldSetting) => {
        arrayMove(fieldSetting, 1);
    }

    const handleMoveUp = (fieldSetting: OverviewFieldSetting) => {
        arrayMove(fieldSetting, -1);
    }

    const { t } = useTranslation();

    const onSubmit = (_data: any, e: any) => {
        e.preventDefault();

        if (fieldSettings && itemID && moduleIdx) {
            saveOverviewFieldSettings(moduleIdx, fieldSettings).
            then(() => navigate(navigateBackTo + "#children", { state: { itemID, moduleIdx: parentModuleIdx, tab: "children" }})).
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

                <Tabs key="item-details-tabs" className="mb-3">

                    <Tab title="Field Settings" active={true}>

                        <form key="field-settings-form" onSubmit={methods.handleSubmit(onSubmit)}>

                            {fieldSettings && fieldSettings.map((fieldSetting, counter) => (

                                <Card style={{ width: '80%', marginLeft: "10%", marginRight: "10%" }} key={"card-field-setting-" + fieldSetting.fieldIdx}>
                                    <Card.Body key="card-field-settings-body">

                                        <div className="float-container" style={{ marginTop: "20px" }}>
                                            <div className="float-child">
                                                <input
                                                    type="checkbox"
                                                    id={"field-index-" + fieldSetting.fieldIdx}
                                                    key={"field-index-" + fieldSetting.fieldIdx}
                                                    onChange={() => handleToggle(fieldSetting)}
                                                    checked={fieldSetting.enabled}
                                                />
                                            </div>

                                            <div className="float-child" style={{ marginLeft: "20px", width: "50px", marginRight: "10px" }}>
                                                {(counter < fieldSettings.length - 1) &&
                                                    (<i className="bi bi-arrow-down" style={{ fontSize: "1.2rem", marginRight: "10px" }} onClick={() => handleMoveDown(fieldSetting)}></i>)
                                                }
                                                {(counter != 0) &&
                                                    (<i className="bi bi-arrow-up" style={{ fontSize: "1.2rem" }} onClick={() => handleMoveUp(fieldSetting)}></i>)
                                                }
                                             </div>

                                            <div className="float-child">
                                                {t(fieldSetting.labelKey)}
                                            </div>
                                        </div>
                                    </Card.Body>
                                </Card>
                            ))}

                            <div className="mb-3" style={{ marginLeft: "10%", marginTop: "10px"}}>
                                <Button type="submit" key="field-settings-submit-button">
                                    {t("lblSave")}
                                </Button>
                            </div>
                        </form>
                    </Tab>
                </Tabs>

            </div>
        </RequireAuth>
    );
}

    /*
    function arrayMoveTop(subject: OverviewFieldSetting) {
        if (fieldSettings) {

            const clone = fieldSettings.slice(0);

            let index = clone.findIndex((fieldSetting) => fieldSetting === subject);
            let element = clone[index];

            clone.splice(index, 1);
            clone.unshift(element);

            for (let i = 0; i < clone.length; i++) {
                clone[i].order = i;
            }

            setFieldSettings(clone);
        }
    }
    
    function arrayMoveBottom(subject: OverviewFieldSetting) {
        if (fieldSettings) {

            const clone = fieldSettings.slice(0);

            let index = clone.findIndex((fieldSetting) => fieldSetting === subject);
            let element = clone[index];

            clone.splice(index, 1);
            clone.splice(clone.length, 0, element);

            for (let i = 0; i < clone.length; i++) {
                clone[i].order = i;
            }

            setFieldSettings(clone);
        }
    }      */  