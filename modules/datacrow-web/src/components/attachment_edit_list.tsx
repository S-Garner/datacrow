import { useEffect, useState } from "react";
import { Button, Modal } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "../context/translation_context";
import { deleteAttachment, type Attachment, fetchAttachments } from "../services/datacrow_api";

type Props = {
  itemID: string;
};

export default function AttachmentEditList({itemID} : Props) {

    const [attachments, setAttachments] = useState<Attachment[]>();
    const [attachment, setAttachment] = useState<Attachment>();

    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
    const { t } = useTranslation();
    const navigate = useNavigate();
    
    useEffect(() => {
        itemID && fetchAttachments(itemID).
            then((data) => setAttachments(data)).
            catch(error => {
                console.log(error);
                if (error.status === 401) {
                    navigate("/login");
                }
            });
    }, [itemID]);
    
    function handleDelete(attachment: Attachment) {
        setAttachment(attachment);
        setShowDeleteConfirm(true);
    }
    
    function handleDeleteAfterConfirm() {
        
        setShowDeleteConfirm(false);
        
        if (attachment) {
            deleteAttachment(attachment.objectID, attachment.name).
                then((data) => setAttachments(data)).
                catch(error => {
                    console.log(error);
                    if (error.status === 401) {
                        navigate("/login");
                    }
                });
        } 
    }

    return (
        <div>

            <Modal centered show={showDeleteConfirm} onHide={() => setShowDeleteConfirm(false)}>
                <Modal.Body>{t('msgDeleteAttachmentConfirmation')}</Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={() => setShowDeleteConfirm(false)}>
                        {t("lblClose")}
                    </Button>
                    <Button variant="primary" onClick={() => handleDeleteAfterConfirm()}>
                        {t("lblOK")}
                    </Button>
                </Modal.Footer>
            </Modal>
            
            <div style={{width: "100%", display: "table"}}>
                {attachments && attachments.map((attachment) => (
                    <div className="row mb-3" style={{display: "table-row"}}>
                    
                        <div style={{width: "95%", display: "table-cell"}}>
                            {String(attachment.displayName)}
                        </div>

                        <div style={{width: "5%", display: "table-cell"}}>
                            <Button />
                        </div>
                    </div>
                ))} 
            </div>    
        </div>
    );
}
