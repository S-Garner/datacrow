export interface Props {
    value: string
}

export function Icon({
    value
}: Props) {
    return (
        <img src={value.startsWith("data") ? value : `data:image/png;base64,${value}`} style={{ width: "auto", height: "128px" }} />
    );
}