import { Controller, useFormContext } from "react-hook-form";
import { useState } from "react";
import React, { type InputHTMLAttributes } from "react";
import type { Field } from '../../services/datacrow_api';
import { InputGroup } from "react-bootstrap";

interface InputProps extends InputHTMLAttributes<HTMLInputElement> {
    currentValue: Object;
    field: Field;
}

const Input: React.FC<InputProps> = ({ currentValue, field, ...rest }) => {

    const [rating, setRating] = useState<number>(currentValue as number);
    const { register, setValue, getValues } = useFormContext();
    
    let hrs = 1;
    let min = 2;
    let sec = 2;
    
    
    const RuntimeInput: React.FC = () => {
        return (
            <div>
                <InputGroup className="mb-3">
                    <input type="text" name="fld-hours" className="form-control" defaultValue={hrs} 
                        onChange={e => {runtimeValueChange(e)}} />
                    <input type="text" name="fld-minutes" className="form-control" defaultValue={min} />
                    <input type="text" name="fld-seconds" className="form-control" defaultValue={sec} />
                </InputGroup>
            </div>
        );
    };

    function runtimeValueChange(e: React.ChangeEvent<HTMLInputElement>) {
        
        // update the hidden input field
        setValue("inputfield-" + field.index, "CHANGED");
    }

    return (
        <Controller
            name={"inputfield-" + field.index}
            key={"inputfield-" + field.index}
            defaultValue={rating}
            rules={{ required: field.required }}
            render={renderProps => {
                return (
                    <>
                        <RuntimeInput />
                        <input
                            type="text"
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
