def call(codeNumber, subscriptionid, Map params = [:]) {

    echo "=============================="
    echo ">>> STEP 6: BUILD STATUS CHECK STARTED"
    echo ">>> codeNumber: ${codeNumber}"
    echo "=============================="

    boolean buildCompleted = false

    def clientIdCredentialsId = params.clientIdCredentialsId ?: 'cc-client-id'
    def clientSecretCredentialsId = params.clientSecretCredentialsId ?: 'cc-client-secret'

    while (!buildCompleted) {

        withCredentials([
                string(credentialsId: 'commerceCloudSubscriptionCode',
                        variable: 'SUBSCRIPTION_CODE')
        ]) {

            def token = getCommerceCloudToken(clientIdCredentialsId: clientIdCredentialsId, clientSecretCredentialsId: clientSecretCredentialsId)

            echo ">>> CALLING STATUS API FOR CODE: ${codeNumber}"

            def result = sh(
                    script: """
                curl -sS --location \
                'https://portalapi.commerce.ondemand.com/v2/subscriptions/${subscriptionid}/builds/${codeNumber}' \
                --header 'x-approuter-authorization: Bearer ${token}'
                """,
                    returnStdout: true
            ).trim()

            echo ">>> RAW STATUS RESPONSE:"
            echo result

            if (!result.startsWith("{")) {
                error("STATUS API FAILED - NOT JSON: ${result}")
            }

            def json = readJSON text: result

            echo ">>> STATUS = ${json.status}"

            if (json.status == "SUCCESS") {
                echo ">>> BUILD SUCCESS ✅"
                buildCompleted = true

            } else if (json.status == "FAIL") {
                error("BUILD FAILED IN SAP COMMERCE CLOUD")
            }
        }

        if (!buildCompleted) {
            echo ">>> WAITING 120 SECONDS BEFORE NEXT POLL"
            sleep 120
        }
    }

    echo "=============================="
    echo ">>> STEP 7: BUILD COMPLETED SUCCESSFULLY"
    echo "=============================="
}
