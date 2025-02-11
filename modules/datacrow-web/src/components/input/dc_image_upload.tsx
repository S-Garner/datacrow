import React, { useRef, useState, type ChangeEvent } from "react";


export function ImageUploadField(
    { 
        accept = "image/*", 
        handleImageFileSelect = (file: File) => { } 
    }) 
      
{
    const fileInputRef = useRef<HTMLInputElement>(null);

    const handleImageUploadClick = () => {
        fileInputRef.current?.click();
    };

    const updateImage = (event: ChangeEvent<HTMLInputElement>) => {
        if (event.target.files && event.target.files[0]) {
            const file = event.target.files[0];

            handleImageFileSelect(file);
        }
    };

    const handleDragOver = (event: React.DragEvent<HTMLDivElement>) => {
        event.preventDefault();
    };

    const handleFileDrop = (event: React.DragEvent<HTMLDivElement>) => {
        event.preventDefault();
        if (event.dataTransfer.files && event.dataTransfer.files[0]) {
            const file = event.dataTransfer.files[0];
            
            handleImageFileSelect(file);
        }
    };

    return (
        <div className={"container"}>
            <div
                onDragOver={handleDragOver}
                onDrop={handleFileDrop}
                className="dropZone"
                onClick={handleImageUploadClick}>

                <span>Choose file or drag it here</span>

                <input
                    ref={fileInputRef}
                    type="file"
                    accept={accept}
                    style={{ display: "none" }}
                    onChange={updateImage}
                />
            </div>
        </div>
    );
}
