import { useNavigate } from "react-router-dom";
import { useAuth } from "../../context/authentication_context";
import { Button, Modal } from "react-bootstrap";
import { useEffect, useState } from "react";
import { useTranslation } from "../../context/translation_context";
import { deleteItem, isEditingAllowed } from "../../services/datacrow_api";
import { useMessage } from "../../context/message_context";
import BusyModal from "../message/busy_modal";
import { useModule } from "../../context/module_context";

interface Props {
    moduleIdx: number;
    itemID: string | undefined;
    parentID: string | undefined;
    formTitle: string | undefined;
    editMode: boolean;
    navigateBackTo: string;
}

export default function ItemDetailsMenu({moduleIdx, itemID, parentID, formTitle, navigateBackTo, editMode} : Props)  {
    
    const [editingAllowed, setEditingAllowed] = useState(false);
    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
    const [deleting, setDeleting] = useState(false);
    
    useEffect(() => {
        moduleIdx && isEditingAllowed(moduleIdx).
            then((data) => setEditingAllowed(data)).
            catch(error => {
                console.log(error);
                if (error.status === 401) {
                    navigate("/login");
                }
            });
    }, [moduleIdx]);        
    
    const navigate = useNavigate();
    const message = useMessage();
    const auth = useAuth();
    
    const moduleContext = useModule();
    
    const { t } = useTranslation();
    
    function handleShowSettings() {
        navigate('/fieldsettings', { state: { navFrom: navigateBackTo, itemID, moduleIdx }}); 
    }
    
    function handleToEditMode() {
        navigate('/item_edit', { replace: true, state: { itemID, moduleIdx }}); 
    }
    
    function handleToViewMode() {
        navigate('/item_view', { replace: true, state: { itemID, moduleIdx }}); 
    }
    
    function handleDelete() {
        setShowDeleteConfirm(true);
    }

    function handleDeleteAfterConfirm() {
        setShowDeleteConfirm(false);
        setDeleting(true);
        
        if (itemID) {
            deleteItem(itemID, moduleIdx).
                then(() => {
                    setDeleting(false);
                    
                    if (parentID) {
                        let parentModuleIdx = moduleContext.selectedModule.index; 
                        navigate('/item_view', { state: { itemID: parentID, moduleIdx: parentModuleIdx }});
                    } else {
                        navigate('/');
                    }
                }).
                catch(error => {
                    setDeleting(false);
                    if (error.status === 401) {
                        navigate("/login");
                    } else {
                        message.showMessage(error.response.data);
                    }
                });
        }
    }
    
    return (
         <div style={{ float: "right", width: "100%", marginBottom: "20px" }}>
         
           <BusyModal show={deleting} message={t("msgBusyDeletingItem")} />
         
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

                {(itemID && editMode && auth.user.admin) && <i className="bi bi-trash-fill menu-icon" onClick={() => {handleDelete()}} ></i>}
                
                {(itemID && !editMode && editingAllowed) && <i className="bi bi-pen-fill menu-icon" onClick={() => {handleToEditMode()}} ></i>}
                
                {(itemID && editMode) && <i className="bi bi-eye-fill menu-icon" onClick={() => {handleToViewMode()}} ></i>}
                
                <i className="bi bi-house-fill menu-icon" style={{marginLeft: "5px"}} onClick={() => {navigate('/')}} ></i>
                
                {itemID && auth.user && auth.user.admin &&
                    <i className="bi bi-gear-fill menu-icon" style={{marginLeft: "5px"}} onClick={() => handleShowSettings()} ></i>}
            </div>
            
            <div style={{float:"left"}} className="float-child text-primary">
                {formTitle}
            </div>            
        </div>
    );
}