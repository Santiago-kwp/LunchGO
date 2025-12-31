import axios from "axios";
import { useAccountStore } from "@/stores/account";

const instance = axios.create(
    {withCredentials: true} // refresh token cookie attach
);

// response interceptor
instance.interceptors.response.use((res) => {
    return res;
}, async (err) => {
    const accountStore = useAccountStore();

    if (!err.response) {
        return Promise.reject(err);
    }

    switch (err.response.status) {
        case 401: {
            const isLoginRequest =
                err.config?.skipAuth ||
                err.config?.url?.includes('/api/login') ||
                err.config?.url?.endsWith('/login');
            if (isLoginRequest) {
                return Promise.reject(err);
            }

            const config = err.config || {};
            const currentToken = accountStore.accessToken || localStorage.getItem('accessToken');

            if (!currentToken) {
                return Promise.reject(err);
            }

            if (config.retried) {
                window.alert('로그인 세션이 만료되었습니다.');
                accountStore.clearAccount();
                localStorage.removeItem('accessToken');
                localStorage.removeItem('member');
                window.location.replace('/');
                return Promise.reject(err);
            }

            const res = await axios.get('/api/refresh', {withCredentials: true});

            const {accessToken} = res.data || {};
            if (!accessToken) throw new Error('토큰 리프레시 에러');

            accountStore.setAccessToken(accessToken);
            localStorage.setItem('accessToken', accessToken);

            config.headers = config.headers || {};
            config.headers.Authorization = `Bearer ${accessToken}`;
            config.retried = true;

            return instance(config);
        }
        default:
            return Promise.reject(err);
    }
});

const generateConfig = (options = {}) => {
    if (options.skipAuth) {
        return options.headers ? { headers: { ...options.headers } } : {};
    }

    const accountStore = useAccountStore();
    const token = accountStore.accessToken || localStorage.getItem('accessToken');
    const headers = options.headers ? { ...options.headers } : {};

    if (token) {
        headers.Authorization = `Bearer ${token}`;
    }

    return Object.keys(headers).length ? { headers } : {};
}

export default {
    get(url, params) {
        const config = generateConfig();
        config.params = params;
        return instance.get(url, config);
    },
    post(url, params, options = {}) {
        return instance.post(url, params, generateConfig(options));
    },
    put(url, params, options = {}) {
        return instance.put(url, params, generateConfig(options));
    },
    delete(url, options = {}) {
        return instance.delete(url, generateConfig(options));
    },
};
