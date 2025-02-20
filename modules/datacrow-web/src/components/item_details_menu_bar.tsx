import { useNavigate } from "react-router-dom";
import { useAuth } from "../context/authentication_context";

interface Props {
    itemID: string;
}

export default function ItemDetailsMenu({itemID} : Props)  {
    
    const navigate = useNavigate();
    const auth = useAuth();
    
    function handleShowSettings() {
        navigate('/fieldsettings',
            { replace: true, state: { navFrom: "/item", itemID }}); 
            // Replace is needed here; the current page in history will be replaced by the settings.
            // The settings will replace the settings url with the url that is navigate to - which will be the navFrom.
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