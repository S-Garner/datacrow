import {
    DndContext,
    closestCenter,
    KeyboardSensor,
    PointerSensor,
    useSensor,
    useSensors,
    TouchSensor,
} from '@dnd-kit/core';
import {
    arrayMove,
    SortableContext,
    sortableKeyboardCoordinates,
    verticalListSortingStrategy,
} from '@dnd-kit/sortable';
import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { useLocation, useNavigate } from 'react-router-dom';
import { useMessage } from '../../context/message_context';
import { fetchFieldSettings, saveFieldSettings, type FieldSetting } from '../../services/datacrow_api';
import { useTranslation } from '../../context/translation_context';
import { Tabs, Tab, Button } from 'react-bootstrap';
import { SortableItem } from './droppable_field_setting';
import { RequireAuth } from '../../context/authentication_context';

export function FieldSettingsPage() {

    const [fieldSettings, setFieldSettings] = useState<FieldSetting[]>();
    const navigate = useNavigate();
    const methods = useForm();
    const message = useMessage();
    const {state} = useLocation();
    const { t } = useTranslation();
    
    const navigateBackTo = state.navFrom;
    
    useEffect(() => {
        if (!state) {
            navigate('/login');
        }
    }, []);
    
    let itemID = state?.itemID;
    let moduleIdx = state?.moduleIdx;
    
    useEffect(() => {
        moduleIdx && fetchFieldSettings(moduleIdx).
            then((data) => setFieldSettings(data)).
            catch(error => {
                console.log(error);
                if (error.status === 401) {
                    navigate("/login");
                }
            });
    }, [moduleIdx]);

    const onSubmit = (_data: any, e: any) => {
        e.preventDefault();

        if (fieldSettings && itemID && moduleIdx) {
            saveFieldSettings(moduleIdx, fieldSettings).
            then(() => navigate(navigateBackTo, { state: { itemID, moduleIdx }})).
            catch(error => {
                if (error.status === 401) {
                    navigate("/login");
                } else {
                    message.showMessage(error.response.data);
                }
            });
        }
    }

    const sensors = useSensors(
        useSensor(TouchSensor),
        useSensor(PointerSensor),
        useSensor(KeyboardSensor, {
            coordinateGetter: sortableKeyboardCoordinates,
        })
    );
  
    function handleDragEnd(event: { active: any; over: any; }) {

        const { active, over } = event;

        if (fieldSettings && active.id !== over.id) {
            setFieldSettings((fieldSettings) => {
                
                if (fieldSettings) {

                    const oldIndex = fieldSettings.findIndex((fieldSetting) => fieldSetting.fieldIdx === active.id);
                    const newIndex = fieldSettings.findIndex((fieldSetting) => fieldSetting.fieldIdx === over.id);
    
                    return arrayMove(fieldSettings, oldIndex, newIndex);
                    
                } else {
                    console.log("Drag-end error: field settings is null");
                    return fieldSettings;
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
                        
                            {fieldSettings && ( 
                                <div className='drag-and-drop'>
                                    <DndContext
                                        sensors={sensors}
                                        collisionDetection={closestCenter}
                                        onDragEnd={handleDragEnd}>
                                        <SortableContext
                                            items={fieldSettings}
                                            strategy={verticalListSortingStrategy}>
                                            
                                            {fieldSettings && fieldSettings.map(fieldSetting => 
                                                <SortableItem 
                                                    key={fieldSetting.fieldIdx}
                                                    fieldSetting={fieldSetting} />)}
                                                    
                                        </SortableContext>
                                    </DndContext>
                                </div>)}
                            
                            <div className="mb-3" style={{ marginTop: "10px"}}>
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