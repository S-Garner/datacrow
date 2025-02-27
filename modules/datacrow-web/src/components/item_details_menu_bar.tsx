import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/authentication_context";
import { useState } from "react";

interface Props {
    itemID: string;
    formTitle: string | undefined;
    navigateBackTo: string;
}

export default function ItemDetailsMenu({itemID, formTitle, navigateBackTo} : Props)  {
    
    const navigate = useNavigate();
    const auth = useAuth();
    
    function handleShowSettings() {
        navigate('/fieldsettings',
            { state: { navFrom: navigateBackTo, itemID }}); 
    }     
    
    return (
         <div style={{ float: "right", width: "100%", marginBottom: "20px" }}>
            <div style={{float:"right"}} className="float-child">
                
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