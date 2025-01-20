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
        fetchPictures(itemID).
        then((data) => setPictures(data)).
        catch(error => {
            console.log(error);
            if (error.status === 401) {
                navigate("/login");
            }
        });
    });
    
    return (
        <div style={{ display: "flex", flexWrap: "wrap" }}>
            {pictures && pictures.map((picture) => (
                <Card style={{ width: '18rem' }} key={"card-pic-" + picture.filename}>
                    <Card.Header style={{ height: '112px' }}>
                        {picture.filename}
                    </Card.Header>
                    <Card.Img src={picture.thumbUrl}  />
                </Card>
            ))}        
        </div>
    );
}