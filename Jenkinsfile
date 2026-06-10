pipeline {
  agent any

  stages {
    stage('Backend teste') {
      steps {
        dir('Scout5') {
          sh 'mvn test'
        }
      }
    }
  }
}
