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
    };

    return (
        <div ref={setNodeRef} style={style} {...attributes} {...listeners}>
            <Card key={"card-field-setting-" + fieldSetting.fieldIdx}>
                <Card.Body key="card-field-settings-body">

                    <div className="float-container" style={{ marginTop: "20px" }}>
                        <div className="float-child" style={{ marginRight: "10px" }}>
                            <input
                                type="checkbox"
                                id={"field-index-" + fieldSetting.fieldIdx}
                                key={"field-index-" + fieldSetting.fieldIdx}
                                //onChange={() => handleToggle(fieldSetting)}
                                disabled={fieldSetting.locked}
                                checked={fieldSetting.locked || (fieldSetting.enabled as boolean)}
                            />
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