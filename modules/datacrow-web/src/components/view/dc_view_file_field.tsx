import { Link, useNavigate } from "react-router-dom";
import { useMessage } from "../../context/message_context";
import { downloadFile, type Field, type Reference } from "../../services/datacrow_api";
import { useTranslation } from "../../context/translation_context";

export interface Props {
    field: Field,
    value: Object | undefined,
}

export default function ViewFileField({
    field,
    value
}: Props) {
    
    const navigate = useNavigate();
    const message = useMessage();
    const { t } = useTranslation();
    
    let name = value ? String(value).split(/(\\|\/)/g).pop()  : "";
    
    const handleDownload = () => {
        downloadFile(String(value)).
            then((blob) => {
                const url = window.URL.createObjectURL(new Blob([blob]));
                const link = document.createElement("a");
                link.href = url;
                link.setAttribute("download", String(name));
                document.body.appendChild(link);
                link.click();
                link.parentNode?.removeChild(link)
            }).
            catch(error => {
                console.log(error);
                if (error.status === 401) {
                    navigate("/login");
                } else {
                    message.showMessage(t("msgFileCouldNotBeFound"));
                }
            });
    } 
    
    return (
        <div key={"div-" + field.index}>
            {String(value)}<i className="bi bi-download" onClick={() => handleDownload()} style={{fontSize:"1.2rem", marginLeft: "10px"}}></i>
        </div>
    )
}