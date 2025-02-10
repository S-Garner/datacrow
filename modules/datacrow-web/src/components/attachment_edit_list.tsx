import { useEffect, useState } from "react";
import { Button, Card, Modal } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "../context/translation_context";
import { deleteAttachment, type Attachment, fetchAttachments, downloadAttachment } from "../services/datacrow_api";

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
    
    function handleDownload(attachment: Attachment) {
        downloadAttachment(attachment.objectID, attachment.name).
            then((blob) => {
                const url = window.URL.createObjectURL(new Blob([blob]));
                const link = document.createElement("a");
                link.href = url;
                link.setAttribute("download", attachment.name);
                document.body.appendChild(link);
                link.click();
                link.parentNode?.removeChild(link);
        });
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
            
            <div style={{ display: "flex", flexWrap: "wrap" }}>
                {attachments && attachments.map((attachment) => (

                <Card style={{ width: '24rem' }} key={"card-pic-" + attachment.name}>
                    <Card.Body>
                        {attachment.displayName}
                    </Card.Body>
                    <Card.Header style={{ height: '2.5em' }}>
                        <div className="bd-theme" style={{ display: "flex", flexWrap: "wrap", float: "right", top: "0" }} >
                            <i className="bi bi-eraser" onClick={() => handleDelete(attachment)} style={{fontSize:"1.2rem"}}></i>
                        </div>
                        <div className="bd-theme" style={{ display: "flex", flexWrap: "wrap", float: "right", top: "0", marginRight: "10px" }} >
                            <i className="bi bi-download" onClick={() => handleDownload(attachment)} style={{fontSize:"1.2rem"}}></i>
                        </div>                        
                    </Card.Header>
                </Card>
                ))} 
            </div>    
        </div>
    );
}
