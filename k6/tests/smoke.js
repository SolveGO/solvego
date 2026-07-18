import http from "k6/http";
import { check } from "k6";

import { getBaseUrl } from "../config/environment.js";

const problemsUrl = `${getBaseUrl()}/api/problems`;

export const options = {
    vus: 1,
    iterations: 1,

    thresholds: {
        checks: ["rate==1"],
        http_req_failed: ["rate==0"],
    },
};

export default function smokeTest() {
    const response = http.get(problemsUrl);

    check(response, {
        "returns HTTP 200": ({ status }) => status === 200,
    });
}