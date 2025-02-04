import { useLocation, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { fetchItem, fetchReferences, saveItem, type Field, type Item, type References } from "../../services/datacrow_api";
import { RequireAuth } from "../../context/authentication_context";
import { useModule } from "../../context/module_context";
import { Button, Tab, Tabs } from "react-bootstrap";
import { FormProvider, useForm } from 'react-hook-form';
import { useTranslation } from "../../context/translation_context";
import Form from 'react-bootstrap/Form';
import InputField from "../../components/input/dc_input_field";
import PictureEditList from "../../components/pictures_edit_list";
import ChildrenOverview from "../../components/item_overview_children";

export function ItemPage() {

    const [selectedTab, setSelectedTab] = useState('details');
    const [item, setItem] = useState<Item>();
    const [references, setReferences] = useState<References[]>();
    const [validated, setValidated] = useState(false);
    const currentModule = useModule();
    const navigate = useNavigate();
    const { state } = useLocation();
    const methods = useForm();
    const { t } = useTranslation();

    useEffect(() => {
        if (!state) {
            navigate('/');
        }
    }, []);

    useEffect(() => {
        state && currentModule.selectedModule && fetchItem(currentModule.selectedModule.index, state.itemID).
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
        
        saveItem(currentModule.selectedModule.index, data);
        
        console.log("Okay", data);
    }
        
        
    const onError = (errors: any, e: any) => {
        console.log("Error", errors);
    }

    return (
        <RequireAuth>
            <div style={{ display: "inline-block", width: "100%", textAlign: "left" }} key="div-item-details">
            
                <Tabs
                    defaultActiveKey="profile"
                    id="uncontrolled-tab-example"
                    activeKey={selectedTab}
                    onSelect={(k) => k && setSelectedTab(k)}
                    className="mb-3">

                    <Tab eventKey="details" title={t("lblDetails")}>
                        <FormProvider {...methods}>
                            <Form key="form-item-detail" noValidate validated={validated} onSubmit={methods.handleSubmit(onSubmit)}>
                                {references && item?.fields.map((fieldValue) => (
                                    <InputField
                                        field={fieldValue.field}
                                        value={fieldValue.value}
                                        references={ReferencesForField(fieldValue.field)}
                                    />
                                ))}
                                
                                <input
                                    type="text"
                                    value={currentModule.selectedModule && currentModule.selectedModule.index}
                                    key={"moduleIdx"}
                                    hidden={true}
                                />
                                
                                <Button type="submit" key="item-details-submit-button">
                                    {t("lblSave")}
                                </Button>
                            </Form>
                        </FormProvider>
                    </Tab>

                    {(item?.id && currentModule.selectedModule.hasChild) &&
                        (
                            <Tab eventKey="children" title={t(currentModule.selectedModule.child.name)}>
                                <ChildrenOverview itemID={item?.id} />
                            </Tab>
                        )
                    }
    
                    {state &&
                    <Tab eventKey="images" title={t("lblPictures")}>
                        <PictureEditList itemID={state.itemID} />
                    </Tab>
                    }
                    
                    {state &&
                    <Tab eventKey="attachments" title={t("lblAttachments")}>
                        <div>
                        </div>
                    </Tab>
                    }

                </Tabs>
            </div>
        </RequireAuth>
    );
}
