import { Controller, useFormContext } from "react-hook-form";
import { useState } from "react";
import React, { type InputHTMLAttributes } from "react";
import type { Field } from '../../services/datacrow_api';
import { InputGroup } from "react-bootstrap";
import type { InputFieldComponentProps } from "./dc_input_field";

const DcRuntimeField: React.FC<InputFieldComponentProps> = ({ field, value, ...rest }) => {

    const [rating, setRating] = useState<number>(value as number);
    const { register, setValue } = useFormContext();
    
    // TODO - base these on the total run time
    let hrs = 1;
    let min = 2;
    let sec = 2;
    
    const RuntimeInput: React.FC = () => {
        return (
            <div>
                <InputGroup className="mb-3">
                    <input type="text" name="fld-hours" className="form-control" defaultValue={hrs} 
                        onChange={e => {runtimeValueChange(e)}} />
                    <input type="text" name="fld-minutes" className="form-control" defaultValue={min} 
                        onChange={e => {runtimeValueChange(e)}} />
                    <input type="text" name="fld-seconds" className="form-control" defaultValue={sec} 
                        onChange={e => {runtimeValueChange(e)}} />
                </InputGroup>
            </div>
        );
    };

    function runtimeValueChange(e: React.ChangeEvent<HTMLInputElement>) {
        // TODO - set the value of the hidden field (as controlled below)
        setRating(122);
        setValue("inputfield-" + field.index, 122);
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

export default DcRuntimeField;
