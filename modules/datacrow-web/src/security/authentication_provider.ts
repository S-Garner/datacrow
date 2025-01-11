import { login, type User, type LoginCallBack } from "../services/datacrow_api";

const authenticationProvider = {
    signin(username : string, password: string, callback: LoginCallBack) {
        login(username, password).then(user => handleLogin(user, callback));
    },
    signout(callback: VoidFunction) {
        setTimeout(callback, 100);
    },
};

function handleLogin(user: User, callback: LoginCallBack) {
    setTimeout(() => callback(user), 100)
}

export { authenticationProvider };
