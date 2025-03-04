import {
    DndContext,
    closestCenter,
    KeyboardSensor,
    PointerSensor,
    useSensor,
    useSensors,
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
import { Tabs, Tab } from 'react-bootstrap';
import { SortableItem } from './droppable';

export function FieldSettingsPage() {

    const [fieldSettings, setFieldSettings] = useState<FieldSetting[]>();
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

    const handleToggle = (subject: FieldSetting) => {
        if (fieldSettings) {

            const clone = fieldSettings.slice(0);

            let index = clone.findIndex((fieldSetting) => fieldSetting === subject);
            let element = clone[index];
            element.enabled = !element.enabled;

            setFieldSettings(clone);
        }
    }

    const { t } = useTranslation();

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
                    console.log("Error");
                    return fieldSettings;
                }
            });
        }
    }
    
    return (
        <div style={{ display: "inline-block", width: "100%", textAlign: "left" }} key="div-item-details">
            <Tabs key="item-details-tabs" className="mb-3">
                <Tab title="Field Settings" active={true}>
                    {fieldSettings && ( <DndContext
                        sensors={sensors}
                        collisionDetection={closestCenter}
                        onDragEnd={handleDragEnd}>
                        <SortableContext
                            items={fieldSettings}
                            strategy={verticalListSortingStrategy}>
                            
                            {fieldSettings && fieldSettings.map(fieldSetting => <SortableItem key={fieldSetting.fieldIdx} id={fieldSetting.fieldIdx} />)}
                        </SortableContext>
                    </DndContext>) }
                </Tab>
            </Tabs>
        </div>
    );
}