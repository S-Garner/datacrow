import { Button } from 'react-bootstrap';
import { useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/authentication_context';


export function LoginPage() {
	let navigate = useNavigate();
	let location = useLocation();
	let auth = useAuth();

	let from = location.state?.from?.pathname || "/";
	
	function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
		event.preventDefault();

		let formData = new FormData(event.currentTarget);
		let username = formData.get("username") as string;

		auth.signin(username, () => {
			// Send them back to the page they tried to visit when they were
			// redirected to the login page. Use { replace: true } so we don't create
			// another entry in the history stack for the login page.  This means that
			// when they get to the protected page and click the back button, they
			// won't end up back on the login page, which is also really nice for the
			// user experience.
			navigate(from, { replace: true });
		});
	}

	return (
		<div style={{position:"absolute", top:"50%", left:"50%", marginTop: "-50px", marginLeft: "-200px", width: "400px", height: "100px"}}>
			<form onSubmit={handleSubmit}>
				<div className="row mb-2">
					<input name="username" type="text" placeholder="Username" className="form-control" id="input-username" />
				</div>

				<div className="row mb-2">
					<input name="password" type="Password" placeholder="Password" className="form-control" id="input-password" />
				</div>

				<div className="row mb-2">
					<Button type="submit">Login</Button>
				</div>
			</form>
		</div>
	);
}