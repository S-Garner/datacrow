import Select, { components, type ActionMeta, type ControlProps, type GroupBase, type MultiValue, type OptionProps, type SingleValue } from 'react-select'
import { type Field, type References } from "../.././services/datacrow_api";
import type { JSX } from 'react/jsx-runtime';
import { useState } from 'react';

export interface IconSelectOption {
    value: string;
    label: string;
    iconUrl: string;
}

const { Option } = components;

const IconOption = (props: JSX.IntrinsicAttributes & OptionProps<IconSelectOption, boolean, GroupBase<IconSelectOption>>) => (
    <Option {...props}>
        <img
            src={props.data.iconUrl}
            style={{ width: "24px", paddingRight: "8px" }}
        />

        {props.data.label}
    </Option>
);

export default function DcReferenceField({
    value,
    references
}: {
    value: Object,
    references?: References
}) {

    const [selectedValue, setSelectedValue] = useState<IconSelectOption>();

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
            <img 
                src={selectedValue ? selectedValue.iconUrl : currentValue?.iconUrl}
                style={{ width: "24px", paddingLeft: "8px" }} />
                
            {children}
        </components.Control>
    );

    function selectionChanged(newValue: SingleValue<IconSelectOption> | MultiValue<IconSelectOption>, actionMeta: ActionMeta<IconSelectOption>): void {
        if (!Array.isArray(newValue))
            setSelectedValue(newValue as IconSelectOption);
    }

    return (
        <Select
            className="basic-single"
            classNamePrefix="select"
            options={options}
            defaultValue={currentValue}
            onChange={selectionChanged}
            isClearable
            isSearchable
            placeholder="..."
            components={{ Option: IconOption, Control }} />
    );
}