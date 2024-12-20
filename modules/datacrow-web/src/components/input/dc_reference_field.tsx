import Select, { components, type ActionMeta, type ControlProps, type GroupBase, type MultiValue, type OptionProps, type SingleValue } from 'react-select'
import type { JSX } from 'react/jsx-runtime';
import { useState } from 'react';
import type { InputFieldProps } from './dc_input_field';
import { useFormContext, Controller } from 'react-hook-form';

export interface IconSelectOption {
    value: string;
    label: string;
    iconUrl: string;
}

const { Option } = components;

const IconOption = (props: JSX.IntrinsicAttributes & OptionProps<IconSelectOption, boolean, GroupBase<IconSelectOption>>) => (
    <Option {...props}>
    
        {props.data.iconUrl && (
            <img
                src={props.data.iconUrl}
                style={{ width: "24px", paddingRight: "8px" }}
            />
        )}

        {props.data.label}
    </Option>
);

export default function DcReferenceField({
    field,
    value,
    references
}: InputFieldProps) {

    const [selectedValue, setSelectedValue] = useState<IconSelectOption>();
    
    const { register } = useFormContext();
    const options = Options();
    const currentValue = CurrentValue();

    function CurrentValue() {
        let idx = 0;
        let selectedIdx = -1;

        options.forEach((option) => {
            if (option.value === value)
                selectedIdx = idx;
            idx++;
        });
        
        return options[selectedIdx];
    }

    function Options() {

        let options: IconSelectOption[] = [];

        if (references && references.items) {
            references.items.map(reference =>
                options.push({ value: reference.id, label: reference.name, iconUrl: reference.iconUrl }),
            );
        }

        return options;
    }

    const Control = ({ children, ...props }: ControlProps<IconSelectOption, boolean, GroupBase<IconSelectOption>>) => (
        <components.Control {...props}>
            {(  (selectedValue && selectedValue.iconUrl) || 
                (!selectedValue && currentValue && currentValue.iconUrl)) && (
                <img
                    src={selectedValue ? selectedValue.iconUrl : currentValue?.iconUrl}
                    style={{ width: "24px", paddingLeft: "8px" }} />
            )}
            {children}
        </components.Control>
    );

    return (
        <Controller
            name={"inputfield-" + field.index}
            rules={{ required: true }}
            render={renderProps => {
                return (
                    <Select
                        className="basic-single"
                        classNamePrefix="select"
                        key={"inputfield-" + field.index}
                        options={options}
                        defaultValue={currentValue}
                        isClearable
                        isSearchable
                        placeholder="..."
                        components={{ Option: IconOption, Control }}
                        {...register("inputfield-" + field.index)}
                        {...renderProps.field}
                        onChange={e => {
                            setSelectedValue(e as IconSelectOption);
                            renderProps.field.onChange(e);
                        }}
                    />
                );
            }}
        />
    )
}