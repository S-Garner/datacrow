import { useEffect, useState } from "react";
import { deletePicture, fetchPictures, movePictureDown, movePictureUp, savePicture, type Picture } from "../services/datacrow_api";
import { useNavigate } from "react-router-dom";
import { Button, Card, Modal } from "react-bootstrap";
import { useTranslation } from "../context/translation_context";
import { useMessage } from "../context/message_context";

type Props = {
  itemID: string;
};

export default function PictureEditList({itemID} : Props) {
    
    const [pictures, setPictures] = useState<Picture[]>();
    const [picture, setPicture] = useState<Picture>();
    const [imageSrc, setImageSrc] = useState('');
    
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
    
    const handleUploadFromClipboard = async () => {
        try {

            const clipboardItems = await navigator.clipboard.read();

            for (const clipboardItem of clipboardItems) {

                const imageItem = clipboardItem.types.find(type => type.startsWith('image/'));

                if (imageItem) {
                    const blob = await clipboardItem.getType(imageItem);

                    savePicture(blob, itemID).
                        then((data) => setPictures(data)).
                        catch(error => {
                            console.log(error);
                            if (error.status === 401) {
                                navigate("/login");
                            } else {
                                message.showError(t(error.response.data));
                            }
                        });
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
    
    return (
        <div>

            <div className="bd-theme" style={{top: "0", marginBottom: "10px" }} >
                <i className="bi bi-clipboard-plus lg" style={{fontSize:"1.5rem"}} onClick={handleUploadFromClipboard}></i>
            </div>
        
            <Modal show={showDeleteConfirm} onHide={() => setShowDeleteConfirm(false)}>
                <Modal.Body></Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={() => setShowDeleteConfirm(false)}>
                        {t("lblClose")}
                    </Button>
                    <Button variant="primary" onClick={() => handleDeleteAfterConfirm()}>
                        {t("lblOK")}
                    </Button>
                </Modal.Footer>
            </Modal>
            
            {imageSrc && <img src={imageSrc} alt="Pasted" />}
        
            {pictures && pictures.map((picture) => (
                <Card style={{ width: '18rem' }} key={"card-pic-" + picture.filename}>
                    <Card.Img src={picture.thumbUrl + "?" + Date.now()}  />
                    <Card.Header style={{ height: '2.5em' }}>
                        {t("lblPicture")}&nbsp;#{picture.order}
                        
                        <div className="bd-theme" style={{ display: "flex", flexWrap: "wrap", float: "right", top: "0" }} >
                            
                            {(picture.order < pictures.length) &&
                                (<i className="bi bi-arrow-down" onClick={() => handlePictureDown(picture)} style={{fontSize:"1.2rem"}}></i>)
                            }

                            {(picture.order > 1) &&
                                (<i className="bi bi-arrow-up" onClick={() => handleMovePictureUp(picture)} style={{fontSize:"1.2rem"}}></i>)
                            }
                            
                            <i className="bi bi-eraser" onClick={() => handleDelete(picture)} style={{fontSize:"1.2rem"}}></i>
                        </div>            
                        
                    </Card.Header>
                </Card>
            ))}        
        </div>
    );
}