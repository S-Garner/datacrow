import Select, { components, type GroupBase, type OptionProps} from 'react-select'
import type { JSX } from 'react/jsx-runtime';
import type { InputFieldProps } from './dc_input_field';

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
}: InputFieldProps) {

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

    return (
        <Select
            className="basic-single"
            classNamePrefix="select"
            key={"inputfield-" + field.index}
            options={options}
            defaultValue={currentValue}
            isClearable
            isMulti
            isSearchable
            placeholder="..."
            components={{ Option: IconOption }} />
    );
}