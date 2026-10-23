pipeline {
    agent any

    environment {
        SERVER_IP = credentials('maven-server-ip')
        APP_NAME = "Financial-planner"
    }

    stages {

        stage('checkout'){
            steps{
                checkout scm
            }
        }

        stage('Verify Tools') {
            steps {
                sh 'javac -version'
            }
        }

        stage('maven build and deploy'){
            steps {
                sshagent(credentials: ['server-ssh-key']) {
                    sh '''
                        ssh ubuntu@${SERVER_IP} "
                            rm -rf ${APP_NAME}
                            git clone https://github.com/Sivachaithanya-Organisation/Financial-planner.git
                            cd ${APP_NAME}
                            mvn clean package
                            sudo cp target/finplan.war /opt/tomcat/webapps/
                        "
                    '''
                }
            }
        }
    }
}
