def call() {

    echo "=============================="
    echo ">>> STEP 1: GETTING OAUTH TOKEN"
    echo "=============================="

    def tokenResponse

    withCredentials([
            string(credentialsId: 'cc-client-id', variable: 'CLIENT_ID'),
            string(credentialsId: 'cc-client-secret', variable: 'CLIENT_SECRET')
    ]) {

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

    if (!tokenResponse.startsWith("{")) {
        error("TOKEN API FAILED:\n${tokenResponse}")
    }

    def json = readJSON text: tokenResponse

    if (!json.access_token) {
        error("No access_token in response")
    }

    echo ">>> TOKEN GENERATED SUCCESSFULLY ✅"

    return json.access_token
}