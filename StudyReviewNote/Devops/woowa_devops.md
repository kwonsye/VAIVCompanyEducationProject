# 우아한 형제들 IAC(Infra As Code)

> from 2020 우아콘 "나만 알고 싶은 우아한형제들의 인프라 스토리"

- 문제
    - 환경에 따라, 요청에 따라 수십개의 도메인 서비스 인프라가 각기 달리 구성되다보니 일관성 없고
    - 확장,변경의 어려움 발생

- 솔루션
    - 클라우드 **인프라의 표준화**
    - 운영편의를 위한 **시스템 자동화**
    - ==> **Infra As Code**
        - 운영과 표준화된 인프라 구성을 code로 구현하자

- IAC 의 도입 이유
    - **인프라 환경의 약속된 표준화**
    - **표준화된 인프라를 코드 기반으로 구성 및 업데이트**할 수 있다.
    - 인프라환경은 **어느누가 작업해도 일관성이 유지**될 수 있다.
    - 인프라 변경에 대한 추적(형상관리)도 되면 좋고

- 그래서 `Terraform` 도입
    - 표준화된 인프라 구성을 한번의 실행으로 여러 인프라에 동시에 쉽게 적용할 수 있게됨

- 인프라 표준화를 통해 채택한 운영 flow
    - 깃랩에 테라폼 + 코드 deploy 오픈소스 Atlantis + Jira 와 연동해서 프로젝트 형상관리 
        - Atlantis?
            - 인프라 관리를 Github PR에서 할 수 있게하는 툴

    - 인프라 생성 후 status 파일은 S3 에 저장
    - 권한 있는 사용자만 code 로 인프라를 update 할 수 있게 구현

    - Event 발생 시 제어/차단 System Automation
        - 클라우드에 문제나 변경, 미사용 리소스 발견, 리소스 limit , root 계정 접근자 정보 등 Event 발생 -> aws cloud watch -> aws lambda -> api -> 관련자에게 jira, slack 실시간 noti


<br>


## 핵심은 "표준화된 인프라를 누구나 쉽게 구성할 수 있게 코드로 관리하자"

<br>

## Reference
- [우아콘2020_나만 알고 싶은 우아한형제들의 인프라 스토리](https://www.youtube.com/watch?v=zXLabtLOJ3o&feature=emb_logo)
- https://devops-art-factory.gitbook.io/devops-workshop/terraform/atlantis

