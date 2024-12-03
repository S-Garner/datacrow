import { useLocation, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { fetchItem, type FieldValue, type Item } from "../../services/datacrow_api";
import { RequireAuth } from "../../context/authentication_context";
import { useModule } from "../../context/module_context";
import { InputField } from "../../components/input/component_factory";
import Form from 'react-bootstrap/Form';

export function ItemPage() {

	const currentModule = useModule();

	const navigate = useNavigate();
	const [item, setItem] = useState<Item>();
	const { state } = useLocation();

	useEffect(() => {
		if (!state) {
			navigate('/');
		}
	}, []);

	useEffect(() => {
		state && currentModule.selectedModule && fetchItem(currentModule.selectedModule!.index, state.itemID).then((data) => setItem(data));
	}, []);

	if (state && item) {
		return (
			<RequireAuth>
				<div style={{ display: "inline-block", width: "100%" }} key="item-details">
					<Form className="align-items-left" key="form-item-detail">
						{item.fields.map((fieldValue) => (
							InputField(fieldValue.field, fieldValue.value)
						))}
					</Form>
				</div>
			</RequireAuth>
		);
	}
}
