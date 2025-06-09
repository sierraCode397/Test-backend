pipeline {
    agent { label 'worker-agents-02' }

    options {
        buildDiscarder(logRotator(numToKeepStr: '10', artifactNumToKeepStr: '5'))
        timestamps()
    }

    tools {
        maven 'maven3'
    }

    environment {
        DOCKERHUB_CREDENTIALS_ID = 'dockerhub-creds'
        DOCKERHUB_USERNAME       = 'isaacluisjuan107'
        APP_NAME                 = 'primarket-backend-dev'
        BACKEND_IMAGE_NAME       = "${DOCKERHUB_USERNAME}/${APP_NAME}"
        IMAGE_TAG                = "build-${env.BUILD_NUMBER}"
        TARGET_HOST_IP           = '192.168.0.186'
        SSH_USER_ON_TARGET       = 'primaket'

        BACKEND_HOST_PORT        = 8890
        BACKEND_CONTAINER_NAME   = 'primarket_prod_backend'

        SSH_CREDENTIAL_ID        = 'primarket-ssh'

        // Application URLs
        VERIFICATION_URL         = "http://${TARGET_HOST_IP}:${BACKEND_HOST_PORT}"
        SPRINT_BOOT_PUBLIC_API_BASE_URL_FOR_BUILD = "https://primarket-dev.codershub.top"
    }

    parameters {
        string(name: 'GIT_BRANCH_BACKEND', defaultValue: 'develop', description: 'Git branch for Primarket Backend App code')
    }

    stages {
        stage('Lint') {
            steps {
                catchError(buildResult: 'UNSTABLE', stageResult: 'UNSTABLE') {
                    sh "mvn checkstyle:checkstyle"
                }
            }
            post {
                always {
                    archiveArtifacts artifacts: 'target/checkstyle-result.xml', fingerprint: true
                    recordIssues tools: [checkStyle(pattern: 'target/checkstyle-result.xml')]
                }
            }
        }

        stage('Checkout Backend Code') {
            agent { label 'worker-agents-02' }
            steps {
                echo "Preparing workspace and checking out Primarket Backend code from branch ${params.GIT_BRANCH_BACKEND}"
                cleanWs()

                git url: 'git@github.com:Primarke/Front-Primarket.git',
                    credentialsId: 'jenkins-github-ssh',
                    branch: params.GIT_BRANCH_BACKEND

                sh """
                    set -e
                    echo "Checkout complete. Current commit: \$(git rev-parse HEAD)"
                    echo "Workspace contents:"
                    ls -la
                """
            }
        }

        stage('Build Backend Docker Image') {
            agent { label 'worker-agents-02' }
            steps {
                echo "Building Backend Docker image: ${env.BACKEND_IMAGE_NAME}:${env.IMAGE_TAG}"
                echo "Using SPRINT_BOOT_PUBLIC_API_BASE_URL=${env.SPRINT_BOOT_PUBLIC_API_BASE_URL_FOR_BUILD} for the build."
                sh """
                    set -e
                    BUILD_SUCCESSFUL=false
                    for i in \$(seq 1 3); do
                        if docker build \
                                --build-arg SPRINT_BOOT_PUBLIC_API_BASE_URL="${env.SPRINT_BOOT_PUBLIC_API_BASE_URL_FOR_BUILD}" \
                                -t "${env.BACKEND_IMAGE_NAME}:${env.IMAGE_TAG}" \
                                -f Dockerfile \
                                .; then
                            BUILD_SUCCESSFUL=true
                            break
                        fi
                        echo "Backend build failed, attempt \$i of 3. Retrying in 30 seconds..."
                        sleep 30
                    done
                    if [ "\$BUILD_SUCCESSFUL" = "false" ]; then
                        echo "ERROR: Backend build failed after 3 attempts."
                        exit 1
                    fi
                    docker tag "${env.BACKEND_IMAGE_NAME}:${env.IMAGE_TAG}" "${env.BACKEND_IMAGE_NAME}:latest"
                    echo "Backend image built: ${env.BACKEND_IMAGE_NAME}:${env.IMAGE_TAG} and tagged as :latest"
                """
            }
        }

        stage('Push Backend Image to Docker Hub') {
            agent { label 'worker-agents-02' }
            steps {
                withCredentials([usernamePassword(credentialsId: "${env.DOCKERHUB_CREDENTIALS_ID}", usernameVariable: 'DOCKER_LOGIN_USER', passwordVariable: 'DOCKER_LOGIN_PASS')]) {
                    sh """
                        set -e
                        echo "Logging into Docker Hub as ${env.DOCKERHUB_USERNAME}"
                        echo "\$DOCKER_LOGIN_PASS" | docker login -u "${env.DOCKERHUB_USERNAME}" --password-stdin

                        for target_tag in "${env.IMAGE_TAG}" "latest"; do
                            TARGET_IMAGE_WITH_TAG="${env.BACKEND_IMAGE_NAME}:\${target_tag}"
                            echo "Pushing \$TARGET_IMAGE_WITH_TAG"
                            PUSH_SUCCESSFUL=false
                            for i in \$(seq 1 3); do
                                if docker push "\$TARGET_IMAGE_WITH_TAG"; then
                                    PUSH_SUCCESSFUL=true
                                    break
                                fi
                                echo "Push for \$TARGET_IMAGE_WITH_TAG failed, attempt \$i of 3. Retrying in 10 seconds..."
                                sleep 10
                            done
                            if [ "\$PUSH_SUCCESSFUL" = "false" ]; then
                                echo "ERROR: Failed to push \$TARGET_IMAGE_WITH_TAG to Docker Hub."
                                exit 1
                            fi
                        done
                        echo "Image push for ${env.BACKEND_IMAGE_NAME} completed."
                    """
                }
            }
        }

        stage('Ensure Database on VM') {
            agent { label 'worker-agents-02' }
            steps {
                withCredentials([
                    string(credentialsId: 'db-host',     variable: 'DB_HOST'),
                    string(credentialsId: 'db-port',     variable: 'DB_PORT'),
                    string(credentialsId: 'db-name',     variable: 'DB_NAME'),
                    usernamePassword(credentialsId: 'db-postgres', usernameVariable: 'DB_USERNAME', passwordVariable: 'DB_PASSWORD')
                ]) {

                    script {
                        env.DB_URL = "jdbc:postgresql://${env.DB_HOST}:${env.DB_PORT}/${env.DB_NAME}"
                    }

                    sshagent (credentials: [env.SSH_CREDENTIAL_ID]) {
                        sh ('
                            set -e

                            SSH_TARGET="${SSH_USER_ON_TARGET}@${TARGET_HOST_IP}"

                            ssh -o StrictHostKeyChecking=no \$SSH_TARGET \
                            'docker inspect my-postgres >/dev/null 2>&1 || \
                            docker run -d --name my-postgres \
                                --network primarket \
                                -p 5432:5432 \
                                -e POSTGRES_DB=${DB_NAME} \
                                -e POSTGRES_USER=${DB_USERNAME} \
                                -e POSTGRES_PASSWORD='${DB_PASSWORD}' \
                                -v pgdata:/var/lib/postgresql/data \
                                --restart unless-stopped \
                                postgres:latest'
                            
                            echo "Waiting for Postgres to become available..."
                            ssh -o StrictHostKeyChecking=no \$SSH_TARGET \
                            'retries=0; \
                            until docker exec my-postgres pg_isready -U '${DB_USERNAME}' >/dev/null 2>&1; do \
                                if [ \$retries -ge 15 ]; then \
                                    echo "❌ Postgres did not become ready in time."; \
                                    exit 1; \
                                fi; \
                                elapsed=\$(( retries * 2 )); \
                                echo "  - still waiting (\$((retries*2))s elapsed)..."; \
                                retries=\$((retries+1)); \
                                sleep 2; \
                            done; \
                            echo "✅ Postgres is up (took \$((retries*2))s)."'
                        ')
                    }
                }
            }
        }

        stage('Deploy Backend to VM') {
            agent { label 'worker-agents-02' }
            steps {
                withCredentials([
                    string(credentialsId: 'jwt-secret',         variable: 'JWT_SECRET'),
                    string(credentialsId: 'jwt-expiration',     variable: 'JWT_EXPIRATION'),
                    string(credentialsId: 'app-port',           variable: 'PORT'),
                    string(credentialsId: 'recaptcha-secret-key',variable: 'RECAPTCHA_SECRET_KEY'),
                    string(credentialsId: 'recaptcha-site-key',  variable: 'RECAPTCHA_SITE_KEY'),
                    string(credentialsId: 'cloud-name',          variable: 'CLOUD_NAME'),
                    string(credentialsId: 'api-key',             variable: 'API_KEY'),
                    string(credentialsId: 'api-secret',          variable: 'API_SECRET'),
                    string(credentialsId: 'google-client-id',    variable: 'GOOGLE_CLIENT_ID'),
                    string(credentialsId: 'google-client-secret',variable: 'GOOGLE_CLIENT_SECRET')
                ]) {
                    echo "Deploying backend container (${env.BACKEND_CONTAINER_NAME}) to ${env.TARGET_HOST_IP} on port ${env.BACKEND_HOST_PORT}"

                    sshagent (credentials: [env.SSH_CREDENTIAL_ID]) {
                        sh """
                            set -e
                            echo "Waiting a few seconds for the DB to fully start..."
                            sleep 15

                            SSH_TARGET="${env.SSH_USER_ON_TARGET}@${env.TARGET_HOST_IP}"

                            echo "Pulling new backend image ${env.BACKEND_IMAGE_NAME}:latest on \$SSH_TARGET..."
                            ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null \$SSH_TARGET \
                                "docker pull ${env.BACKEND_IMAGE_NAME}:latest"

                            echo "Stopping & removing old container ${env.BACKEND_CONTAINER_NAME} on \$SSH_TARGET if it exists..."
                            ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null \$SSH_TARGET \
                                "docker stop ${env.BACKEND_CONTAINER_NAME} >/dev/null 2>&1 || true; docker rm ${env.BACKEND_CONTAINER_NAME} >/dev/null 2>&1 || true"

                            echo "Running new backend container ${env.BACKEND_CONTAINER_NAME} on \$SSH_TARGET..."
                            ssh -o StrictHostKeyChecking=no -o UserKnownHostsFile=/dev/null \$SSH_TARGET \
                                "docker run -d --name ${env.BACKEND_CONTAINER_NAME} \
                                    --network primarket \
                                    -p ${env.BACKEND_HOST_PORT}:${env.PORT} \
                                    -e DB_HOST=${env.DB_HOST} \
                                    -e DB_PORT=${env.DB_PORT} \
                                    -e DB_NAME=${env.DB_NAME} \
                                    -e DB_USERNAME=${env.DB_USERNAME} \
                                    -e DB_PASSWORD=\"${env.DB_PASSWORD}\" \
                                    -e DB_URL='${env.DB_URL}' \
                                    -e JWT_SECRET='${env.JWT_SECRET}' \
                                    -e JWT_EXPIRATION='${env.JWT_EXPIRATION}' \
                                    -e RECAPTCHA_SECRET_KEY='${env.RECAPTCHA_SECRET_KEY}' \
                                    -e RECAPTCHA_SITE_KEY='${env.RECAPTCHA_SITE_KEY}' \
                                    -e CLOUD_NAME='${env.CLOUD_NAME}' \
                                    -e API_KEY='${env.API_KEY}' \
                                    -e API_SECRET='${env.API_SECRET}' \
                                    -e GOOGLE_CLIENT_ID='${env.GOOGLE_CLIENT_ID}' \
                                    -e GOOGLE_CLIENT_SECRET='${env.GOOGLE_CLIENT_SECRET}' \
                                    -e SERVER_PORT=${env.PORT} \
                                    --restart unless-stopped \
                                    ${env.BACKEND_IMAGE_NAME}:latest"

                            echo "Backend deployment commands sent to \$SSH_TARGET."
                        """
                    }
                }
            }
        }
    }

    post {
        success {
            echo "✅ Primarket Backend pipeline completed successfully!"
        }
        failure {
            echo "❌ Primarket Backend pipeline failed. Check console output for errors."
        }
        always {
            echo "Pipeline finished. Cleaning workspace..."
            cleanWs()
        }
    }
}
