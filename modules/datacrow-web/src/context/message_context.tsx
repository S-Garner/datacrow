import { createContext, useContext, useState } from "react";
import { Button, Modal } from "react-bootstrap";
import { useTranslation } from "./translation_context";

export interface MessageType {
	showError: (message: string | undefined) => void;
	close: () => void;
}

export const MessageContext = createContext<MessageType>(null!);

export function useMessage() {
	return useContext(MessageContext);
}

export function MessageProvider({ children }: { children: React.ReactNode }) {
    
    let [show, setShow] = useState<boolean>(false);
    let [message, setMessage] = useState<String | undefined>("");
    
    const { t } = useTranslation();
    
	let close = () => {
        setShow(false);    
    }
    
	let showError = (message: string | undefined) => {
		{	
            setMessage(message);
            setShow(true);
		}
	};
	
	let value = { showError, close};
	
	return (
        <MessageContext.Provider value={value}>
        
            <Modal centered style={{}} show={show} onHide={() => setShow(false)}>
                <Modal.Body>{message}</Modal.Body>
                <Modal.Footer>
                    <Button variant="primary" onClick={() => setShow(false)}>
                        {t("lblOK")}
                    </Button>
                </Modal.Footer>
            </Modal>

            {children}
        </MessageContext.Provider>);
}