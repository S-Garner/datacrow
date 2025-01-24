import { Button, Form } from 'react-bootstrap';
import { data, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/authentication_context';
import { fetchResources, type User } from '../../services/datacrow_api';
import { MessageBox, MessageBoxType } from '../../components/message_box';
import { useEffect, useState } from 'react';
import { languages, useTranslation, type Language } from '../../context/translation_context';

export function LoginPage() {
    
    const [success, setSuccess] = useState(true);
    
	let navigate = useNavigate();
	let auth = useAuth();
	
	const { setTranslations, t, language, setLanguage } = useTranslation();
    const [ selectedLanguage, setSelectedLanguge ] = useState(language);
    
    let uiLanguages: { key: string, label: string, language: Language}[] = [
        {key: "English", label: "English", language: languages.english},
        {key: "German", label: "Deutsch", language: languages.german},
        {key: "French", label: "Francais", language: languages.french},
        {key: "Italian", label: "Italiano", language: languages.italien},
        {key: "Dutch", label: "Nederlands", language: languages.dutch},
        {key: "Polski", label: "Polski", language: languages.polski},
        {key: "Portuguese", label: "Português", language: languages.portuguese},
        {key: "Spanish", label: "Español", language: languages.spanish},
        {key: "Russian", label: "Русский", language: languages.russian}
    ]
    
    const handleLanguageSelect = (event: React.ChangeEvent<HTMLSelectElement>): void => {
        if (event.target.value === "Dutch")
            setSelectedLanguge(languages.dutch);
        if (event.target.value === "English")
            setSelectedLanguge(languages.english);
        if (event.target.value === "French")
            setSelectedLanguge(languages.french);
        if (event.target.value === "German")
            setSelectedLanguge(languages.german);
        if (event.target.value === "Italian")
            setSelectedLanguge(languages.italien);            
        if (event.target.value === "Polski")
            setSelectedLanguge(languages.polski);
        if (event.target.value === "Portuguese")
            setSelectedLanguge(languages.portuguese);
        if (event.target.value === "Russian")
            setSelectedLanguge(languages.russian);
        if (event.target.value === "Spanish")
            setSelectedLanguge(languages.spanish);
            
        localStorage.setItem("language", selectedLanguage);
        setLanguage(selectedLanguage);
    };
    
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
        }, 1000);
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
                    <Form.Select onChange={(event) => handleLanguageSelect(event)} key="language-menu">
                        <option value="English">English</option>
                        <option value="German">Deutsch</option>
                        <option value="French">Francais</option>
                        <option value="Italian">Italiano</option>
                        <option value="Dutch">Nederlands</option>
                        <option value="Polski">Polski</option>
                        <option value="Portuguese">Português</option>
                        <option value="Spanish">Español</option>
                        <option value="Russian">Русский</option>
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