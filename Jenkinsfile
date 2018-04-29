pipeline {
  agent { label 'hi-speed' }
  tools {
    jdk 'Oracle JDK 10'
  }
  options {
    buildDiscarder(logRotator(numToKeepStr: '10'))
  }
  stages {
    stage('Build') {
      steps {
        sh './gradlew --no-daemon -PenableJaCoCo clean build jacocoRootReport'
      }
      post {
        always {
          junit '**/build/test-results/**/*.xml'
          jacoco execPattern: 'build/jacoco/*.exec', classPattern: 'build/jacoco/classes'
          archiveArtifacts artifacts: 'build/reports/jacoco/jacocoRootReport/html/**'
        }
      }
    }
    stage('Publish Artifacts') {
      when {
        branch 'master'
      }
      steps {
        milestone 1
        withCredentials([usernamePassword(credentialsId: '50481102-b416-45bd-8628-bd890c4f0188', usernameVariable: 'ORG_GRADLE_PROJECT_ossrhUsername', passwordVariable: 'ORG_GRADLE_PROJECT_ossrhPassword')]) {
          sh './gradlew --no-daemon uploadArchives -x test'
        }
      }
    }
    stage('Aggregate Javadoc') {
      steps {
        sh './gradlew --no-daemon aggregateJavadocs'
      }
      post {
        success {
          step([
            $class: 'JavadocArchiver',
            javadocDir: 'build/docs/javadoc',
            keepAll: true
          ])
        }
      }
    }
    stage('Generate User Guide') {
      steps {
        sh './gradlew --no-daemon --stacktrace asciidoctor'
      }
    }
    stage('Update Website') {
      when {
        branch 'master'
      }
      steps {
        milestone 2
        withCredentials([string(credentialsId: '9f982a37-747d-42bd-abf9-643534f579bd', variable: 'GRGIT_USER')]) {
          sh './gradlew --no-daemon --stacktrace gitPublishPush'
        }
      }
    }
  }
}
