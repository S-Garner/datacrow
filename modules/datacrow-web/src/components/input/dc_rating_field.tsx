import { Form } from "react-bootstrap";
import type { InputFieldProps } from "./dc_input_field";
import { Controller, useFormContext } from "react-hook-form";
import { useState } from "react";


interface StarRatingProps {
    rating: number;
}

export default function DcRatingField({
    field,
    value,
}: InputFieldProps) {

    const [rating, setRating] = useState<number>(value as number);
    
    const StarRating: React.FC = () => {

        const stars = [1, 2, 3, 4, 5];

        return (
            <div>
                {stars.map((star) => (
                    <span
                        key={star}
                        onClick={() => setRating(star)}
                        style={{
                            cursor: 'pointer',
                            color: star <= rating ? 'gold' : 'gray',
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
        <StarRating />  
    )
}