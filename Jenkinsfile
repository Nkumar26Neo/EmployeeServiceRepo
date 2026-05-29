pipeline {
agent any

    tools {
        // This matches the name you configured in Jenkins Tools UI.
        // Jenkins handles downloading it and setting up the PATH for Windows/Linux.
        maven 'maven3'
    }

    environment {
        APP_NAME       = 'employee-service'
        REGISTRY       = 'registry.skygrid.io'
        IMAGE_TAG      = "1.0.${BUILD_NUMBER}"
        CONTAINER_NAME = "employee-service-api"
        APP_PORT       = "8080"
        PODMAN_BIN     = 'podman'
    }

    stages {
        stage('Checkout Source') {
            steps {
                checkout scm
            }
        }

        stage('Build & Compile') {
            steps {
                echo 'Compiling Java classes...'
                // No more './mvnw' or 'mvnw.cmd' checks! Just use 'mvn'.
                // The 'withMaven' wrapper takes care of cross-platform execution.
                withMaven {
                    if (isUnix()) {
                        sh 'mvn clean compile -DskipTests'
                    } else {
                        bat 'mvn clean compile -DskipTests'
                    }
                }
            }
        }

        stage('Execute Tests') {
            steps {
                echo 'Running testing suite...'
                withMaven {
                    if (isUnix()) {
                        sh 'mvn test'
                    } else {
                        bat 'mvn test'
                    }
                }
            }
            post {
                always {
                    junit '**/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Package Application') {
            steps {
                echo 'Packaging application fat executable JAR...'
                withMaven {
                    if (isUnix()) {
                        sh 'mvn package -DskipTests'
                    } else {
                        bat 'mvn package -DskipTests'
                    }
                }
                archiveArtifacts artifacts: 'target/*.jar', fingerprint: true
            }
        }

        stage('Build Podman Image') {
            steps {
                echo "Compiling container image using local PODMAN daemon..."
                script {
                    if (isUnix()) {
                        sh "${PODMAN_BIN} build -t ${REGISTRY}/${APP_NAME}:${IMAGE_TAG} ."
                        sh "${PODMAN_BIN} tag ${REGISTRY}/${APP_NAME}:${IMAGE_TAG} ${REGISTRY}/${APP_NAME}:latest"
                    } else {
                        bat "${PODMAN_BIN} build -t ${REGISTRY}/${APP_NAME}:${IMAGE_TAG} ."
                        bat "${PODMAN_BIN} tag ${REGISTRY}/${APP_NAME}:${IMAGE_TAG} ${REGISTRY}/${APP_NAME}:latest"
                    }
                }
            }
        }

        stage('Deploy as Podman Container') {
            steps {
                echo 'Deploying application to local Podman runtime...'
                script {
                    if (isUnix()) {
                        // 1. Stop and remove the existing container if it is already running (Unix)
                        sh """
                            if ${PODMAN_BIN} ps -a --format '{{.Names}}' | grep -Eq "^${CONTAINER_NAME}\$"; then
                                echo "Stopping and removing existing container : ${CONTAINER_NAME} ..."
                                ${PODMAN_BIN} stop ${CONTAINER_NAME} || true
                                ${PODMAN_BIN} rm ${CONTAINER_NAME} || true
                            fi
                        """
                        // 2. Run the new container mapping port 8080 (Unix)
                        sh "${PODMAN_BIN} run -d --name ${CONTAINER_NAME} -p ${APP_PORT}:${APP_PORT} ${REGISTRY}/${APP_NAME}:${IMAGE_TAG}"
                    } else {
                        // 1. Stop and remove the existing container if it is already running (Windows Batch)
                        // Uses podman inspect to avoid grep dependencies on native CMD environments
                        bat """
                            ${PODMAN_BIN} inspect ${CONTAINER_NAME} >nul 2>&1
                            if %ERRORLEVEL% EQU 0 (
                                echo "Stopping and removing existing container : ${CONTAINER_NAME} ..."
                                ${PODMAN_BIN} stop ${CONTAINER_NAME}
                                ${PODMAN_BIN} rm ${CONTAINER_NAME}
                            )
                        """
                        // 2. Run the new container mapping port 8080 (Windows Batch)
                        bat "${PODMAN_BIN} run -d --name ${CONTAINER_NAME} -p ${APP_PORT}:${APP_PORT} ${REGISTRY}/${APP_NAME}:${IMAGE_TAG}"
                    }
                }
                echo "Deployment complete. Application is available at http://localhost:8080/EmployeeService/api/employees/health"
            }
        }
    }

    post {
        success {
            echo '==================================================='
            echo 'MICROSERVICE PIPELINE SUCCESSFUL'
            echo '==================================================='
        }
        failure {
            echo '==================================================='
            echo 'MICROSERVICE PIPELINE FAILED (ALERT SENT)'
            echo '==================================================='
        }
    }
}