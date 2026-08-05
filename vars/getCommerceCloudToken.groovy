def call(Map params = [:]) {

    echo "=============================="
    echo ">>> STEP 1: GETTING OAUTH TOKEN"
    echo "=============================="

    // Allow callers to provide the Jenkins credential IDs. If not provided, fall back to the project defaults.
    // Example usage in a Jenkinsfile: getCommerceCloudToken(clientIdCredentialsId: 'my-client-id', clientSecretCredentialsId: 'my-client-secret')
    def clientIdCredentialsId = params.clientIdCredentialsId ?: params.CLIENT_ID_CREDENTIALS_ID ?: 'cc-client-id'
    def clientSecretCredentialsId = params.clientSecretCredentialsId ?: params.CLIENT_SECRET_CREDENTIALS_ID ?: 'cc-client-secret'

    // Optionally allow passing raw client id/secret directly (not recommended for production):
    def providedClientId = params.clientId ?: params.CLIENT_ID
    def providedClientSecret = params.clientSecret ?: params.CLIENT_SECRET

    def tokenResponse
    echo "Using credential IDs: ${providedClientId} / ${providedClientSecret}"
    if (providedClientId && providedClientSecret) {
       echo "Using credential IDs: providedClientId / providedClientSecret"

        // Use provided values directly
        tokenResponse = sh(
                script: """
            curl --compressed -sS --location --request POST \
            'https://ycloud.accounts.ondemand.com/oauth2/token' \
            -H 'Content-Type: application/x-www-form-urlencoded' \
            -H 'Accept: application/json' \
            --data-urlencode "client_id=${providedClientId}" \
            --data-urlencode "client_secret=${providedClientSecret}" \
            --data-urlencode 'grant_type=client_credentials' \
            --data-urlencode 'resource=urn:sap:identity:application:provider:name:cp-dependency'
            """,
                returnStdout: true
        ).trim()
    } else {
        // Use Jenkins credentials (credentialsId) so secrets are kept in Jenkins
        withCredentials([
                string(credentialsId: clientIdCredentialsId, variable: 'CLIENT_ID'),
                string(credentialsId: clientSecretCredentialsId, variable: 'CLIENT_SECRET')
        ]) {

            echo "Using credential IDs: ${clientIdCredentialsId} / ${clientSecretCredentialsId}"

            tokenResponse = sh(
                    script: """
            curl --compressed -sS --location --request POST \
            'https://ycloud.accounts.ondemand.com/oauth2/token' \
            -H 'Content-Type: application/x-www-form-urlencoded' \
            -H 'Accept: application/json' \
            --data-urlencode "client_id=${CLIENT_ID}" \
            --data-urlencode "client_secret=${CLIENT_SECRET}" \
            --data-urlencode 'grant_type=client_credentials' \
            --data-urlencode 'resource=urn:sap:identity:application:provider:name:cp-dependency'
            """,
                    returnStdout: true
            ).trim()
        }
    }

    if (!tokenResponse?.startsWith("{")) {
        error("TOKEN API FAILED:\n${tokenResponse}")
    }

    def json = readJSON text: tokenResponse

    if (!json.access_token) {
        error("No access_token in response")
    }

    echo ">>> TOKEN GENERATED SUCCESSFULLY ✅"

    return json.access_token
}
