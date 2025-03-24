import Select,{ components, type GroupBase, type OptionProps} from 'react-select'
import type { JSX } from 'react/jsx-runtime';
import type { InputFieldComponentProps } from './dc_input_field';
import { Controller, useFormContext } from 'react-hook-form';
import { ItemCreateModal } from '../../pages/item/item_create_modal';
import { useState } from 'react';
import { fetchReference, type Reference } from '../../services/datacrow_api';

export interface IconSelectOption {
    value: string;
    label: string;
    iconUrl: string;
    id: string;
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
    
    const [creatingItem, setCreatingItem] = useState(false);
    const { register, setValue } = useFormContext();
    const [options, setOptions] = useState<IconSelectOption[]>(getOptions());
    const [currentValue, setCurrentValue] = useState<IconSelectOption[]>(getCurrentValues());
    
    function getCurrentValues() {
        let selection = new Array<IconSelectOption>();
        let idx = 0;
        
        if (value != undefined) {
            options.forEach((option: IconSelectOption) => {
                (value as Array<string>).forEach((v) => {
                    if (option.value === v)
                        selection[idx++] = option;
                });
            });
        }
        
        return selection;
    }
    
    function getOptions() {

        let options: IconSelectOption[] = [];

        if (references && references.items) {
            references.items.map(reference =>
                options.push({ value: reference.id, id: reference.id, label: reference.name, iconUrl: reference.iconUrl }),
            );
        }

        return options;
    }
    
    const handleCreateOption = () => {
        setCreatingItem(true);
    }
    
    const onCreateItem = (itemID: string | undefined) => {
        
        setCreatingItem(false);

        if (itemID) {
            fetchReference(field.referencedModuleIdx, itemID).then((reference) => {
                let option: IconSelectOption = {
                    value: reference.id, id: reference.id, label: reference.name, iconUrl: reference.iconUrl
                }

                let values = currentValue.slice(0);
                values.push(option);

                setOptions((options) => [...options, option]);
                setCurrentValue(values);
                setValue("inputfield-" + field.index, values);
            });
        }
    }
    
    return (
        <div className="float-container" style={{ width: "100%"  }}>
            <ItemCreateModal show={creatingItem} moduleIdx={field.referencedModuleIdx} onCreateItem={onCreateItem} />
            <div className="float-child input-dropdown">
                <Controller
                    name={"inputfield-" + field.index}
                    key={"inputfield-" + field.index}
                    defaultValue={currentValue}
                    rules={{ required: field.required }}
                    render={renderProps => {
                        return (
                            <Select
                                className="react-select-container react-select"
                                classNamePrefix="react-select "
                                options={options}
                                isClearable
                                isMulti
                                isSearchable
                                isDisabled={field.readOnly}
                                placeholder=""
                                components={{ Option: IconOption }}
                                {...register("inputfield-" + field.index)}
                                {...renderProps.field}
                                onChange={e => {
                                    setCurrentValue(e as IconSelectOption[]);
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
    );
}