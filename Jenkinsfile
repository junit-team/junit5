pipeline {
    agent any

    environment {
        SIGRID_CI_ACCOUNT = credentials('SIGRID_CI_ACCOUNT')
        SIGRID_CI_TOKEN = credentials('SIGRID_CI_TOKEN')
    }

    stages {
        stage('build') {
            steps {
                sh 'if cd repo; then git pull; else git clone https://github.com/Software-Improvement-Group/sigridci.git sigridci; fi'
                sh './sigridci/sigridci/sigridci.py --customer opensource --system junit --source .'
            }
        }
    }
}
