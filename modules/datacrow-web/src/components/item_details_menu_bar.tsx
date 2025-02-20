import { useNavigate } from "react-router-dom";
import { useItemNavigation } from "../context/navigation_context";
import { useAuth } from "../context/authentication_context";

interface Props {
    itemID: string;
    allowNavigation: boolean;
}

export default function ItemDetailsMenu({itemID, allowNavigation} : Props)  {
    
    const navigate = useNavigate();
    const itemNavigation = useItemNavigation();
    const auth = useAuth();
    
    const previousPage = allowNavigation && itemNavigation.previousPage();
    
    const back = () => {
        console.log(previousPage);
        
        if (previousPage) {
            let parameters = previousPage.parameters;
            
            console.log(parameters);
            
            if (parameters)
                navigate(previousPage.address, parameters);
            else
                navigate(previousPage.address);
        }
    }
    
    function handleShowSettings() {
        navigate('/fieldsettings', 
            { state: { navFrom: "/item", itemID }});
    }     
    
    return (
         <div style={{ float: "right", width: "100%", marginBottom: "20px" }}>
        
            <div style={{float:"right"}} className="float-child">
                
                {previousPage &&
                    <i className="bi bi-arrow-left-circle-fill menu-icon" onClick={() => {back()}}></i>}
                    
                <i className="bi bi-house-fill menu-icon" onClick={() => {navigate('/')}} ></i>
                
                {auth.user && auth.user.admin &&
                    <i className="bi bi-tools menu-icon" style={{marginLeft: "10px"}} onClick={() => handleShowSettings()} ></i>}

            </div>
        </div>
    );
}