def call(branch, buildName) {

    echo "=============================="
    echo ">>> STEP 2: BUILD INITIATED"
    echo ">>> branch: ${branch}"
    echo ">>> buildName: ${buildName}"
    echo "=============================="

    def token = getCommerceCloudToken()

    withCredentials([
            string(credentialsId: 'commerceCloudSubscriptionCode',
                    variable: 'SUBSCRIPTION_CODE')
    ]) {

        echo ">>> STEP 3: CALLING BUILD API"
        echo ">>> Using subscriptionCode: ${SUBSCRIPTION_CODE}"

        def responseFile = "build_response.json"

        def status = sh(
                script: """
                curl --compressed -sS \
                -o ${responseFile} \
                -w "%{http_code}" \
                --location --request POST \
                "https://portalapi.commerce.ondemand.com/v2/subscriptions/${SUBSCRIPTION_CODE}/builds" \
                --header "Content-Type: application/json" \
                --header "Accept: application/json" \
                --header "x-approuter-authorization: Bearer ${token}" \
                --data-raw '{"branch":"${branch}","name":"${buildName}"}'
                """,
                returnStdout: true
        ).trim()

        def body = readFile(responseFile).trim()

        echo "=============================="
        echo "HTTP STATUS = ${status}"
        echo "BODY:"
        echo body
        echo "=============================="

        if (!(status in ["200", "201", "202"])) {
            error("Build API FAILED with HTTP ${status}")
        }

        def json = readJSON text: body

        echo "=============================="
        echo "BUILD TRIGGERED SUCCESSFULLY"
        echo "CODE = ${json.code}"
        echo "=============================="

        return json.code
    }
}