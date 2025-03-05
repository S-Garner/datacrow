export default function StarRating(props: { rating: number }) {
    const StarRating: React.FC = () => {
        const stars = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];
        return (
            <div>
                {stars.map((star) => (
                    <span
                        key={star}
                        style={{
                            cursor: 'pointer',
                            color: star <= props.rating ? 'gold' : 'gray',
                            fontSize: '24px',
                        }}
                    >
                        ★
                    </span>
                ))}
            </div>
        );
    };

    return (
        <>
            <StarRating />
        </>
    );
}


