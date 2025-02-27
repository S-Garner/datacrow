import { useNavigate } from "react-router-dom";
import { useAuth } from "../../context/authentication_context";

interface Props {
    itemID: string;
    formTitle: string | undefined;
    editMode: boolean;
    navigateBackTo: string;
}

export default function ItemDetailsMenu({itemID, formTitle, navigateBackTo, editMode} : Props)  {
    
    const navigate = useNavigate();
    const auth = useAuth();
    
    function handleShowSettings() {
        navigate('/fieldsettings', { state: { navFrom: navigateBackTo, itemID }}); 
    }
    
    function handleToEditMode() {
        navigate('/item_edit', { replace: true, state: { itemID }}); 
    }
    
    function handleToViewMode() {
        navigate('/item_view', { replace: true, state: { itemID }}); 
    }    
    
    return (
         <div style={{ float: "right", width: "100%", marginBottom: "20px" }}>
            <div style={{float:"right"}} className="float-child">
                
                {!editMode && <i className="bi bi-pen-fill menu-icon" onClick={() => {handleToEditMode()}} ></i>}
                
                {editMode && <i className="bi bi-eye-fill menu-icon" onClick={() => {handleToViewMode()}} ></i>}
                
                <i className="bi bi-house-fill menu-icon" style={{marginLeft: "5px"}} onClick={() => {navigate('/')}} ></i>
                
                {auth.user && auth.user.admin &&
                    <i className="bi bi-gear-fill menu-icon" style={{marginLeft: "5px"}} onClick={() => handleShowSettings()} ></i>}
            </div>
            
            <div style={{float:"left"}} className="float-child text-primary">
                {formTitle}
            </div>            
        </div>
    );
}