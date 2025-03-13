import { type RelatedItem } from "../../services/datacrow_api";
import { useNavigate } from "react-router-dom";
import { Card } from "react-bootstrap";

type Props = {
    relatedItems: RelatedItem[];
}

export default function RelatedItemList({relatedItems} : Props) {
    
    const navigate = useNavigate();
    
    const handleOpen = (moduleIdx: number, itemID: string) => {
        navigate('/item_view', { state: { itemID, moduleIdx, tab : "details" }});
    } 
    
    return (
        <div className="py-20 bg-slate-900 h-full" style={{width: "100%"}}>
        
        <div style={{ display: "flex", flexWrap: "wrap", float: "left", clear: "both" }}>
        
            {relatedItems && relatedItems.map((relatedItem) => (

                <Card style={{ width: '18rem', border: "none" }} key={"card-pic-" + relatedItem.id} 
                        onClick={() => handleOpen(relatedItem.moduleIdx, relatedItem.id)}>
                        
                    {relatedItem.scaledImageUrl ? <Card.Img src={relatedItem.scaledImageUrl + "?" + Date.now()} /> : <div style={{ height: '300px' }} />}
                    
                    <Card.Header style={{ border: "none" }}>
                        {relatedItem.name}
                    </Card.Header>
                    
                </Card>
            ))}
            
            </div>
        </div>
    );
}