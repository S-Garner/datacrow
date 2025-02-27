import { Alert } from "react-bootstrap";

export interface MessageBoxProperties {
    header: string, 
    message: string,
    type: MessageBoxType
}

export enum MessageBoxType {
    information = "info",
    error = "danger",
    warning = "warning"
}

export function MessageBox({
    header,
    message,
    type
}: MessageBoxProperties) {
    return (
        <Alert variant={type} show={true}>
            <Alert.Heading>{header}</Alert.Heading>
            <p>{message}</p>
        </Alert>
    );
}