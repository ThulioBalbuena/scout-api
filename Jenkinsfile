pipeline {
  agent any
  stages {
    stage('Build Frontend') {
      steps {
        dir('Scout-front') {
          sh 'npm ci'
          sh 'npm run build'
        }
      }
    }
    stage('Build backend') {
      steps {
        dir('Scout5') {
          sh './mvnw package -DskipTests'
        }
      }
    }
    stage('Backend teste') {
      steps {
        dir('Scout5') {
          sh 'mvn test'
        }
      }
    }

  }
}
