pipeline {
    agent {
        kubernetes {
            yaml '''
apiVersion: v1
kind: Pod
spec:
  imagePullSecrets:
   - name: ecr-registry-secret
  containers:
  - name: maven
    image: maven:3.9.6-eclipse-temurin-21
    command: ["sleep"]
    args: ["99d"]
    volumeMounts:
    - name: maven-cache
      mountPath: /root/.m2
  - name: kaniko
    image: gcr.io/kaniko-project/executor:debug
    command: ["sleep"]
    args: ["99d"]
    volumeMounts:
    - name: kaniko-config
      mountPath: /kaniko/.docker/
  - name: aws-cli
    image: amazon/aws-cli:latest
    command: ["sleep"]
    args: ["99d"]
  - name: git-kustomize
    image: alpine/k8s:1.30.1
    command: ["sleep"]
    args: ["99d"]
  - name: trivy
    image: 187104821419.dkr.ecr.ap-southeast-1.amazonaws.com/devops-tools/trivy:0.49.1
    command: ["sleep"]
    args: ["99d"]
  volumes:
  - name: kaniko-config
    projected:
      sources:
      - secret:
          name: ecr-registry-secret
          items:
            - key: .dockerconfigjson
              path: config.json
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
                        try {
                            echo "--- 🛡️ Fetching configurations from AWS SSM Parameter Store ---"
                            // Fetch Account ID
                            env.AWS_ACCOUNT = sh(script: "aws ssm get-parameter --name '/cicd/super-app/aws-account' --query 'Parameter.Value' --output text --region ${AWS_REGION}", returnStdout: true).trim()
                            echo "${env.AWS_ACCOUNT}"
                            // Fetch Artifact Domain
                            env.DOMAIN_ARTIFACT = sh(script: "aws ssm get-parameter --name '/cicd/super-app/artifact-domain' --query 'Parameter.Value' --output text --region ${AWS_REGION}", returnStdout: true).trim()
                            echo "env.DOMAIN_ARTIFACT"
                            // Fetch Sonar Token (Dạng SecureString)
//                            env.SONAR_TOKEN = sh(script: "aws ssm get-parameter --name '/cicd/super-app/sonar-token' --with-decryption --query 'Parameter.Value' --output text --region ${AWS_REGION}", returnStdout: true).trim()

                            // Thiết lập Image URL động
                            env.ECR_REPO = "task-management"
                            env.IMAGE_TAG = "build-${env.BUILD_NUMBER}"
                            env.FULL_IMAGE_URL = "${env.AWS_ACCOUNT}.dkr.ecr.${AWS_REGION}.amazonaws.com/${env.ECR_REPO}:${env.IMAGE_TAG}"
                            // Lấy GitHub User và Token từ SSM (Token nên là SecureString)
                            env.GIT_USER  = sh(script: "aws ssm get-parameter --name '/cicd/github/user' --query 'Parameter.Value' --output text --region ${AWS_REGION}", returnStdout: true).trim()
                            echo "env.GIT_USER"
                            env.GIT_TOKEN = sh(script: "aws ssm get-parameter --name '/cicd/github/token' --with-decryption --query 'Parameter.Value' --output text --region ${AWS_REGION}", returnStdout: true).trim()
                            echo "env.GIT_TOKEN"
                            // Fetch Manifest Repo URL (Ví dụ: github.com/phongnd/super-app-manifests.git)
                            env.MANIFEST_REPO = sh(script: "aws ssm get-parameter --name '/cicd/super-app/manifest-repo' --query 'Parameter.Value' --output text --region ${AWS_REGION}", returnStdout: true).trim()
                            echo "env.MANIFEST_REPO"
                            // Mask biến nhạy cảm để không bị lộ trong log Jenkins
                            sh "set +x"
                             } catch (Exception e) {
                                // Nếu lỗi, nó sẽ in ra Log cho bạn thấy
                                echo "❌ LỖI RỒI CỘNG SỰ: ${e.getMessage()}"
                                // Chạy thử lệnh CLI trực tiếp để xem thông báo lỗi từ AWS
                                sh "aws ssm get-parameter --name '/cicd/super-app/aws-account' --region ${AWS_REGION}"
                                error("Dừng build do không lấy được tham số từ AWS.")
                             }
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
                // 1. Build & Push Image bằng Kaniko (Không cần Docker daemon)
                container('kaniko') {
                    script {
                        echo "--- 🚀 Building Image with Kaniko ---"
                        sh "/kaniko/executor --context `pwd` --dockerfile Dockerfile --destination ${env.FULL_IMAGE_URL}"
                    }
                }

                // 2. Quét bảo mật (Lấy Token từ container aws-cli rồi truyền sang trivy)
//                script {
//                    def ecrPassword = ""
//
//                    // Bước A: Nhảy vào container aws-cli để lấy password
//                    container('aws-cli') {
//                        ecrPassword = sh(script: "aws ecr get-login-password --region ${env.AWS_REGION}", returnStdout: true).trim()
//                    }
//
//                    // Bước B: Nhảy vào container trivy để quét, dùng password vừa lấy được
//                    container('trivy') {
//                        echo "--- 🛡️ Scanning Image from ECR ---"
//                        // Dùng dấu nháy kép "" để Groovy truyền biến ecrPassword vào shell
//                        sh """
//                            export TRIVY_USERNAME=AWS
//                            export TRIVY_PASSWORD=${ecrPassword}
//
//                            trivy image --severity HIGH,CRITICAL --exit-code 1 --no-progress ${env.FULL_IMAGE_URL}
//                        """
//                    }
//                }
            }
        }

        stage('Stage 3: Update Manifest (GitOps Step)') {
            steps {
                container('git-kustomize') {
                    script {
                        sh """
                            set +x
                            # Xóa thư mục cũ nếu có để tránh lỗi clone
                            rm -rf super-app-k8s-manifests

                            # Clone repo manifest
                            git clone https://${env.GIT_USER}:${env.GIT_TOKEN}@github.com/${env.GIT_USER}/${env.MANIFEST_REPO}.git
                            # Truy cập vào thư mục overlay của môi trường tương ứng
                            cd super-app-k8s-manifests/k8s/overlays/${env.DEPLOY_ENV}

                            # Cập nhật Image Tag mới vào file kustomization.yaml
                            kustomize edit set image task-management=${env.FULL_IMAGE_URL}

                            # Config danh tính để Commit
                            git config --global user.email "jenkins-bot@phongnd.uk"
                            git config --global user.name "Jenkins GitOps Bot"

                            # Commit và Push
                            git add .
                            git commit -m "🚀 [CI] Update image ${env.DEPLOY_ENV} to ${env.IMAGE_TAG}"
                            git push origin main
                            # 5. Dọn dẹp sau khi xong để tiết kiệm tài nguyên Node
                            cd ../../../../
                            rm -rf super-app-k8s-manifests
                            set -x
                        """
                    }
                }
            }
        }
    }

}