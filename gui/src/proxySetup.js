const { createProxyMiddleware } = require('http-proxy-middleware');

module.exports = function(app) {
    console.log("env = "+process.env.NODE_ENV)
    if (process.env.NODE_ENV !== 'production') {
        app.use(
            createProxyMiddleware(["/api"], { target: "http://examsim_app:9000/api", changeOrigin: true})
        );}
    app.use(
        createProxyMiddleware(["/api"], { target: "http://localhost:9000/api", changeOrigin: true})
    );
};