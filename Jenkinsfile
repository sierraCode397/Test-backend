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

            // Fail the stage if it goes past 15 minutes
            options {
                timeout(time: 15, unit: 'MINUTES')
            }

            // Enable BuildKit caching
            environment {
                DOCKER_BUILDKIT = '1'
            }

            steps {
                echo "Building Docker image ${env.BACKEND_IMAGE_NAME}:${env.IMAGE_TAG}"
                echo "Using SPRINT_BOOT_PUBLIC_API_BASE_URL=${env.SPRINT_BOOT_PUBLIC_API_BASE_URL_FOR_BUILD}"

                script {
                // Let Jenkins re-try the entire build up to 3 times
                    retry(3) {
                        // BuildKit will cache ~/.m2 between builds if your Dockerfile uses --mount=type=cache
                        def img = docker.build(
                        "${env.BACKEND_IMAGE_NAME}:${env.IMAGE_TAG}",
                        "--build-arg SPRINT_BOOT_PUBLIC_API_BASE_URL=${env.SPRINT_BOOT_PUBLIC_API_BASE_URL_FOR_BUILD} -f Dockerfile ."
                        )
                    }
                }

                // Tag “latest” only once the build has succeeded
                sh """
                docker tag ${env.BACKEND_IMAGE_NAME}:${env.IMAGE_TAG} ${env.BACKEND_IMAGE_NAME}:latest
                echo "Tagged image as :latest"
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
                        sh '''#!/usr/bin/env bash
                            set -e

                            SSH_TARGET="${SSH_USER_ON_TARGET}@${TARGET_HOST_IP}"

                            echo "$SSH_TARGET Isaaac look at this"

                            ssh -o StrictHostKeyChecking=no $SSH_TARGET bash -lc "
                            docker inspect my-postgres >/dev/null 2>&1 || \
                            docker run -d --name my-postgres \
                                --network primarket \
                                -p 5432:5432 \
                                -e POSTGRES_DB='${DB_NAME}' \
                                -e POSTGRES_USER='${DB_USERNAME}' \
                                -e POSTGRES_PASSWORD='${DB_PASSWORD}' \
                                -v pgdata:/var/lib/postgresql/data \
                                --restart unless-stopped \
                                postgres:latest
                            "

                            echo "Waiting for Postgres to become available..."
                                sleep 15
                            echo "✅ Postgres is up (took \$((retries*2))s)."
                            
                        '''
                    }
                }
            }
        }

        stage('Ensure Redis on VM') {
            agent { label 'worker-agents-02' }
            steps {
                withCredentials([
                    string(credentialsId: 'redis-host', variable: 'REDIS_HOST'),
                    string(credentialsId: 'redis-port', variable: 'REDIS_PORT')
                ]) {
                    sshagent (credentials: [env.SSH_CREDENTIAL_ID]) {
                        sh '''#!/usr/bin/env bash
                        set -e

                        SSH_TARGET="${SSH_USER_ON_TARGET}@${TARGET_HOST_IP}"

                        echo "🔍 Checking for Redis container 'my-redis' on '${TARGET_HOST_IP}'..."

                        ssh -o StrictHostKeyChecking=no $SSH_TARGET \
                            "docker stop ${REDIS_HOST} >/dev/null 2>&1 || true; \
                            docker rm  ${REDIS_HOST} >/dev/null 2>&1 || true"

                        ssh -o StrictHostKeyChecking=no $SSH_TARGET bash -lc "
                        if ! docker inspect '${REDIS_HOST}' >/dev/null 2>&1; then
                            echo "📦 Redis not found. Creating '${REDIS_HOST}' on network primarket..."
                            docker run -d --name '${REDIS_HOST}' \
                            --network primarket \
                            -p '${REDIS_PORT}':6379 \
                            redis
                        else
                            echo "✅ Redis container already exists."
                        fi
                        "

                        echo "⏳ Waiting a few seconds for Redis to start..."
                        sleep 5
                        echo "✅ Redis should be up at '${REDIS_HOST}':'${REDIS_PORT}'"
                        '''
                    }
                }
            }
        }


        stage('Deploy Backend to VM') {
            agent { label 'worker-agents-02' }
            steps {
                withCredentials([
                    string(credentialsId: 'db-host',             variable: 'DB_HOST'),
                    string(credentialsId: 'db-port',             variable: 'DB_PORT'),
                    string(credentialsId: 'db-name',             variable: 'DB_NAME'),
                    string(credentialsId: 'jwt-secret',          variable: 'JWT_SECRET'),
                    string(credentialsId: 'jwt-expiration',      variable: 'JWT_EXPIRATION'),
                    string(credentialsId: 'app-port',            variable: 'PORT'),
                    string(credentialsId: 'recaptcha-secret-key',variable: 'RECAPTCHA_SECRET_KEY'),
                    string(credentialsId: 'recaptcha-site-key',  variable: 'RECAPTCHA_SITE_KEY'),
                    string(credentialsId: 'cloud-name',          variable: 'CLOUD_NAME'),
                    string(credentialsId: 'api-key',             variable: 'API_KEY'),
                    string(credentialsId: 'api-secret',          variable: 'API_SECRET'),
                    string(credentialsId: 'google-client-id',    variable: 'GOOGLE_CLIENT_ID'),
                    string(credentialsId: 'google-client-secret',variable: 'GOOGLE_CLIENT_SECRET'),
                    string(credentialsId: 'mail-host',           variable: 'MAIL_HOST'),
                    string(credentialsId: 'mail-port',           variable: 'MAIL_PORT'),
                    string(credentialsId: 'redis-host',          variable: 'REDIS_HOST'),
                    string(credentialsId: 'redis-port',          variable: 'REDIS_PORT'),
                    usernamePassword(credentialsId: 'mail-credentials', usernameVariable: 'MAIL_USERNAME', passwordVariable: 'MAIL_PASSWORD'),
                    usernamePassword(credentialsId: 'db-postgres', usernameVariable: 'DB_USERNAME', passwordVariable: 'DB_PASSWORD')
                ]) {
                    echo "Deploying backend container (${env.BACKEND_CONTAINER_NAME}) to ${env.TARGET_HOST_IP} on port ${env.BACKEND_HOST_PORT}"

                    script {
                        env.DB_URL = "jdbc:postgresql://${env.DB_HOST}:${env.DB_PORT}/${env.DB_NAME}"
                    }
                    
                    sshagent (credentials: [env.SSH_CREDENTIAL_ID]) {
                        sh '''
                        set -e
                        echo "Waiting a few seconds for the DB to fully start…"
                        sleep 15

                        SSH_TARGET="${SSH_USER_ON_TARGET}@${TARGET_HOST_IP}"

                        ssh -o StrictHostKeyChecking=no $SSH_TARGET \
                            "docker pull ${BACKEND_IMAGE_NAME}:latest"

                        ssh -o StrictHostKeyChecking=no $SSH_TARGET \
                            "docker stop ${BACKEND_CONTAINER_NAME} >/dev/null 2>&1 || true; \
                            docker rm  ${BACKEND_CONTAINER_NAME} >/dev/null 2>&1 || true"

                        ssh -o StrictHostKeyChecking=no $SSH_TARGET \
                            "docker run -d --name ${BACKEND_CONTAINER_NAME} \
                            --network primarket \
                            -p ${BACKEND_HOST_PORT}:${PORT} \
                            -e DB_HOST='${DB_HOST}' \
                            -e DB_PORT='${DB_PORT}' \
                            -e DB_NAME='${DB_NAME}' \
                            -e DB_USERNAME='${DB_USERNAME}' \
                            -e DB_PASSWORD='${DB_PASSWORD}' \
                            -e DB_URL='${DB_URL}' \
                            -e JWT_SECRET='${JWT_SECRET}' \
                            -e JWT_EXPIRATION='${JWT_EXPIRATION}' \
                            -e RECAPTCHA_SECRET_KEY='${RECAPTCHA_SECRET_KEY}' \
                            -e RECAPTCHA_SITE_KEY='${RECAPTCHA_SITE_KEY}' \
                            -e CLOUD_NAME='${CLOUD_NAME}' \
                            -e API_KEY='${API_KEY}' \
                            -e API_SECRET='${API_SECRET}' \
                            -e GOOGLE_CLIENT_ID='${GOOGLE_CLIENT_ID}' \
                            -e GOOGLE_CLIENT_SECRET='${GOOGLE_CLIENT_SECRET}' \
                            -e SERVER_PORT='${PORT}' \
                            -e MAIL_HOST='${MAIL_HOST}' \
                            -e MAIL_PORT='${MAIL_PORT}' \
                            -e REDIS_HOST='${REDIS_HOST}' \
                            -e REDIS_PORT='${REDIS_PORT}' \
                            -e MAIL_USERNAME='${MAIL_USERNAME}' \
                            -e MAIL_PASSWORD='${MAIL_PASSWORD}' \
                            --restart unless-stopped \
                            ${BACKEND_IMAGE_NAME}:latest"

                        echo "✅ Backend is starting on port ${BACKEND_HOST_PORT}"
                    '''
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
