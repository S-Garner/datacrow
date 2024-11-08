import { useAuth } from "../context/authentication_context";
import { useNavigate } from "react-router-dom";
import { Button } from "react-bootstrap";

export function UserStatus() {
	let auth = useAuth();
	let navigate = useNavigate();

	if (!auth.user) {
		return <div />;
	}


	return (
		<div style={{ float: "right", top: "0", right: "0"}}>
			&nbsp;&nbsp;<Button  className="main-menu-button"
				onClick={() => {
					auth.signout(() => navigate("/"));
				}}
			> 
			<i className="bi bi-person-fill"></i>
			
			</Button>
		</div>
	);
}

export function ColorStyleMenu() {
	return (
		<div className="mode-switch" style={{ display: "flex", flexWrap: "wrap", float: "right", top: "0" }} >
			<Button className="main-menu-button" data-bs-theme-value="light" aria-pressed="false">
				<i className="bi bi-sun"></i>
			</Button>
			<Button className="main-menu-button" data-bs-theme-value="dark" aria-pressed="false">
				<i className="bi bi-moon"></i>
			</Button>
			<Button className="main-menu-button" data-bs-theme-value="auto" aria-pressed="false">
				<i className="bi bi-display"></i>
			</Button>
		</div>
	);
}

export default function MainMenuBar() {
	return (
		<div className="main-menu-bar" style={{paddingBottom: "8px"}}>
			<UserStatus />
			<ColorStyleMenu />
		</div>
	);
}
