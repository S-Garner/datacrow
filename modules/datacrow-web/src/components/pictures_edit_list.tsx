import { useEffect, useState } from "react";
import { deletePicture, fetchPictures, type Picture } from "../services/datacrow_api";
import { useNavigate } from "react-router-dom";
import { Button, Card, Modal } from "react-bootstrap";
import { useTranslation } from "../context/translation_context";

type Props = {
  itemID: string;
};

export default function PictureEditList({itemID} : Props) {
    
    const [pictures, setPictures] = useState<Picture[]>();
    
    const [picture, setPicture] = useState<Picture>();
    
    const navigate = useNavigate();
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
    
    function movePictureDown(picture: Picture) {
        console.log("moving it down");
    }
    
    function movePictureUp(picture: Picture) {
        console.log("moving it up");
    }
    
    function handleDelete(picture: Picture) {
        setPicture(picture);
        setShowDeleteConfirm(true);
    }
    
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
        <div >
        
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
        
            {pictures && pictures.map((picture) => (
                <Card style={{ width: '18rem' }} key={"card-pic-" + picture.filename}>
                    <Card.Img src={picture.thumbUrl}  />
                    <Card.Header style={{ height: '2.5em' }}>
                        {t("lblPicture")}&nbsp;#{picture.order}
                        
                        <div className="bd-theme" style={{ display: "flex", flexWrap: "wrap", float: "right", top: "0" }} >
                            
                            {(picture.order < pictures.length) &&
                                (<i className="bi bi-arrow-down" onClick={() => movePictureDown(picture)}></i>)
                            }

                            {(picture.order > 1) &&
                                (<i className="bi bi-arrow-up" onClick={() => movePictureUp(picture)}></i>)
                            }
                            
                            <i className="bi bi-eraser" onClick={() => handleDelete(picture)}></i>
                        </div>            
                        
                    </Card.Header>
                </Card>
            ))}        
        </div>
    );
}