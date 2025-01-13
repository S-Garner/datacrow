import Select, { components, type GroupBase, type OptionProps} from 'react-select'
import type { JSX } from 'react/jsx-runtime';
import type { InputFieldProps } from './dc_input_field';
import { Controller, useFormContext } from 'react-hook-form';

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

    const { register } = useFormContext();
    const options = Options();
    const currentValue = CurrentValue();

    function CurrentValue() {
        let selection = new Array<IconSelectOption>(3);
        let idx = 0;
        
        options.forEach((option: IconSelectOption) => {
            (value as Array<String>).forEach((v) => {
                if (option.value === v)
                    selection[idx++] = option;
            });
        });
        
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

    return (
        <Controller
            name={"controller-inputfield-" + field.index}
            key={"controller-inputfield-" + field.index}
            defaultValue={currentValue}
            rules={{ required: field.required }}
            render={renderProps => {
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
    );
}