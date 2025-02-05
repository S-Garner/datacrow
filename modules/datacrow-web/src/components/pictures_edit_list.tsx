import { useEffect, useState } from "react";
import { fetchPictures, type Picture } from "../services/datacrow_api";
import { useNavigate } from "react-router-dom";
import { Card } from "react-bootstrap";

type Props = {
  itemID: string;
};

export default function PictureEditList({itemID} : Props) {
    
    const [pictures, setPictures] = useState<Picture[]>();
    const navigate = useNavigate();
    
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
    
    return (
        <div style={{ display: "flex", flexWrap: "wrap" }}>
            {pictures && pictures.map((picture) => (
                <Card style={{ width: '18rem' }} key={"card-pic-" + picture.filename}>
                    <Card.Header style={{ height: '2.5em' }}>
                        {picture.order}
                    </Card.Header>
                    <Card.Img src={picture.thumbUrl}  />
                </Card>
            ))}        
        </div>
    );
}