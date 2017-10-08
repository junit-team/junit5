pipeline {
  agent { label 'hi-speed' }
  tools {
    jdk 'Oracle JDK 9'
  }
  options {
    ansiColor('xterm')
  }
  stages {
    stage('Build') {
      steps {
        sh './gradlew --no-daemon clean build'
      }
      post {
        always {
          junit '**/build/test-results/**/*.xml'
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
      tools {
        jdk 'Oracle JDK 8 (latest)' // Use JDK 8 because of custom stylesheet
      }
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
      tools {
        jdk 'Oracle JDK 8 (latest)' // Use JDK 8 because AsciiDoctor has problems on JDK 9
      }
      steps {
        sh './gradlew --no-daemon asciidoctor'
      }
    }
    stage('Update Website') {
      when {
        branch 'master'
      }
      steps {
        milestone 2
        withCredentials([string(credentialsId: '9f982a37-747d-42bd-abf9-643534f579bd', variable: 'GRGIT_USER')]) {
          sh './gradlew --no-daemon gitPublishPush'
        }
      }
    }
    stage('Coverage') {
      steps {
        withCredentials([file(credentialsId: 'CLOVER_LICENSE', variable: 'ORG_GRADLE_PROJECT_clover.license.path')]) {
          sh './gradlew --no-daemon --refresh-dependencies -PenableClover clean cloverHtmlReport cloverXmlReport'
        }
      }
      post {
        success {
          step([
            $class: 'CloverPublisher',
            cloverReportDir: 'build/reports/clover',
            cloverReportFileName: 'clover.xml'
          ])
        }
      }
    }
  }
}
