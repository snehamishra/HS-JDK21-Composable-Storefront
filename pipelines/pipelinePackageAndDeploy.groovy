def codeNumber;

pipeline {
    libraries {
        lib("shared-library@${params.LIBRARY_BRANCH}")
    }
    agent {
        node {
            label 'Built-In'
        }
    }
    options {
        skipDefaultCheckout(true) // No more 'Declarative: Checkout' stage
        timeout(time: 1, unit: 'HOURS') // build should take no more than 1 hour
    }

    stages {
        stage('Prepare') {
            steps{
                script{
                    currentBuild.displayName = "#${currentBuild.number}-${params.PROJECT_TAG}-${params.BUILD_NAME}"
                }
            }
        }

        stage('Build') {
            steps{
                script{
                    def ccCreds = [
                        clientIdCredentialsId: params.COMMERCE_CLIENT_ID_CREDENTIALS_ID,
                        clientSecretCredentialsId: params.COMMERCE_CLIENT_SECRET_CREDENTIALS_ID
                    ]

                    codeNumber = buildCommerceCloud(params.PROJECT_TAG, params.BUILD_NAME, params.SUBSCRIPTION_ID, ccCreds)
                    buildCommerceCloudCheck(codeNumber, params.SUBSCRIPTION_ID, ccCreds)
                }
            }
        }

       stage('Deploy') {
            steps{
                script{
                    def ccCreds = [
                        clientIdCredentialsId: params.COMMERCE_CLIENT_ID_CREDENTIALS_ID,
                        clientSecretCredentialsId: params.COMMERCE_CLIENT_SECRET_CREDENTIALS_ID
                    ]

                    deploymentCode = commerceCloudDeploy(codeNumber, params.DB_UPDATE_MODE, params.ENVIRONMENT_ID, params.DEPLOY_STRATEGY, params.SUBSCRIPTION_ID, ccCreds)
                    commerceCloudDeployCheck(deploymentCode, params.SUBSCRIPTION_ID, ccCreds)
                }
            }
        }
/*        stage('Deploy') {
            when {
                expression { false }
            }
            steps {
                echo "Deployment disabled"
            }
        }*/
    }

    // post build actions
    post {
        failure {
            echo "Issue with build package and deploy"
        }
    }
}
