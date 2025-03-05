import { useSortable } from '@dnd-kit/sortable';
import { CSS } from '@dnd-kit/utilities';
import type { FieldSetting } from '../../services/datacrow_api';
import { Card } from 'react-bootstrap';
import { useTranslation } from '../../context/translation_context';

export function SortableItem(props: { fieldSetting: FieldSetting }) {
    
    let fieldSetting = props.fieldSetting;
    
    const { t } = useTranslation();
    
    const {
        attributes,
        listeners,
        setNodeRef,
        transform,
        transition,
    } = useSortable({ id: fieldSetting.id });

    const style = {
        transform: CSS.Transform.toString(transform),
        transition,
        touchAction: 'none'
    };
    
    return (
        <div ref={setNodeRef} style={style} {...attributes} {...listeners}>
            <Card key={"card-field-setting-" + fieldSetting.fieldIdx} className="droppable-field">
                <Card.Body key="card-field-settings-body">
                    <div className="float-container" >
                        <div className="float-child droppable-field-icon">
                            <i className="bi bi-arrows-move"></i>
                        </div>
                        <div className="float-child">
                            {t(fieldSetting.labelKey)}
                        </div>
                    </div>
                </Card.Body>
            </Card>
        </div>
    );
}