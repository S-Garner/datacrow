import Select, { components } from 'react-select'
import { type Field, type References } from "../.././services/datacrow_api";

export function DcDropDown(field: Field, value: Object, references?: References) {

    function getOptions() {

        let options: { value: string; label: string; }[] = [];

        if (references && references.items) {
            references.items.map(reference =>
                options.push({ value: reference.id, label: reference.name }),
            );
        }
        
        return options;
    }

    return (
        <Select 
            options={getOptions()}
            isClearable
            isSearchable />
    );
}