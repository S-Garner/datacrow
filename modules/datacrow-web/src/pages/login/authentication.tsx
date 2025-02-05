import { Button, Form } from 'react-bootstrap';
import { data, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/authentication_context';
import { fetchResources, type User } from '../../services/datacrow_api';
import { MessageBox, MessageBoxType } from '../../components/message_box';
import { useEffect, useState } from 'react';
import { useTranslation, languageArray } from '../../context/translation_context';

export function LoginPage() {
    
    const [success, setSuccess] = useState(true);
    
	let navigate = useNavigate();
	let auth = useAuth();
	
	const { setTranslations, t, language, setLanguage } = useTranslation();
    const [ selectedLanguage, setSelectedLanguage ] = useState(language);
    
    const handleLanguageSelect = (event: React.ChangeEvent<HTMLSelectElement>): void => {
        applyLanguage(event.target.value);
    }
    
    function applyLanguage(key: string) {
        for (const lang of languageArray) {
            if (lang.key === key) {
                setSelectedLanguage(lang.language);
                localStorage.setItem("language", lang.key);
                setLanguage(selectedLanguage);
            }
        }
    }

    useEffect(() => {
        fetchResources(selectedLanguage).then((data) => setTranslations(data)).catch(error => 
        {
            console.log(error);
            if (error.status === 401) {
                navigate("/login");    
            }
        });
    }, [selectedLanguage]);

	function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
		event.preventDefault();

		let formData = new FormData(event.currentTarget);
		let username = formData.get("username") as string;
		let password = formData.get("password") as string;

		auth.signin(username, password, callback);
		
        setTimeout(function() {
            setSuccess(false);
        }, 1500);
	}
	
	/**
     * Purely used to make sure we can display labels when the resources have not yet been retrieved, 
     * or if there was an error in retrieving the resources.
     */
	function translateOrDefault(tag : string, fallback : string) {
        let s = t(tag);
        
        if (s === tag || s === "undefined")
            s = fallback;
        
        return String(s);
    }

    function callback(user : User | null) {
        if (user) {
            setSuccess(true);
            localStorage.setItem("token", user.token);
            navigate("/", { replace: true });
        } else {
            setSuccess(false);
            localStorage.removeItem("token");
            console.debug("The user not authorized");
        }
    }
 
	return (
		<div style={{position:"absolute", top:"50%", left:"50%", marginTop: "-50px", marginLeft: "-200px", width: "400px", height: "100px"}}>
		    
		    { !success && <MessageBox
                    header={translateOrDefault("msgLoginFailed", "Login Failed")}
                    message={translateOrDefault("msgLoginFailedDetails", "Invalid credentials were provided. Please enter the correct username and password.")}
                    type={MessageBoxType.warning} /> }
		
            <form onSubmit={handleSubmit}>
                <div className="row mb-2" >
                    <Form.Select onChange={(event) => handleLanguageSelect(event)} key="language-menu" defaultValue={language}>
                        {languageArray.map((lang) => (
                            <option value={lang.key} key={lang.key}>
                                {lang.label}
                            </option>
                        ))}
                    </Form.Select>
                </div>
			
				<div className="row mb-2">
					<input name="username" type="text" placeholder={translateOrDefault("lblUsername", "Username")} className="form-control" id="input-username" required={true} />
				</div>

				<div className="row mb-2">
					<input name="password" type="Password" placeholder={translateOrDefault("lblPassword", "Password")} className="form-control" id="input-password" required={true} />
				</div>

				<div className="row mb-2">
					<Button type="submit">{translateOrDefault("lblLogin", "Login")}</Button>
				</div>
			</form>
		</div>
	);
}