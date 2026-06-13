pipeline {
  agent any
  stages {
    stage('Backend Tests') {
      steps {
        dir('Scout5') {
          sh 'chmod +x ./mvnw'
          sh './mvnw test'
        }
      }
    }

    stage('Project Configuration Validation') {
      steps {
        sh 'test -f docker-compose.yml'
        sh 'test -f .env.example'
        sh 'test -f Scout5/pom.xml'
        sh 'test -f Scout-front/package.json'
      }
    }

    stage('Build Frontend') {
      steps {
        dir('Scout-front') {
          sh 'npm ci --include=dev'
          sh 'npm run build'
        }
      }
    }

    stage('Build Frontend') {
      steps {
        dir('Scout-front') {
          sh 'npm run build'
        }
      }
    }
  }
}
