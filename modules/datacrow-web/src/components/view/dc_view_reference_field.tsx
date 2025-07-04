import { Badge } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import type { Field, Reference } from "src/services/datacrow_api";

export interface Props {
    field: Field,
    reference: Reference,
}

export default function ViewReferenceField({
    field,
    reference
}: Props) {
    
    const navigate = useNavigate();
    const itemID = reference.id;
    
    const handleOpen = () => {
        let moduleIdx = field.referencedModuleIdx; 
        navigate('/item_view', { state: { itemID, moduleIdx }});
    } 
    
    return (
        <Badge bg="secondary" style={{ height: "2.5em", alignItems: "center", display: "flex"}} onClick={handleOpen}>
            {reference.iconUrl && (<img
                src={reference.iconUrl + '?' + Date.now()}
                style={{ width: "24px", paddingRight: "8px" }} />)}

            {reference.name}
        </Badge>
    )
}