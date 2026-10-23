
pipeline {
    agent any

    environment {
        SERVER_IP = credentials('maven-server-ip')
        APP_NAME = 'Financial-planner'
        REPO_URL = 'https://github.com/Sivachaithanya-Organisation/Financial-planner.git'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Verify Tools') {
            steps {
                sh '''
                    java -version
                    javac -version
                    git --version
                '''
            }
        }

        stage('Build and Deploy') {
            steps {
                sshagent(credentials: ['server-ssh-key']) {
                    sh '''
                        set -eu

                        ssh -o StrictHostKeyChecking=accept-new \
                            -o BatchMode=yes \
                            ubuntu@${SERVER_IP} "
                                set -e
                                cd /tmp
                                rm -rf ${APP_NAME}
                                git clone ${REPO_URL} ${APP_NAME}
                                cd ${APP_NAME}
                                mvn clean package
                                test -f target/finplan.war
                                sudo cp target/finplan.war /opt/tomcat/webapps/
                                echo 'Deployment completed successfully'
                            "
                    '''
                }
            }
        }
    }

    post {
        success {
            echo 'SUCCESS: Application deployed.'
        }
        failure {
            echo 'FAILED: Check Console Output for the error.'
        }
    }
}
