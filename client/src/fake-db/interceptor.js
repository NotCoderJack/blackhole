const MockAdapter = require('axios-mock-adapter');
const axios = require('axios');
const MockInterceptor = new MockAdapter(axios);
export default MockInterceptor;
