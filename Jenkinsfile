pipeline {
    agent {
        kubernetes {
            yaml '''
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: maven
    image: maven:3.9.6-eclipse-temurin-21
    command: ["sleep"]
    args: ["99d"]
    volumeMounts:
    - name: maven-cache
      mountPath: /root/.m2
  - name: docker
    image: docker:24.0.7
    command: ["sleep"]
    args: ["99d"]
    volumeMounts:
    - name: docker-socket
      mountPath: /var/run/docker.sock
  - name: git-kustomize
    image: line/kubectl-kustomize:latest
    command: ["sleep"]
    args: ["99d"]
  - name: trivy
    image: 187104821419.dkr.ecr.ap-southeast-1.amazonaws.com/devops-tools/trivy:0.49.1
    command: ["sleep"]
    args: ["99d"]
    volumeMounts:
    - name: docker-socket
      mountPath: /var/run/docker.sock
  volumes:
  - name: docker-socket
    hostPath:
      path: /var/run/docker.sock
  - name: maven-cache
    persistentVolumeClaim:
      claimName: maven-cache-pvc # Tối ưu cache Maven
'''
        }
    }

    environment {
        // AWS & Image Config
        AWS_REGION      = "ap-southeast-1"
//        AWS_ACCOUNT     = "187104821419"
//        ECR_REPO        = "super-app"
//        IMAGE_TAG       = "build-${env.BUILD_NUMBER}"
//        FULL_IMAGE_URL  = "${AWS_ACCOUNT}.dkr.ecr.${AWS_REGION}.amazonaws.com/${ECR_REPO}:${IMAGE_TAG}"
//
//        // GitOps Config
//        MANIFEST_REPO   = "github.com/phongnd/super-app-manifests.git"
    }

    stages {
    stage('Step 0: Fetch AWS Parameters') {
                steps {
                    container('aws-cli') {
                        script {
                            echo "--- 🛡️ Fetching configurations from AWS SSM Parameter Store ---"
                            // Fetch Account ID
                            env.AWS_ACCOUNT = sh(script: "aws ssm get-parameter --name '/cicd/super-app/aws-account' --query 'Parameter.Value' --output text --region ${AWS_REGION}", returnStdout: true).trim()

                            // Fetch Artifact Domain
                            env.DOMAIN_ARTIFACT = sh(script: "aws ssm get-parameter --name '/cicd/super-app/artifact-domain' --query 'Parameter.Value' --output text --region ${AWS_REGION}", returnStdout: true).trim()

                            // Fetch Sonar Token (Dạng SecureString)
//                            env.SONAR_TOKEN = sh(script: "aws ssm get-parameter --name '/cicd/super-app/sonar-token' --with-decryption --query 'Parameter.Value' --output text --region ${AWS_REGION}", returnStdout: true).trim()

                            // Thiết lập Image URL động
                            env.ECR_REPO = "task-management"
                            env.IMAGE_TAG = "build-${env.BUILD_NUMBER}"
                            env.FULL_IMAGE_URL = "${env.AWS_ACCOUNT}.dkr.ecr.${AWS_REGION}.amazonaws.com/${env.ECR_REPO}:${env.IMAGE_TAG}"
                            // Lấy GitHub User và Token từ SSM (Token nên là SecureString)
                            env.GIT_USER  = sh(script: "aws ssm get-parameter --name '/cicd/github/user' --query 'Parameter.Value' --output text --region ${AWS_REGION}", returnStdout: true).trim()
                            env.GIT_TOKEN = sh(script: "aws ssm get-parameter --name '/cicd/github/token' --with-decryption --query 'Parameter.Value' --output text --region ${AWS_REGION}", returnStdout: true).trim()

                            // Fetch Manifest Repo URL (Ví dụ: github.com/phongnd/super-app-manifests.git)
                            env.MANIFEST_REPO = sh(script: "aws ssm get-parameter --name '/cicd/super-app/manifest-repo' --query 'Parameter.Value' --output text --region ${AWS_REGION}", returnStdout: true).trim()
                            // Mask biến nhạy cảm để không bị lộ trong log Jenkins
                            sh "set +x"
                        }
                    }
                }
            }
        stage('Determine Environment') {
            steps {
                script {
                    // Logic xác định môi trường đích dựa trên nhánh hiện tại
                    if (env.BRANCH_NAME == 'dev') {
                        env.DEPLOY_ENV = 'dev'
                    } else if (env.BRANCH_NAME == 'uat') {
                        env.DEPLOY_ENV = 'uat'
                    } else if (env.BRANCH_NAME == 'stg') {
                        env.DEPLOY_ENV = 'stg'
                    } else if (env.BRANCH_NAME == 'main') {
                        env.DEPLOY_ENV = 'prod'
                    } else {
                        error("Nhánh ${env.BRANCH_NAME} không được hỗ trợ deploy.")
                    }
                    echo "Môi trường đích: ${env.DEPLOY_ENV}"
                }
            }
        }

        stage('Step 1: Build & Deploy CodeArtifact') {
            steps {
                container('aws-cli') {
                    script {
                        env.CODEARTIFACT_AUTH_TOKEN = sh(script: "aws codeartifact get-authorization-token --domain ${env.DOMAIN_ARTIFACT} --domain-owner ${env.AWS_ACCOUNT} --query authorizationToken --output text --region ${AWS_REGION}", returnStdout: true).trim()
                    }
                }
                container('maven') {
                    sh """
                        echo '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0">
                            <servers>
                                <server>
                                    <id>codeartifact</id>
                                    <username>aws</username>
                                    <password>${env.CODEARTIFACT_AUTH_TOKEN}</password>
                                </server>
                            </servers>
                        </settings>' > settings.xml
                        mvn -s settings.xml clean deploy -DskipTests
                    """
                }
            }
        }


        stage('Stage 2: Dockerize & Trivy Scan') {
            steps {
                container('docker') {
                    script {
                        // Build Image
                        sh "docker build -t ${env.FULL_IMAGE_URL} ."
                    }
                }
                container('trivy') {
                    script {
                        // Quét lỗ hổng bảo mật mức CRITICAL & HIGH. Fail build nếu phát hiện.
                        sh """
                            trivy image --severity HIGH,CRITICAL --exit-code 1 --no-progress ${env.FULL_IMAGE_URL}
                        """
                    }
                }
                container('docker') {
                    script {
                        // Nếu quét Trivy qua (exit code 0), thực hiện Push lên ECR
                        // Lưu ý: Worker node đã gán IAM Role cho ECR
                        sh """
                            aws ecr get-login-password --region ${AWS_REGION} | docker login --username AWS --password-stdin ${env.AWS_ACCOUNT}.dkr.ecr.${AWS_REGION}.amazonaws.com
                            docker push ${env.FULL_IMAGE_URL}
                        """
                    }
                }
            }
        }

        stage('Stage 3: Update Manifest (GitOps Step)') {
            steps {
                container('git-kustomize') {
                    script {
                        sh """
                            set +x # Tắt log để giấu Token
                            # Dùng trực tiếp biến env.GIT_USER và env.GIT_TOKEN đã lấy từ Step 0
                            git clone https://${env.GIT_USER}:${env.GIT_TOKEN}@${env.MANIFEST_REPO}

                            cd task-management-k8s-manifests/overlays/${env.DEPLOY_ENV}
                            kustomize edit set image task-management=${env.FULL_IMAGE_URL}

                            git config user.email "jenkins-bot@phongnd.uk"
                            git config user.name "Jenkins GitOps Bot"
                            git commit -am "🚀 [CI] Update image ${env.DEPLOY_ENV} to ${env.IMAGE_TAG}"
                            git push origin main
                            set -x
                        """
                    }
                }
            }
        }
    }

}