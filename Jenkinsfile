// Pipeline CI de admin-proyectos-front (Ejercicio5Cliente)
// Requiere en Jenkins: herramienta JDK "jdk-21", herramienta Maven "maven-3.9",
// servidor SonarQube llamado "SonarQube" y Semgrep instalado en el agente.
pipeline {
    agent any

    tools {
        jdk 'jdk-21'
        maven 'maven-3.9'
    }

    options {
        timestamps()
        buildDiscarder(logRotator(numToKeepStr: '10'))
        disableConcurrentBuilds()
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Compilar y probar (JaCoCo)') {
            steps {
                sh 'java -version'
                sh 'mvn -version'
                sh 'mvn -B clean verify'
            }
            post {
                always {
                    junit allowEmptyResults: true, testResults: 'target/surefire-reports/*.xml'
                    recordCoverage(tools: [[parser: 'JACOCO', pattern: 'target/site/jacoco/jacoco.xml']],
                                   sourceDirectories: [[path: 'src/main/java']])
                    archiveArtifacts allowEmptyArchive: true, artifacts: 'target/site/jacoco/**'
                }
            }
        }

        stage('Analisis SonarQube') {
            steps {
                withSonarQubeEnv('SonarQube') {
                    sh 'mvn -B org.sonarsource.scanner.maven:sonar-maven-plugin:sonar'
                }
            }
        }

        stage('Quality Gate') {
            steps {
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: false
                }
            }
        }

        stage('Analisis Semgrep (OWASP Top 10)') {
            steps {
                sh '''
                    semgrep --version
                    semgrep scan --config p/owasp-top-ten --metrics=off --exclude target \
                        --json-output=semgrep-report.json --sarif-output=semgrep-report.sarif .
                '''
            }
            post {
                always {
                    archiveArtifacts allowEmptyArchive: true, artifacts: 'semgrep-report.*'
                }
            }
        }
    }
}
