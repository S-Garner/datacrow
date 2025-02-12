import { useEffect, useState } from "react";
import { Button, Card, Modal, Spinner } from "react-bootstrap";
import { useNavigate } from "react-router-dom";
import { useTranslation } from "../context/translation_context";
import { deleteAttachment, type Attachment, fetchAttachments, downloadAttachment, saveAttachment } from "../services/datacrow_api";
import { useMessage } from "../context/message_context";
import FileUploadField from "./input/dc_file_upload";
import BusyModal from "./busy_modal";
import { useAuth } from "../context/authentication_context";

type Props = {
  itemID: string;
};

export default function AttachmentEditList({itemID} : Props) {

    const [attachments, setAttachments] = useState<Attachment[]>();
    const [attachment, setAttachment] = useState<Attachment>();
    
    const [uploading, setUploading] = useState(false);
    const [showUpload, setShowUpload] = useState(false);
    
    const message = useMessage();
    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
    const { t } = useTranslation();
    const navigate = useNavigate();
    
    const auth = useAuth();
    
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
    
    function handleUpload(file: File) {
        
        // TODO: check file size!
        
        if (file.size > (auth.user.settings.maxUploadAttachmentSize)) {
            
            let currentSize = String(file.size / 1000);
            currentSize = currentSize.indexOf(".") > 0 ? currentSize.substring(0, currentSize.indexOf(".")) : currentSize;
            
            message.showMessage(t("msgFileIsTooLarge", 
                [currentSize, String(auth.user.settings.maxUploadAttachmentSize / 1000) + " KB"]));
            
        } else {
        
            setUploading(true);
            
            saveAttachment(file, itemID, file.name).
                then((data) => setAttachments(data)).
                then(() => setUploading(false)).
                catch(error => {
                    setUploading(true);
                    
                    console.log(error);
                    if (error.status === 401) {
                        navigate("/login");
                    } else {
                        message.showMessage(t(error.response.data));
                    }
                });
        }
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
                link.parentNode?.removeChild(link)
            }).
            catch(error => {
                console.log(error);
                if (error.status === 401) {
                    navigate("/login");
                } else {
                    message.showMessage(error.response.data);
                }
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
                    } else {
                        message.showMessage(error.response.data);
                    }
                });
        } 
    }

    return (
        <div>
            <div className="bd-theme" style={{top: "0", marginBottom: "10px" }} >
                <i className="bi bi-folder" style={{fontSize:"1.7rem"}} onClick={() => setShowUpload(!showUpload)}></i>
            </div>        

            <BusyModal show={uploading} message={t("msgBusyUploadingAttachment")} />
        
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
            
            {showUpload && <FileUploadField accept="*" handleFileSelect={handleUpload} />}
            
            <div style={{ display: "flex", flexWrap: "wrap" }}>
                {attachments && attachments.map((attachment) => (

                <Card style={{ width: '24rem' }} key={"card-pic-" + attachment.name}>
                    <Card.Body>
                        {attachment.displayName}
                    </Card.Body>
                    <Card.Header style={{ height: '2.5em' }}>
                        <div className="bd-theme" style={{ display: "flex", flexWrap: "wrap", float: "right", top: "0" }} >
                            <i className="bi bi-trash" onClick={() => handleDelete(attachment)} style={{fontSize:"1.2rem"}}></i>
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
