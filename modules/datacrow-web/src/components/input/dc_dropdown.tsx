import Select, { components } from 'react-select'
import { type Field, type References } from "../.././services/datacrow_api";

export function DcDropDown(field: Field, value: Object, references?: References) {

    function getOptions() {

        if (references && references.items) {
            let options = [{ value: '-1', label: '..' }];
            references.items.map(reference =>
                options.push({ value: reference.id, label: reference.name }),
            );

            return options;

        } else {
            return [{ value: '-1', label: '..' }]
        }
    }

    return (
        <Select options={getOptions()} />
    );
}