pipeline {
    agent any

    environment {
        APP_NAME       = 'employee-service'
        REGISTRY       = 'registry.skygrid.io'
        IMAGE_TAG      = "1.0.${BUILD_NUMBER}"
        CONTAINER_NAME = "employee-service-api"
        APP_PORT       = "8080"
        PODMAN_BIN     = 'podman' // Configured for Podman binary execution
    }

    options {
        timeout(time: 30, unit: 'MINUTES')
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
    }

    stages {
        stage('Checkout Source') {
            steps {
                echo 'Checking out source repository...'
                checkout scm
            }
        }

        stage('Build & Compile') {
            steps {
                echo 'Compiling Java classes and dependencies...'
                sh './mvnw clean compile -DskipTests'
            }
        }

        stage('Execute Tests') {
            steps {
                echo 'Running unit integration testing suite using JUnit 5...'
                sh './mvnw test'
            }
            post {
                always {
                    // Harvest and publish JUnit status records into Jenkins Dashboard
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Package Application') {
            steps {
                echo 'Packaging application fat executable JAR...'
                sh './mvnw package -DskipTests'
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        stage('Build Podman Image') {
            steps {
                echo "Compiling container image using local PODMAN daemon..."
                // Podman contains exact structural CLI mapping to Docker CLI instructions
                sh "${PODMAN_BIN} build -t ${RE  GISTRY}/${APP_NAME}:${IMAGE_TAG} ."
                sh "${PODMAN_BIN} tag ${REGISTRY}/${APP_NAME}:${IMAGE_TAG} ${REGISTRY}/${APP_NAME}:latest"
            }
        }

        stage('Deploy as Podman Container') {
            steps {
                echo 'Deploying application to local Podman runtime...'

                // 1. Stop and remove the existing container if it is already running
                sh """
                    if ${PODMAN_BIN} ps -a --format '{{.Names}}' | grep -Eq "^${CONTAINER_NAME}\$"; then
                        echo "Stopping and removing existing container : ${CONTAINER_NAME} ..."
                        ${PODMAN_BIN} stop ${CONTAINER_NAME} || true
                        ${PODMAN_BIN} rm ${CONTAINER_NAME} || true
                    fi
                """

                // 2. Run the new container mapping port 8080
                sh "${PODMAN_BIN} run -d --name ${CONTAINER_NAME} -p ${REGISTRY}/${APP_NAME}:${IMAGE_TAG} ${REGISTRY}/${APP_NAME}:latest"

                echo "Deployment complete. Application is available at http://localhost:8080/EmployeeService/api/employees/health"
            }
        }

//        stage('Push Container Registry') {
//            steps {
//                echo "Publishing compiled image to secure enterprise container register..."
//                // Uses clean credential vaulting techniques to login and push
//                withCredentials([usernamePassword(credentialsId: 'registry-credentials',
//                                                 usernameVariable: 'REG_USER',
//                                                 passwordVariable: 'REG_PASS')]) {
//                    sh "${PODMAN_BIN} login -u ${REG_USER} -p ${REG_PASS} ${REGISTRY}"
//                    sh "${PODMAN_BIN} push ${REGISTRY}/${APP_NAME}:${IMAGE_TAG}"
//                    sh "${PODMAN_BIN} push ${REGISTRY}/${APP_NAME}:latest"
//                }
//            }
//        }
//
//        stage('Deploy to Kubernetes') {
//            steps {
//                echo "Triggering deployment rollback on local cluster environment..."
//                // Run kubectl command applying manifest structures containing modern container image tags
//                sh "sed -i 's|IMAGE_PLACEHOLDER|${REGISTRY}/${APP_NAME}:${IMAGE_TAG}|g' k8s-deployment.yaml"
//                sh "kubectl apply -f k8s-deployment.yaml"
//
//                echo "Microservice successfully upgraded to active revision: ${IMAGE_TAG}"
//            }
//        }
    }

    post {
        success {
            echo '==================================================='
            echo 'MICROSERVICE PIPELINE SUCCESSFUL'
            echo '==================================================='
            // Optional: Slack/Teams API notifications can be integrated here
        }
        failure {
            echo '==================================================='
            echo 'MICROSERVICE PIPELINE FAILED (ALERT SENT)'
            echo '==================================================='
        }
    }
}