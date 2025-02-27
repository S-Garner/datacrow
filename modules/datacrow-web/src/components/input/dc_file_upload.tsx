import React, { useRef, type ChangeEvent } from "react";
import { useTranslation } from "../../context/translation_context";

export interface FieldProps {
    accept: string,
    handleFileSelect: (file: File) => void
}

export default function FileUploadField({
    accept,
    handleFileSelect
}: FieldProps) {

    const fileInputRef = useRef<HTMLInputElement>(null);
    const { t } = useTranslation();

    const handleImageUploadClick = () => {
        fileInputRef.current?.click();
    };

    const updateImage = (event: ChangeEvent<HTMLInputElement>) => {
        if (event.target.files && event.target.files[0]) {
            const file = event.target.files[0];

            handleFileSelect(file);
        }
    };

    const handleDragOver = (event: React.DragEvent<HTMLDivElement>) => {
        event.preventDefault();
    };

    const handleFileDrop = (event: React.DragEvent<HTMLDivElement>) => {
        event.preventDefault();
        if (event.dataTransfer.files && event.dataTransfer.files[0]) {
            const file = event.dataTransfer.files[0];
            
            handleFileSelect(file);
        }
    };

    return (
            <div
                onDragOver={handleDragOver}
                onDrop={handleFileDrop}
                className="dropZone"
                onClick={handleImageUploadClick}>

                <span>{t("msgDragOrSelectImageFile")}</span>

                <input
                    ref={fileInputRef}
                    type="file"
                    accept={accept}
                    style={{ display: "none" }}
                    onChange={updateImage}
                />
            </div>
    );
}
