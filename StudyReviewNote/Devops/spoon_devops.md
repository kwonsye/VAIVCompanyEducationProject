# 스푼라디오 IAC(Infra As Code)

> from [테라폼으로 글로벌 서비스 구성하기 - DRY 체험기](https://www.slideshare.net/sanggi/configuring-global-infrastructure-in-terraform-173174690)

- 문제
    - 동일한 인프라를 프로비저닝할때
    - WET (Write Everything Twice) 중복된 코드가 너무 많다

- 지향점
    - 설정 값만을 별도로 관리한 뒤, 동일한 인프라 설정을 일종의 템플릿처럼 재사용하자

    -  DRY (Don't Repeat Yourself)
        - 최대한 코드의 중복을 피하고, 코드 자체의 품질을 높이며, 재사용 할 수 있는 모듈형의 소스코드를 만드는 것이 최종 목표

- 수단
    -  테라폼 도입
    - 인프라 생성을 위한 설정 변경 및 프로비저닝 정의 등을 테라폼 코드로 작성
    - 소규모 조직에서 테라폼의 장단점을 완벽하게 파악하고 IaC를 도입하기 보다는, 그냥 빠르게 써보고 경험해서 도입하는 것이 옳은 선택일 수도 있다..

- 테라폼 한계점
    - 무료버전은 한 번 작성한 테라폼 코드를 GCP, AWS 와 같은 멀티 클라우드에 동일하게 적용하기 어려움
    -  테라폼에 숙련되지 않은 사람은 tf 파일을 작성하는 것만으로도 시간을 꽤 소비

<br>

## Reference
- [테라폼으로 글로벌 서비스 구성하기 - DRY 체험기](https://www.slideshare.net/sanggi/configuring-global-infrastructure-in-terraform-173174690)
- https://m.blog.naver.com/PostView.nhn?blogId=alice_k106&logNo=221584250113&proxyReferer=https:%2F%2Fwww.google.com%2F
