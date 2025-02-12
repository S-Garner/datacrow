import { Controller, useFormContext } from "react-hook-form";
import { useState } from "react";
import React, { type InputHTMLAttributes } from "react";
import type { Field } from '../../services/datacrow_api';

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
    currentValue: Object;
    field: Field;
}

const Input: React.FC<InputProps> = ({ currentValue, field, ...rest }) => {

    let v = currentValue === undefined ? 0 : currentValue;

    const [rating, setRating] = useState<number>(v as number);
    const { register, setValue } = useFormContext();
    
    const StarRating: React.FC = () => {
        const stars = [1, 2, 3, 4, 5, 6, 7, 8, 9, 10];
        return (
            <div>
                {stars.map((star) => (
                    <span
                        key={star}
                        onClick={() => changeRating(star)}
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

    function changeRating(star: number) {
        setRating(star);
        // update the hidden input field
        setValue("inputfield-" + field.index, star)
    }

    return (
        <Controller
            name={"inputfield-" + field.index}
            key={"inputfield-" + field.index}
            defaultValue={rating}
            disabled={field.readOnly}
            rules={{ required: field.required }}
            render={renderProps => {
                return (
                    <>
                        <StarRating />
                        <input
                            type="text"
                            key={"inputfield-" + field.index}
                            {...register("inputfield-" + field.index)}
                            {...renderProps.field}
                            {...rest}
                            hidden={true}
                        />
                    </>
                );
            }}
        />
    );
};

export default Input;
