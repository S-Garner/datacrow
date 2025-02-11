import { useEffect, useState } from "react";
import { deletePicture, fetchPictures, movePictureDown, movePictureUp, savePicture, type Picture } from "../services/datacrow_api";
import { useNavigate } from "react-router-dom";
import { Button, Card, Modal } from "react-bootstrap";
import { useTranslation } from "../context/translation_context";
import { useMessage } from "../context/message_context";
import FileUploadField from "./input/dc_file_upload";
import BusyModal from "./busy_modal";

type Props = {
  itemID: string;
};

export default function PictureEditList({itemID} : Props) {
    
    const [pictures, setPictures] = useState<Picture[]>();
    const [picture, setPicture] = useState<Picture>();
    const [uploading, setUploading] = useState(false);
    const [showUpload, setShowUpload] = useState(false);
    
    const navigate = useNavigate();
    const message = useMessage();
    const { t } = useTranslation();
    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
    
    useEffect(() => {
        itemID && fetchPictures(itemID).
            then((data) => setPictures(data)).
            catch(error => {
                console.log(error);
                if (error.status === 401) {
                    navigate("/login");
                }
            });
    }, [itemID]);
    
    function handlePictureDown(picture: Picture) {
        movePictureDown(picture.objectID, picture.order).
                then((data) => setPictures(data)).
                catch(error => {
                    console.log(error);
                    if (error.status === 401) {
                        navigate("/login");
                    }
                });
    }
    
    function handleMovePictureUp(picture: Picture) {
        movePictureUp(picture.objectID, picture.order).
                then((data) => setPictures(data)).
                catch(error => {
                    console.log(error);
                    if (error.status === 401) {
                        navigate("/login");
                    }
                });
    }
    
    function handleDelete(picture: Picture) {
        setPicture(picture);
        setShowDeleteConfirm(true);
    }
    
    function uploadImage(blob: any) {
        
        setUploading(true);
        
        savePicture(blob, itemID).
            then((data) => setPictures(data)).
            then(() => setUploading(false)).
            catch(error => {
                setUploading(true);
                
                console.log(error);
                if (error.status === 401) {
                    navigate("/login");
                } else {
                    message.showError(t(error.response.data));
                }
            });
    }
    
    const handleUploadFromClipboard = async () => {
        try {
            const clipboardItems = await navigator.clipboard.read();
            
            for (const clipboardItem of clipboardItems) {
                const imageItem = clipboardItem.types.find(type => type.startsWith('image/'));
                
                if (imageItem) {
                    
                    const blob = await clipboardItem.getType(imageItem);
                    uploadImage(blob);
                }
            }
        } catch (error) {
            console.error('Error reading clipboard or uploading image:', error);
        }
    };

    function handleDeleteAfterConfirm() {
        
        setShowDeleteConfirm(false);
        
        if (picture) {
            deletePicture(picture.objectID, picture.order).
                then((data) => setPictures(data)).
                catch(error => {
                    console.log(error);
                    if (error.status === 401) {
                        navigate("/login");
                    }
                });
        } 
    }
    
    function handleImageFileSelect(file : File) {
        uploadImage(file);
    }
    
    return (
        <div>
            <div className="bd-theme" style={{top: "0", marginBottom: "10px" }} >
                <i className="bi bi-clipboard-plus" style={{fontSize:"1.7rem", marginRight:"15px"}} onClick={handleUploadFromClipboard}></i>
                <i className="bi bi-image" style={{fontSize:"1.7rem"}} onClick={() => setShowUpload(!showUpload)}></i>
            </div>
            
            <BusyModal show={uploading} message={t("msgBusyUploadingImage")} />
            
            <Modal centered show={showDeleteConfirm} onHide={() => setShowDeleteConfirm(false)}>
                <Modal.Body>{t('msgDeletePictureConfirmation')}</Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={() => setShowDeleteConfirm(false)}>
                        {t("lblClose")}
                    </Button>
                    <Button variant="primary" onClick={() => handleDeleteAfterConfirm()}>
                        {t("lblOK")}
                    </Button>
                </Modal.Footer>
            </Modal>
            
            {
                showUpload && <FileUploadField handleFileSelect={handleImageFileSelect} accept="image/*" />    
            }
            
            {pictures && pictures.map((picture) => (
                <Card style={{ width: '18rem' }} key={"card-pic-" + picture.filename}>
                    <Card.Img src={picture.thumbUrl + "?" + Date.now()}  />
                    <Card.Header style={{ height: '2.5em' }}>
                        {t("lblPicture")}&nbsp;#{picture.order}
                        
                        <div className="bd-theme" style={{ display: "flex", flexWrap: "wrap", float: "right", top: "0" }} >
                            
                            {(picture.order < pictures.length) &&
                                (<i className="bi bi-arrow-down" onClick={() => handlePictureDown(picture)} style={{fontSize:"1.2rem", marginLeft: "10px"}}></i>)
                            }

                            {(picture.order > 1) &&
                                (<i className="bi bi-arrow-up" onClick={() => handleMovePictureUp(picture)} style={{fontSize:"1.2rem", marginLeft: "10px"}}></i>)
                            }
                            
                            <i className="bi bi-trash" onClick={() => handleDelete(picture)} style={{fontSize:"1.2rem", marginLeft: "10px"}}></i>
                        </div>            
                        
                    </Card.Header>
                </Card>
            ))}        
        </div>
    );
}