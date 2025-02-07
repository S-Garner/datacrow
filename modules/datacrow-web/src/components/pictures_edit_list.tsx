import { useEffect, useState } from "react";
import { deletePicture, fetchPictures, movePictureDown, movePictureUp, savePicture, type Picture } from "../services/datacrow_api";
import { useNavigate } from "react-router-dom";
import { Button, Card, Modal } from "react-bootstrap";
import { useTranslation } from "../context/translation_context";

type Props = {
  itemID: string;
};

export default function PictureEditList({itemID} : Props) {
    
    const [pictures, setPictures] = useState<Picture[]>();
    const [picture, setPicture] = useState<Picture>();
    const [imageSrc, setImageSrc] = useState('');
    
    const navigate = useNavigate();
    const { t } = useTranslation();
    
    const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
    const [showError, setShowError] = useState(false);
    const [error, setError] = useState<String | undefined>("");
    
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
    
    
    const handleClick = async () => {
        try {
            // Read the clipboard data
            const clipboardItems = await navigator.clipboard.read();
            for (const clipboardItem of clipboardItems) {
                const imageItem = clipboardItem.types.find(type => type.startsWith('image/'));
                if (imageItem) {
                    const blob = await clipboardItem.getType(imageItem);
                    const formData = new FormData();
                    
                    const file = new File([blob], 'hello.txt', { type: blob.type });
                    
                    
                    formData.append('image', file, 'clipboard-image.png'); // You can change the filename as needed

                    // Send the image to your server
                    savePicture(blob).catch(error => {
                        console.log(error);
                        if (error.status === 401) {
                            navigate("/login");
                        } else {
                            t(error.response.data) && setError(t(error.response.data));
                            setShowError(true);
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
                <i className="bi bi-clipboard-plus lg" style={{fontSize:"1.5rem"}} onClick={handleClick}></i>
            </div>
        
            <Modal show={showDeleteConfirm} onHide={() => setShowDeleteConfirm(false)}>
                <Modal.Body>{t("msgDeletePictureConfirmation")}</Modal.Body>
                <Modal.Footer>
                    <Button variant="secondary" onClick={() => setShowDeleteConfirm(false)}>
                        {t("lblClose")}
                    </Button>
                    <Button variant="primary" onClick={() => handleDeleteAfterConfirm()}>
                        {t("lblOK")}
                    </Button>
                </Modal.Footer>
            </Modal>
            
            <Modal show={showError} onHide={() => setShowDeleteConfirm(false)}>
                <Modal.Body>{error}</Modal.Body>
                <Modal.Footer>
                    <Button variant="primary" onClick={() => setShowError(false)}>
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