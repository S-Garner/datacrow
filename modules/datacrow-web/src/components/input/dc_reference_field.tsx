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
                //alt={props.data.label}
            />
            
            &nbsp;
            
            {props.data.label}
        </Option>
    );

    function getOptions() {

        let options: { value: string; label: string; iconUrl: string}[] = [];

        if (references && references.items) {
            references.items.map(reference =>
                options.push({ value: reference.id, label: reference.name, iconUrl: reference.iconUrl }),
            );
        }
        
        return options;
    }

    return (
        <Select 
            options={getOptions()}
            isClearable
            isSearchable 
            components={{ Option: IconOption }}/>
    );
}