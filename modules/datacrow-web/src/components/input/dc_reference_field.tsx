import Select, { components, type GroupBase, type OptionProps } from 'react-select'
import { type Field, type References } from "../.././services/datacrow_api";
import type { JSX } from 'react/jsx-runtime';

export function DcReferenceField(field: Field, value: Object, references?: References) {

    const { Option } = components;
    
    const IconOption = (props: JSX.IntrinsicAttributes & OptionProps<{ value: string; label: string; iconUrl: string},
                        boolean, GroupBase<{ value: string; label: string; iconUrl: string}>>) => (
        <Option {...props}>
            <img
                src={props.data.iconUrl}
                style={{ width: 24}}
            />
            
            &nbsp;
            
            {props.data.label}
        </Option>
    );
    
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

        let options: { value: string; label: string; iconUrl: string}[] = [];

        if (references && references.items) {
            references.items.map(reference =>
                options.push({ value: reference.id, label: reference.name, iconUrl: reference.iconUrl }),
            );
        }
        
        return options;
    }

    const options = Options();
    const currentValue = CurrentValue();

    return (
        <Select 
            className="basic-single"
            classNamePrefix="select"
            options={ options }
            defaultValue={ currentValue }
            isClearable
            isSearchable
            placeholder="..."
            components={{ Option: IconOption }}/>
    );
}