# 📸 MOOTD

## 📌 프로젝트 소개

**MOOTD**은 사용자가 사진 촬영 시 보다 나은 구도를 잡을 수 있도록 인물 및 배경에 대한 가이드라인을 제공하는 어플리케이션입니다.

사용자는 간편한 설정과 검색을 통해 원하는 가이드라인을 활용하여 원하는 구도로 사진을 찍을 수 있습니다.


## 💻 프로젝트 주요 기능

### 📸 가이드라인 제공

사용자가 원하는 구도로 사진을 찍을 수 있도록 가이드라인을 제공합니다.


- 다양한 가이드라인 제공:
  - 원본 이미지, 인물 윤곽선, 배경 윤곽선을 카메라 화면에 오버레이 형태로 표시.
  - 사용자 설정을 통해 가이드라인의 투명도를 조절 가능.
- 사용자 갤러리 활용:
  - 갤러리에 저장된 사진을 기반으로 가이드라인 생성.


|    원본 이미지 가이드라인    |      인물 가이드라인     | 배경 가이드라인 |
| :-------------------: | :-----------------: | :-------------: |
| <img width="250" src="./asset/origin_guideline.jpg"> | <img width="250" src="./asset/person_guideline.jpg"> | <img width="250" src="./asset/background_guideline.png"> |


| 촬영하기 |
| :---: |
| <img width="250" src="./asset/take_photo.gif"> |

### 🔍 가이드라인 검색 

다른 사용자가 찍은 사진을 조회하여 해당 가이드라인을 사용할 수 있습니다. 사진은 초상권 보호를 위해 인물을 마스킹하여 제공합니다.

- 현재 위치 기반 이미지 검색
  - 특정 위치에서 다른 사용자가 찍은 사진을 조회.
  - 가독성을 높이기 위해 동일한 위치나 가까운 위치에 있는 사진들을 하나의 클러스터로 묶어 직관적으로 탐색 가능.
- 텍스트 기반 이미지 검색
  - 텍스트 키워드로 검색하여 다른 사용자가 찍은 사진을 조회.
  - 사용자가 입력한 이전 검색어를 기록하여 편리한 검색 환경 제공.

|       위치 기반 검색        |        텍스트 기반 검색           |
| :----------------------: | :----------------------: |
| <img width="250" src="./asset/map.png"> | <img width="250" src="./asset/search.png"> |


### 📂 가이드라인 관리

- 최근 사용 가이드라인 조회
  - 사용자가 최근 사용한 가이드라인을 쉽게 다시 적용 가능.
- 사용자 생성 가이드라인 저장:
  - 사용자가 생성한 가이드라인을 저장 및 관리.
  - 서버가 아닌 사용자의 기기에 저장하여 인터넷 없이 사용 가능.



## 🤖 사용된 AI 모델

### DexiNed
- 기능: 인물 및 배경 윤곽선 추출.
- 특징: Dense 블록과 Side-output을 활용한 정교한 엣지 검출.

<img src="./asset/dexined.png"> 

### Mask-RCNN
- 기능: 초상권 보호를 위한 인물 마스킹.
- 특징: 이미지 내 객체를 픽셀 단위로 분류 및 탐지.

<img src="./asset/mask_rcnn.png"> 
  

### NLP 기반 Image Captioning
- 기능: 이미지 태그 생성 및 텍스트 키워드 추출.
- 특징: Transformer 인코더-디코더 구조로 이미지 분석 및 캡션 생성.
- Meta CLIP: 이미지-텍스트 간 유사도 계산.


<img src="./asset/nlp.png"> 
<img src="./asset/meta_clip.png"> 




## 🧑🏻 팀원

### 🖥️ Client

| 김주연 | 정승훈 |
| :---: | :---: | 
| <a href="https://github.com/izodam"><img src="https://avatars.githubusercontent.com/izodam" width=160/></a> | <a href="https://github.com/Jeongseunghun"><img src="https://avatars.githubusercontent.com/Jeongseunghun" width=160/></a> | 
| [izodam](https://github.com/izodam) | [Jeongseunghun](https://github.com/Jeongseunghun)  |

### 🖥️ Server

| 김도훈 | 최재혁 | 
| :---: | :---: |
| <a href="https://github.com/kdh610"><img src="https://avatars.githubusercontent.com/kdh610" width=160/></a> | <a href="https://github.com/hoiae"><img src="https://avatars.githubusercontent.com/hoiae" width=160/></a> |
|[kdh610](https://github.com/kdh610) | [hoiae](https://github.com/hoiae) 
### 🖥️ AI

| 박정영 | 박경령 | 정승훈 |
| :---: | :---: | :---: |
| <a href="https://github.com/WiFros"><img src="https://avatars.githubusercontent.com/WiFros" width=160/></a> | <a href="https://github.com/Kryoung1215"><img src="https://avatars.githubusercontent.com/Kryoung1215" width=160/></a> | <a href="https://github.com/Jeongseunghun"><img src="https://avatars.githubusercontent.com/Jeongseunghun" width=160/></a> | 
|[WiFros](https://github.com/WiFros) | [Kryoung1215](https://github.com/Kryoung1215) |  [Jeongseunghun](https://github.com/Jeongseunghun) |

### 🖥️ Infra

| 김도훈 | 최재혁 | 박정영 |
| :---: | :---: | :---: |
| <a href="https://github.com/kdh610"><img src="https://avatars.githubusercontent.com/kdh610" width=160/></a> | <a href="https://github.com/hoiae"><img src="https://avatars.githubusercontent.com/hoiae" width=160/></a> | <a href="https://github.com/WiFros"><img src="https://avatars.githubusercontent.com/WiFros" width=160/></a> |
|[kdh610](https://github.com/kdh610) | [hoiae](https://github.com/hoiae) |[WiFros](https://github.com/WiFros) |

## ⚒️ 기술 스택

### 🖥️ Client

|  |  |
| :----- | :----- |
| Language             | <img src="https://img.shields.io/badge/kotlin-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white"/>                                                                                                        |
| Version Control      | <img src="https://img.shields.io/badge/git-F05032?style=for-the-badge&logo=git&logoColor=white"/> <img src="https://img.shields.io/badge/gitLAB-fc6d26?style=for-the-badge&logo=gitlab&logoColor=white"/>              |
| IDE                  | <img src="https://img.shields.io/badge/android%20studio-3DDC84.svg?style=for-the-badge&logo=androidstudio&logoColor=white"/>                                                                                  |

### 🖥️ Server

|  |  |
| :-------------------: | :-------------- |
| Framework             | <img src="https://img.shields.io/badge/SpringBoot-6DB33F?style=for-the-badge&logo=SpringBoot&logoColor=white"/> <img src="https://img.shields.io/badge/fastapi-009688?style=for-the-badge&logo=fastapi&logoColor=white"/> |
| Language | <img src="https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white"/> <img src="https://img.shields.io/badge/python-3776AB.svg?style=for-the-badge&logo=python&logoColor=white"/> |
| Database              | <img src="https://img.shields.io/badge/mongodb-003545?style=for-the-badge&logo=mongodb&logoColor=white" alt="MongoDB"/> <img src="https://img.shields.io/badge/Elasticsearch-005571?style=for-the-badge&logo=Elasticsearch&logoColor=white">  |
|Monitoring | <img src="https://img.shields.io/badge/prometheus-E6522C?style=for-the-badge&logo=prometheus&logoColor=white" alt="prometheus"/> <img src="https://img.shields.io/badge/grafana-F46800?style=for-the-badge&logo=grafana&logoColor=white" alt="grafana"/>|
| Cloud                 | <img src="https://img.shields.io/badge/Amazon%20EC2-FF9900?style=for-the-badge&logo=Amazon%20EC2&logoColor=white"> <img src="https://img.shields.io/badge/Amazon%20S3-569A31?style=for-the-badge&logo=Amazon%20S3&logoColor=white"> |
| DevOps                | <img src="https://img.shields.io/badge/Docker-2496ED?style=for-the-badge&logo=Docker&logoColor=white" alt="Docker"> <img src="https://img.shields.io/badge/jenkins-D24939?style=for-the-badge&logo=jenkins&logoColor=white" alt="jenkins"> |  
| Version Control       | <img src="https://img.shields.io/badge/git-F05032?style=for-the-badge&logo=git&logoColor=white"/> <img src="https://img.shields.io/badge/gitLAB-fc6d26?style=for-the-badge&logo=gitlab&logoColor=white"/> |
| IDE                   | <img src="https://img.shields.io/badge/IntelliJIDEA-000000.svg?style=for-the-badge&logo=intellij-idea&logoColor=white"/> |

### 🖥️ Common

|  |  |
| :--- | :--- |
| Collaboration | <img src="https://img.shields.io/badge/jira-0052CC?style=for-the-badge&logo=jira&logoColor=white" alt="Notion"/> <img src="https://img.shields.io/badge/notion-000000?style=for-the-badge&logo=notion&logoColor=white" alt="Notion"/> <img src="https://img.shields.io/badge/swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=white" alt="Swagger"/> <img src="https://img.shields.io/badge/figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white" alt="Figma"/> |

## 📚 산출물
|  |  |
| :--: | ---: |
| Architecture | <img width="700" src="./asset/architecture.png"> |
