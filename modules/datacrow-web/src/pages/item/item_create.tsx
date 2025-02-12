import { useLocation, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { fetchItem, fetchReferences, saveItem, type Field, type Item, type References } from "../../services/datacrow_api";
import { RequireAuth } from "../../context/authentication_context";
import { useModule } from "../../context/module_context";
import { Button, Modal, Tab, Tabs } from "react-bootstrap";
import { FormProvider, useForm } from 'react-hook-form';
import { useTranslation } from "../../context/translation_context";
import Form from 'react-bootstrap/Form';
import InputField from "../../components/input/dc_input_field";
import PictureEditList from "../../components/pictures_edit_list";
import ChildrenOverview from "../../components/item_overview_children";
import { useMessage } from "../../context/message_context";
import AttachmentEditList from "../../components/attachment_edit_list";

export function ItemCreatePage() {

    const [selectedTab, setSelectedTab] = useState('details');
    const [references, setReferences] = useState<References[]>();
    const currentModule = useModule();
    const message = useMessage();
    const navigate = useNavigate();
    const methods = useForm();
    const { t } = useTranslation();

    useEffect(() => {
        currentModule.selectedModule && fetchReferences(currentModule.selectedModule.index).
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
        saveItem(currentModule.selectedModule.index, data).
        then((itemID) => navigate('/item', { state: { itemID }})).
        catch(error => {
            if (error.status === 401) {
                navigate("/login");
            } else {
                console.log(error);
                message.showMessage(error.response.data);
            }
        });
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
                        <FormProvider {...methods}>
                            <Form key="form-item-detail" validated={false} onSubmit={methods.handleSubmit(onSubmit)}>
                                
                                {references && currentModule.mainModule.fields.map((field) => (
                                    <InputField
                                        field={field}
                                        value={undefined}
                                        references={ReferencesForField(field)}
                                    />
                                ))}
                                
                                <Button type="submit" key="item-details-submit-button">
                                    {t("lblSave")}
                                </Button>
                                
                            </Form>
                        </FormProvider>
                    </Tab>

                </Tabs>
            </div>
        </RequireAuth>
    );
}
