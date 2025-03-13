import { Controller, useFormContext } from "react-hook-form";
import { useEffect, useState } from "react";
import React, { type InputHTMLAttributes } from "react";
import type { Field } from '../../services/datacrow_api';
import { InputGroup } from "react-bootstrap";
import type { InputFieldComponentProps } from "./dc_input_field";
import { useTranslation } from "../../context/translation_context";

const DcRuntimeField: React.FC<InputFieldComponentProps> = ({ field, value, ...rest }) => {

    const { t } = useTranslation();

    const [runtime, setRuntime] = useState<number>(value as number);
    
    const [hours, setHours] = useState<number>();
    const [minutes, setMinutes] = useState<number>();
    const [seconds, setSeconds] = useState<number>();
    
    const { register, setValue } = useFormContext();

    useEffect(() => {
            split();
    }, [value]);

    function split() {
        let seconds = value as number;
        
        var hrs = seconds / (60*60);
        var absHrs = Math.floor(hrs); 
        setHours(absHrs);

        var min = (hrs - absHrs) * 60;
        var absMin = Math.floor(min);
        
        setMinutes(absMin);
        setSeconds(Math.floor((min - absMin) * 60));
    }

    function runtimeValueChange(e: React.ChangeEvent<HTMLInputElement>) {
        
        const newValue = e.currentTarget.value;
        let source = e.target.name;
        
        let total = 0;
        
        if (source === "fld-hours") {
            setHours(+newValue);
            total = (seconds ? seconds : 0) + (minutes ? minutes * 60 : 0) + (+newValue ? +newValue * 60 * 60 : 0);
        } else if (source === "fld-minutes") {
            setMinutes(+newValue);
            total = (seconds ? seconds : 0) + (+newValue ? +newValue * 60 : 0) + (hours ? hours * 60 * 60 : 0);
        } else if (source === "fld-seconds") {
            setSeconds(+newValue);
            total = (+newValue ? +newValue : 0) + (minutes ? minutes * 60 : 0) + (hours ? hours * 60 * 60 : 0);
        }
        
        setRuntime(total);
        setValue("inputfield-" + field.index, total);
    }

    return (
        <Controller
            name={"inputfield-" + field.index}
            key={"inputfield-" + field.index}
            defaultValue={runtime}
            disabled={field.readOnly}
            rules={{ required: field.required }}
            render={renderProps => {
                return (
                    <>
                        <div>
                            <InputGroup className="mb-3">
                                <input type="text" name="fld-hours" className="form-control" defaultValue={hours} style={{ maxWidth: "10em" }}
                                    onChange={e => { runtimeValueChange(e) }} placeholder={t("lblHours")} />
                                <input type="text" name="fld-minutes" className="form-control" defaultValue={minutes} style={{ maxWidth: "10em" }}
                                    onChange={e => { runtimeValueChange(e) }} placeholder={t("lblMinutes")} />
                                <input type="text" name="fld-seconds" className="form-control" defaultValue={seconds} style={{ maxWidth: "10em" }}
                                    onChange={e => { runtimeValueChange(e) }} placeholder={t("lblSeconds")} />
                            </InputGroup>
                        </div>

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
