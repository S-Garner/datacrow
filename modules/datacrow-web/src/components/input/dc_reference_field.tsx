import Select, { components, type ControlProps, type GroupBase, type OptionProps } from 'react-select'
import type { JSX } from 'react/jsx-runtime';
import { useState } from 'react';
import type { InputFieldComponentProps } from './dc_input_field';
import { useFormContext, Controller } from 'react-hook-form';
import { ItemCreateModal } from '../../pages/item/item_create_modal';
import { fetchReference, type Reference } from '../../services/datacrow_api';

export interface IconSelectOption {
    value: string;
    label: string;
    iconUrl: string;
    id: string;
}

const { Option } = components;

const IconOption = (props: JSX.IntrinsicAttributes & OptionProps<IconSelectOption, boolean, GroupBase<IconSelectOption>>) => (
    <Option {...props}>
    
        {props.data.iconUrl && (
            <img
                src={props.data.iconUrl}
                key={props.data.iconUrl}
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
}: InputFieldComponentProps) {

    const [creatingItem,  setCreatingItem] = useState(false);
    const [options, setOptions] = useState<IconSelectOption[]>(getOptions());
    const [selectedValue, setSelectedValue] = useState<IconSelectOption>(getCurrentValue());
    const { register } = useFormContext();
    
    function getCurrentValue() {
        let idx = 0;
        let selectedIdx = -1;

        options.forEach((option) => {
            if (option.value === (value as Reference).id)
                selectedIdx = idx;
                
            idx++;
        });
    
        return options[selectedIdx];
    }

    const handleCreateOption = () => {
        setCreatingItem(true);
    }
    
    const onCreateItem = (itemID: string | undefined) => {
        setCreatingItem(false); // hide the modal

        if (itemID) {
            fetchReference(field.referencedModuleIdx, itemID).
                then((reference) => {
                    let option: IconSelectOption = {
                        value: reference.id, id: reference.id, label: reference.name, iconUrl: reference.iconUrl
                    }

                    setOptions((options) => [...options, option]);
                    setSelectedValue(option);
                }
            );
        }
    }  
    
    function getOptions() {

        let options: IconSelectOption[] = [];

        if (references && references.items) {
            references.items.map(reference =>
                options.push({ id: reference.id, value: reference.id, label: reference.name, iconUrl: reference.iconUrl }),
            );
        }

        return options;
    }

    const Control = ({ children, ...props }: ControlProps<IconSelectOption, boolean, GroupBase<IconSelectOption>>) => (
        <components.Control {...props}>
            {(  (selectedValue && selectedValue.iconUrl)) && (
                <img
                    src={selectedValue?.iconUrl}
                    style={{ width: "24px", paddingLeft: "8px" }} />
            )}
            {children}
        </components.Control>
    );

    return (
        <div className="float-container" style={{ width: "100%"  }}>
            <ItemCreateModal show={creatingItem} moduleIdx={field.referencedModuleIdx} onCreateItem={onCreateItem} />
            
            <div className="float-child input-dropdown">
                <Controller
                    name={"inputfield-" + field.index}
                    key={"inputfield-" + field.index}
                    defaultValue={selectedValue}
                    rules={{ required: field.required }}
                    render={renderProps => {
                        return (
                            <Select
                                className="react-select-container"
                                classNamePrefix="react-select"
                                options={options}
                                isClearable
                                isSearchable
                                placeholder=""
                                isDisabled={field.readOnly}
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
            </div>

            <div className="float-child input-button" style={{  }}>
                <i className="bi bi-plus-circle menu-icon" style={{ fontSize: "1.7rem" }} onClick={() => handleCreateOption()} ></i>
            </div>
        </div>
    )
}