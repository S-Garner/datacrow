import { useNavigate } from "react-router-dom";
import { useAuth } from "../../context/authentication_context";
import { Button, Modal } from "react-bootstrap";
import { useState } from "react";
import { useTranslation } from "../../context/translation_context";
import { deleteItem } from "../../services/datacrow_api";
import { useMessage } from "../../context/message_context";

interface Props {
    moduleIdx: number;
    itemID: string;
    formTitle: string | undefined;
    editMode: boolean;
    navigateBackTo: string;
}

export default function ItemDetailsMenu({moduleIdx, itemID, formTitle, navigateBackTo, editMode} : Props)  {
    
    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
    
    const navigate = useNavigate();
    const message = useMessage();
    const auth = useAuth();
    
    const { t } = useTranslation();
    
    function handleShowSettings() {
        navigate('/fieldsettings', { state: { navFrom: navigateBackTo, itemID }}); 
    }
    
    function handleToEditMode() {
        navigate('/item_edit', { replace: true, state: { itemID }}); 
    }
    
    function handleToViewMode() {
        navigate('/item_view', { replace: true, state: { itemID }}); 
    }
    
    function handleDelete() {
        setShowDeleteConfirm(true);
    }

    function handleDeleteAfterConfirm() {
        setShowDeleteConfirm(false);
        deleteItem(itemID, moduleIdx).
            then(() => navigate('/')).
            catch(error => {
                if (error.status === 401) {
                    navigate("/login");
                } else {
                    message.showMessage(error.response.data);
                }
            });
    }
    
    return (
         <div style={{ float: "right", width: "100%", marginBottom: "20px" }}>
         
         
           <Modal centered show={showDeleteConfirm} onHide={() => setShowDeleteConfirm(false)}>
                <Modal.Body>{t('msgDeleteAreYouSure')}</Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={() => setShowDeleteConfirm(false)}>
                        {t("lblClose")}
                    </Button>
                    <Button variant="primary" onClick={() => handleDeleteAfterConfirm()}>
                        {t("lblOK")}
                    </Button>
                </Modal.Footer>
            </Modal>
         
         
            <div style={{float:"right"}} className="float-child">
                
                {!editMode && <i className="bi bi-pen-fill menu-icon" onClick={() => {handleToEditMode()}} ></i>}
                
                {editMode && <i className="bi bi-eye-fill menu-icon" onClick={() => {handleToViewMode()}} ></i>}
                
                {(editMode && auth.user.admin) && <i className="bi bi-trash-fill menu-icon" onClick={() => {handleDelete()}} ></i>}
                
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