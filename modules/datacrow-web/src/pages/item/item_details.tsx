import { useLocation, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { fetchItem, fetchReferences, type Field, type Item, type References } from "../../services/datacrow_api";
import { RequireAuth } from "../../context/authentication_context";
import { useModule } from "../../context/module_context";
import { Button } from "react-bootstrap";
import Form from 'react-bootstrap/Form';
import InputField from "../../components/input/dc_input_field";

export function ItemPage() {

    const currentModule = useModule();

    useEffect(() => {
        currentModule.selectedModule && fetchItem(currentModule.selectedModule.index, state.itemID).then((data) => setItem(data));
    }, [currentModule.selectedModule]);

    useEffect(() => {
        currentModule.selectedModule && fetchReferences(currentModule.selectedModule.index).then((data) => setReferences(data));
    }, [currentModule.selectedModule]);

	
	const navigate = useNavigate();
	const { state } = useLocation();

    const [item, setItem] = useState<Item>();
    const [references, setReferences] = useState<References[]>();

	useEffect(() => {
		if (!state) {
			navigate('/');
		}
	}, []);
	
	
	const [validated, setValidated] = useState(false);
	
	function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
		event.preventDefault();
		event.stopPropagation();

		setValidated(true);
	};
	
    function ReferencesForField(field: Field) {

        var i = 0;
        while (i < references!.length) {
            if (references![i].moduleIdx === field.referencedModuleIdx)
                return references![i];

            i++;
        }

        return undefined;
    }

	return (
		<RequireAuth>
			<div style={{ display: "inline-block", width: "100%", textAlign: "left" }} key="item-details">
				<Form key="form-item-detail" noValidate validated={validated} onSubmit={handleSubmit} >
					{references && item?.fields.map((fieldValue) => (
                        <InputField
                            field={fieldValue.field}
                            value={fieldValue.value}
                            references={ReferencesForField(fieldValue.field)}
                        />
					))}
					<Button type="submit">Save</Button>
				</Form>
			</div>
		</RequireAuth>
	);
}
