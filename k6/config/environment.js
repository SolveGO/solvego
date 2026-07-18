
const environments = {
    local: {
        baseUrl: "http://localhost:8080",
    },
    ec2: {
        baseUrl: "http://3.36.54.41:8080",
    },
};

export function getBaseUrl() {
    const environmentName = __ENV.ENV || "local";
    const environment = environments[environmentName];

    if (!environment) {
        throw new Error(`Unknown environment: ${environmentName}`);
    }

    return environment.baseUrl;
}