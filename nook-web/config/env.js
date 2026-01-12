const defaultBaseUrl = 'http://localhost:8080';
let baseUrl = localStorage.getItem('baseApi');
if (!baseUrl || baseUrl.includes(':8081')) {
	baseUrl = defaultBaseUrl;
	localStorage.setItem('baseApi', baseUrl);
}
export {
	baseUrl,
}
