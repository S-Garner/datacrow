import { type RelatedItem } from "../../services/datacrow_api";
import { useNavigate } from "react-router-dom";
import { Card } from "react-bootstrap";
import Pagination from "../overview/pagination";
import { useState } from "react";

type Props = {
    relatedItems: RelatedItem[];
}

export default function RelatedItemList({relatedItems} : Props) {
    
    const navigate = useNavigate();
    
    let startingPageNumber = Number(localStorage.getItem("related_pagenumber"));
    startingPageNumber = startingPageNumber <= 0 ? 1 : startingPageNumber;

    const [currentPage, setCurrentPage] = useState(startingPageNumber);
    
    const itemsPerPage = 30;
    
    const totalItems = relatedItems.length;
    const lastItemIndex = currentPage * itemsPerPage;
    const firstItemIndex = lastItemIndex - itemsPerPage;
    const currentItems = relatedItems.slice(firstItemIndex, lastItemIndex);
    
    const paginate = (pageNumber: number) => {
        setCurrentPage(pageNumber);
    };

    if (currentItems.length === 0 && totalItems > 0) {
        setCurrentPage(1);
    };    
    
    const handleOpen = (moduleIdx: number, itemID: string) => {
        savePageNumber();
        navigate('/item_view', { state: { itemID, moduleIdx, tab : "details" }});
    }
    
    function savePageNumber() {
        localStorage.setItem("related_pagenumber", String(currentPage));
    }
    
    return (
        <div className="py-20 bg-slate-900 h-full" style={{width: "100%"}}>
        
            <div style={{ float: "left", clear: "both" }}>
                <Pagination
                    itemsPerPage={itemsPerPage}
                    totalItems={totalItems}
                    currentPage={currentPage}
                    paginate={paginate}
                />
            </div>        
        
            <div style={{ display: "flex", flexWrap: "wrap", float: "left", clear: "both" }}>
                {currentItems && currentItems.map((relatedItem) => (
    
                    <Card style={{ width: '18rem', border: "none" }} key={"card-pic-" + relatedItem.id} 
                            onClick={() => handleOpen(relatedItem.moduleIdx, relatedItem.id)}>
                            
                        {relatedItem.scaledImageUrl ? <Card.Img src={relatedItem.scaledImageUrl + "?" + Date.now()} /> : <div style={{ height: '300px' }} />}
                        
                        <Card.Header style={{ border: "none" }}>
                            {relatedItem.name}
                        </Card.Header>
                        
                    </Card>
                ))}
            </div>
            
            <div style={{ float: "left", clear: "both" }}>
                <Pagination
                    itemsPerPage={itemsPerPage}
                    totalItems={totalItems}
                    currentPage={currentPage}
                    paginate={paginate}
                />
            </div>
        </div>
    );
}