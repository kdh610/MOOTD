# MOOTD 포팅 매뉴얼

## 1. 시스템 요구사항

### 1.1 모바일 하드웨어 요구사항
- 안드로이드 버전 13이상의 모바일기기

### 1.2 서버 하드웨어 요구사항
- (선택) VRAM 4GB 이상의 그래픽카드
### 1.3 소프트웨어 요구사항
- Java 17 이상
- Python 3.10 이상
- MongoDB 8.0 이상
- Docker 27.0 이상
- Docker Compose 3.0 이상
- AWS CLI 2.0 이상
- Android Studio (최신 버전)

## 2. 백엔드 서비스 설치
```bash
git clone https://lab.ssafy.com/s11-final/S11P31A105.git
cd S11P31A105
```
### 2.1 Mongo DB 실행
```bash
# 루트 디렉토리에서
cd elastic-mongo-monstache/config
vi config.toml
#elasticsearch-user=""
#elasticsearch-password=""
#두줄에 id,pw 입력

# 루트 디렉토리에서
cd elastic-mongo-monstache/mongodb
vi setup.sh
# username:password << 이 부분을 db의 id,pw로 입력
# !!! spring의 .env와 같아야함 !!!
vi replicaSet.js
#{_id:0,host : "ip:27017"},
#{_id:1,host : "ip:27018"} << 몽고 DB ip 입력
# elastic-mongo-monstache 디렉토리에서
docker-compose up -d # 백그라운드 실행
```


### 2.2 SpringBoot(게이트웨이 서버) 실행
```bash
cd backend
# env 파일을 작성합니다.(이미 있다면 생략)
vi .env

#수정 후
docker-compose up -d # 백그라운드 실행
```
```bash
#mongo db
DB_USERNAME={DB 사용자 이름} # setup.sh 와 동일해야함
DB_PASSWORD={DB 비밀번호} # setup.sh 와 동일해야함
HOST={DB 서버의 ip} # replicaSet.js 의 ip와 동일해야함
# aws s3
S3_BUCKET={S3 버켓이름}
S3_ACCESSKEY={S3 엑세스키}
S3_SECRETKEY={S3 비밀키}
KAFKA_SERVER={개발용 카프카 URL}
ES_USERNAME={ES id}
ES_PASSWORD={ES}
```


### 2.2 FastAPI 서비스 설정
각 서비스별 설치 및 설정 방법:

Masking Service
```bash
# Spring Gateway 프로젝트 실행(루트 디렉토리에서)
cd processing_server
# 프로젝트 빌드
docker-compose up -d # 백그라운드 실행
```
Image Similarity Service
```bash
# Spring Gateway 프로젝트 실행(루트 디렉토리에서)
cd img_similarity
# 프로젝트 빌드
docker-compose up -d # 백그라운드 실행
```
Image Edge Detecting Service
```bash
# Spring Gateway 프로젝트 실행(루트 디렉토리에서)
cd DexiNed
# 프로젝트 빌드
docker-compose up -d # 백그라운드 실행
```

## 3. 안드로이드 앱 설정

### 3.1 프로젝트 설정
```bash
# 루트 디렉토리에서
cd client/app
vi build.gradle.kts
#minSdk = 26
#최소 sdk 버전을 테스트하고자 하는 모바일 기기의 안드로이드 버전에 맞게 수정합니다. 가능하면 26 이상을 지원하는 기기를 추천합니다.

```

## 4. 네트워크 설정
```bash
#각 서비스에서 사용하는 포트를 엽니다. 
# 예시 : UFW 사용시
sudo ufw allow 8080/tcp  # Gateway
sudo ufw allow 27017/tcp # MongoDB
sudo ufw allow 8081/tcp  # Masking Service
sudo ufw allow 8082/tcp  # Similarity Service
sudo ufw allow 8083/tcp  # Edge Detection Service
```