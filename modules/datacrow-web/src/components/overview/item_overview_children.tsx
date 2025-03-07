import { useEffect, useState } from "react";
import { useModule } from "../../context/module_context";
import { fetchChildren, type Item } from "../../services/datacrow_api";
import { Button, ListGroup, Table } from "react-bootstrap";
import ChildrenOverviewSettingsMenu from "../menu/children_overview_menu_bar";
import { useTranslation } from "../../context/translation_context";

type Props = {
    itemID: string;
    moduleIdx: number;
    navigateBackTo: string;
    parentModuleIdx: number;
    title: string;
};

export default function ChildrenOverview({moduleIdx, parentModuleIdx, itemID, navigateBackTo, title} : Props) {

    const [children, setChildren] = useState<Item[]>();
    const { t } = useTranslation();
    
    useEffect(() => {
        itemID && fetchChildren(moduleIdx, itemID).
            then((data) => setChildren(data))
    }, [itemID]);

    return (
        <>
            <ChildrenOverviewSettingsMenu
                moduleIdx={moduleIdx}
                editMode={true} 
                itemID={itemID}
                title={title}
                navigateBackTo={navigateBackTo}
                parentModuleIdx={parentModuleIdx} />        
        
            <Table bordered hover>
            
                <thead>
                    <tr>
                        {children?.at(0)?.fields.map((fieldValue) => (
                            <th className="text-secondary" style={{ textDecoration: 'none'}}>
                                {t(String(fieldValue.field.label))}
                            </th>
                        ))}
                    </tr>
                </thead>
            
            
                {children && children.map((child) => (
                    <tbody>
                        <tr>
                            {child.fields.map((fieldValue) => (
                                <td>
                                    {fieldValue.value && String(fieldValue.value)}
                                </td>
                            ))}
                        </tr>
                    </tbody>
                ))
                }
            </Table>
        </>
    );
};