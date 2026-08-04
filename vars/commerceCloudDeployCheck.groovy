def call(deployCode, subscriptionid, Map params = [:]) {
    script {
        while (true) {
          withCredentials([
              string(credentialsId: 'commerceCloudSubscriptionCode', variable: 'subscriptionCode')
          ]) {
              def clientIdCredentialsId = params.clientIdCredentialsId ?: 'cc-client-id'
              def clientSecretCredentialsId = params.clientSecretCredentialsId ?: 'cc-client-secret'
              def token = getCommerceCloudToken(clientIdCredentialsId: clientIdCredentialsId, clientSecretCredentialsId: clientSecretCredentialsId)
              result = sh (script: "curl --location --request GET 'https://portalapi.commerce.ondemand.com/v2/subscriptions/${subscriptionid}/deployments/$deployCode' --header 'x-approuter-authorization: Bearer ${token}' --silent", returnStdout: true)
          }
          echo "$result"
          statusResult = readJSON text: "$result"

          if("DEPLOYED".equals(statusResult["status"])) {
            break;
          }

          if("FAIL".equals(statusResult["status"])) {
            error("Deployment was not completed successfully on SAP Commerce Cloud")
          }

          sh('sleep 120s')

        }

        echo "Commerce Cloud Deploy Complete"
    }
}  
