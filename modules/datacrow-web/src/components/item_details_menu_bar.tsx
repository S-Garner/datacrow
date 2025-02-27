import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/authentication_context";
import { useState } from "react";

interface Props {
    itemID: string;
    formTitle: string | undefined;
    allowEditMode: boolean;
    navigateBackTo: string;
}

export default function ItemDetailsMenu({itemID, formTitle, navigateBackTo, allowEditMode} : Props)  {
    
    const navigate = useNavigate();
    const auth = useAuth();
    
    function handleShowSettings() {
        navigate('/fieldsettings', {state: { navFrom: navigateBackTo, itemID }}); 
    }
    
    function handleToEditMode() {
        navigate('/item_edit', { replace: true, state: { itemID }}); 
    }
    
    return (
         <div style={{ float: "right", width: "100%", marginBottom: "20px" }}>
            <div style={{float:"right"}} className="float-child">
                
                {allowEditMode && <i className="bi bi-house menu-icon" onClick={() => {handleToEditMode()}} ></i>}
                
                <i className="bi bi-house-fill menu-icon" onClick={() => {navigate('/')}} ></i>
                
                {auth.user && auth.user.admin &&
                    <i className="bi bi-tools menu-icon" style={{marginLeft: "10px"}} onClick={() => handleShowSettings()} ></i>}
            </div>
            
            <div style={{float:"left"}} className="float-child text-primary">
                {formTitle}
            </div>            
        </div>
    );
}