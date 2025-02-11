import { Modal, Spinner } from "react-bootstrap";

type Props = {
    message: string | undefined;
    show: boolean;
};

export default function BusyModal({ message, show }: Props) {

    return (
        <Modal centered show={show}>
            <Modal.Header style={{height: "3em", textAlign: "center"}}>
                {message}<br />
            </Modal.Header>

            <Modal.Body>
                <div style={{ textAlign: "center" }}>
                    <Spinner animation="border" role="status" variant="primary">
                        <span className="visually-hidden">...</span>
                    </Spinner>
                </div>
            </Modal.Body>
        </Modal>
    )
}



