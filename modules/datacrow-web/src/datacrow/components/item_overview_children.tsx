import { useEffect, useState } from "react";
import { useModule } from "../context/module_context";
import { fetchChildren, type Item } from "../services/datacrow_api";
import { Button, ListGroup } from "react-bootstrap";

type Props = {
    itemID: string;
};

export default function ChildrenOverview({itemID} : Props) {

    const [children, setChildren] = useState<Item[]>();
    const currentModule = useModule();

    useEffect(() => {
        itemID && fetchChildren(currentModule.selectedModule.child.index, itemID).
            then((data) => setChildren(data))
    }, [itemID]);

    return (
        <div style={{width: "100%", display: "table"}}>
            {children && children.map((child) => (
                <div className="row mb-3" style={{display: "table-row"}}>
                
                    {child.fields.map((fieldValue) => (
                                    <div style={{width: "25%", display: "table-cell"}}>
                        {String(fieldValue.value)}
                    </div>
                                ))}
                    
                    <div style={{width: "5%", display: "table-cell"}}>
                        <Button />
                    </div>
                </div>
            ))
            }
        </div>
    );
};