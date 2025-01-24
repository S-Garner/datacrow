
type Props = {
    itemID: string;
};

export default function ChildrenOverview({itemID} : Props) {

    return (
        <div>
            {itemID}
        </div>
    );
};