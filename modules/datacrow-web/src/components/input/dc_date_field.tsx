import { Form } from "react-bootstrap";
import type { InputFieldComponentProps } from "./dc_input_field";
import { Controller, useFormContext } from "react-hook-form";
import type { ChangeEvent } from "react";

export function DcDateField({
    field,
    value,
    ...rest
}: InputFieldComponentProps) {
	
	const { register, setValue } = useFormContext();
	
    const changeDate = (e: ChangeEvent<HTMLInputElement>) => {
        console.log(e.currentTarget.value);
    
        if (e.currentTarget.value) {
            setValue("inputfield-" + field.index, e.currentTarget.value);
        }
    };    
	
	
	
	return (
        <Controller
            name={"inputfield-" + field.index}
            key={"inputfield-" + field.index}
            defaultValue={(value as string)}
            disabled={field.readOnly}
            rules={{ required: field.required }}
            render={renderProps => {
                return (
                    <>
                        <input type="date" onChange={changeDate} {...rest} defaultValue={value as string} className="form-control" />

                        <input
                            hidden={true}
                            type="text"
                            key={"inputfield-" + field.index}
                            {...register("inputfield-" + field.index)}
                            {...renderProps.field}
                        />
                    </>
                );
            }}
        />
    );
        
/*		<Form.Control
			id={"inputfield-" + field.index}
			key={"inputfield-" + field.index}
			type="date"
			defaultValue={(value as string)}
			placeholder={field.label}
			aria-label={field.label}
			hidden={field.hidden}
			readOnly={field.readOnly}
			required={field.required}
			{...register("inputfield-" + field.index)} />
	); */
}