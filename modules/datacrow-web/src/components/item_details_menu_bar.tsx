import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/authentication_context";

interface Props {
    itemID: string;
    navigateBackTo: string;
}

export default function ItemDetailsMenu({itemID, navigateBackTo} : Props)  {
    
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
        </div>
    );
}