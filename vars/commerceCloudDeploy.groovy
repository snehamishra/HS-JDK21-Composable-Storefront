def call(buildCode, dbUpdateMode, environmentId, strategy,subscriptionid) {

    echo "##### Initiate Deployment to SAP Commerce Cloud Environment #####"

    withCredentials([
            string(credentialsId: 'commerceCloudSubscriptionCode',
                    variable: 'SUBSCRIPTION_CODE')
    ]) {

        def token = getCommerceCloudToken()

        def responseFile = "deploy_response.json"

        def status = sh(
                script: """
            curl --compressed -sS \
            -o ${responseFile} \
            -w "%{http_code}" \
            --location --request POST \
            "https://portalapi.commerce.ondemand.com/v2/subscriptions/${subscriptionid}/deployments" \
            --header "Content-Type: application/json" \
            --header "Accept: application/json" \
            --header "x-approuter-authorization: Bearer ${token}" \
            --data-raw '{
              "buildCode":"${buildCode}",
              "databaseUpdateMode":"${dbUpdateMode}",
              "environmentCode":"${environmentId}",
              "strategy":"${strategy}"
            }'
            """,
                returnStdout: true
        ).trim()

        def body = readFile(responseFile).trim()

        echo "=============================="
        echo "DEPLOY HTTP STATUS = ${status}"
        echo "DEPLOY RESPONSE:"
        echo body
        echo "=============================="

        if (!(status in ["200", "201", "202"])) {
            error("Deployment API FAILED with HTTP ${status}")
        }

        if (!body.startsWith("{")) {
            error("Deployment API did not return valid JSON:\n${body}")
        }

        def json = readJSON text: body

        echo "=============================="
        echo "DEPLOYMENT TRIGGERED SUCCESSFULLY"
        echo "DEPLOYMENT CODE = ${json.code}"
        echo "=============================="

        return json.code
    }
}
