import { components, type GroupBase, type OptionProps} from 'react-select'
import CreatableSelect from 'react-select/creatable';
import type { JSX } from 'react/jsx-runtime';
import type { InputFieldComponentProps } from './dc_input_field';
import { Controller, useFormContext } from 'react-hook-form';
import { ItemCreateModal } from '../../pages/item/item_create_modal';
import { useState } from 'react';

export interface IconSelectOption {
    value: string;
    label: string;
    iconUrl: string;
}

const { Option } = components;

const IconOption = (props: JSX.IntrinsicAttributes & OptionProps<IconSelectOption, true, GroupBase<IconSelectOption>>) => (
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

export default function DcMultiReferenceField({
    field,
    value,
    references
}: InputFieldComponentProps) {

    const [creatingItem,  setCreatingItem] = useState(false);
    const { register } = useFormContext();
    const options = Options();
    const currentValue = CurrentValue();
    
    function CurrentValue() {
        let selection = new Array<IconSelectOption>(3);
        let idx = 0;
        
        if (value != undefined) {
            options.forEach((option: IconSelectOption) => {
                (value as Array<String>).forEach((v) => {
                    if (option.value === v)
                        selection[idx++] = option;
                });
            });
        }
        
        return selection;
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
    
    const handleCreateOption = (value: string) => {
        setCreatingItem(true);
    }
    
    return (
        <>
            <ItemCreateModal show={creatingItem} moduleIdx={field.referencedModuleIdx} />
            
            <Controller
                name={"inputfield-" + field.index}
                key={"inputfield-" + field.index}
                defaultValue={currentValue}
                rules={{ required: field.required }}
                render={renderProps => {
                    return (
                        <CreatableSelect
                            className="react-select-container"
                            classNamePrefix="react-select"
                            options={options}
                            defaultValue={currentValue}
                            isClearable
                            isMulti
                            isSearchable
                            isDisabled={field.readOnly}
                            onCreateOption={(value) => handleCreateOption(value)}
                            placeholder="..."
                            components={{ Option: IconOption }}
                            {...register("inputfield-" + field.index)}
                            {...renderProps.field}
                            onChange={e => {
                                renderProps.field.onChange(e);
                            }}
                        />
                    );
                }}
            />
        </>          
    );
}