import { useNavigate } from "react-router-dom";
import { useAuth } from "../../context/authentication_context";
import { Button, Modal } from "react-bootstrap";
import { useState } from "react";
import { useTranslation } from "../../context/translation_context";
import { deleteItem } from "../../services/datacrow_api";
import { useMessage } from "../../context/message_context";
import BusyModal from "../message/busy_modal";

interface Props {
    moduleIdx: number;
    itemID: string | undefined;
    editMode: boolean;
    navigateBackTo: string;
}

export default function ChildrenOverviewSettingsMenu({moduleIdx, itemID, navigateBackTo} : Props)  {
    
    const navigate = useNavigate();
    const auth = useAuth();
    
    function handleShowSettings() {
        navigate('/fieldsettings', { state: { navFrom: navigateBackTo, moduleIdx, itemID }}); 
    }
    
    return (
        <div style={{ float: "right", width: "100%", marginBottom: "20px" }}>
            <div style={{ float: "right" }} className="float-child">
                {auth.user && auth.user.admin &&
                    <i className="bi bi-gear-fill menu-icon" style={{ marginLeft: "5px" }} onClick={() => handleShowSettings()}></i>}
            </div>
        </div>
    );
}