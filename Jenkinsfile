pipeline {
  agent any
  stages {
    stage('Backend Tests') {
      steps {
        dir('Scout5') {
          sh './mvnw test'
        }
      }
    }

    stage('Docker Compose Validation') {
      steps {
        sh 'docker compose --env-file .env.example config'
      }
    }

    stage('Build Backend') {
      steps {
        dir('Scout5') {
          sh './mvnw package -DskipTests'
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
