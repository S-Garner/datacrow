import { useLocation, useNavigate } from "react-router-dom";
import { useEffect, useState } from "react";
import { fetchItem, type FieldValue, type Item } from "../../services/datacrow_api";
import { RequireAuth } from "../../context/authentication_context";
import { useModule } from "../../context/module_context";
import Form from 'react-bootstrap/Form';

export function ItemPage() {

	const currentModule = useModule();

	const navigate = useNavigate();
	const [item, setItem] = useState<Item>();
	const {state} = useLocation();
	
	useEffect(() => {
	    if (!state) {
			navigate('/');
		}
	  }, []);
	
	useEffect(() => {
			state && currentModule.selectedModule && fetchItem(currentModule.selectedModule!.index, state.itemID).then((data) => setItem(data));
		}, []);
	
	
	function CreateItemField(field : FieldValue) {
		return (
			<Form className="align-items-left">
				<div className="row mb-2">
					<div className="col-auto" style={{width: "25em"}}>
						<Form.Label>{field.field.label}</Form.Label>
					</div>
					<div className="col-auto">
						<Form.Control value={field.value} />
					</div>
				</div>
			</Form>
		);
	}
	
	if (state && item) {
		return (
			<RequireAuth>
				<div style={{display: "inline-block", width:"100%"}} key="item-details">
					{item.fields.map((fieldValue) => (
						CreateItemField(fieldValue)				
					))}
				</div>
			</RequireAuth>
		);
	} 
}
