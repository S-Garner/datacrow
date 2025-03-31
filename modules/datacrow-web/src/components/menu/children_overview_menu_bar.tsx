import { useNavigate } from "react-router-dom";
import { useAuth } from "../../context/authentication_context";
import { Button, Modal } from "react-bootstrap";
import { useState } from "react";
import { useTranslation } from "../../context/translation_context";
import { deleteItem } from "../../services/datacrow_api";
import { useMessage } from "../../context/message_context";
import BusyModal from "../message/busy_modal";
import { useModule } from "../../context/module_context";

interface Props {
    moduleIdx: number;
    itemID: string | undefined;
    title: string;
    editMode: boolean;
    navigateBackTo: string;
    parentModuleIdx: number;
}

export default function ChildrenOverviewSettingsMenu({moduleIdx, itemID, editMode, navigateBackTo, parentModuleIdx, title} : Props)  {
    
    const navigate = useNavigate();
    const auth = useAuth();
    
    const moduleContext = useModule();
    const module = moduleContext?.getModule(moduleIdx);
    
    
    function handleShowSettings() {
        navigate('/overviewfieldsettings', { state: { navFrom: navigateBackTo, moduleIdx, itemID, parentModuleIdx }}); 
    }
    
    function handleCreateNew() {
        navigate('/item_create', {state: { moduleIdx, parentID : itemID }});
    }
    
    return (
        <div style={{ float: "right", width: "100%", marginBottom: "5px" }}>
            <div style={{ float: "right" }} className="float-child">

                {!module?.isAbstract && editMode &&
                    <i className="bi bi-plus-circle menu-icon" style={{ fontSize: "1.7rem"}} onClick={() => handleCreateNew()} ></i>}

                {auth.user && auth.user.admin &&
                    <i className="bi bi-gear-fill menu-icon" style={{ marginLeft: "5px" }} onClick={() => handleShowSettings()}></i>}
            </div>
            
            <div style={{float:"left"}} className="float-child text-primary">
                {title}
            </div>             
        </div>
    );
}