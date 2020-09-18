# [Section 1-1] Hadoop에 대한 소개와 기본 설치

## 하둡🐘

- 컴퓨터 클러스터에서 **방대한 양의 데이터 세트를 변환하고 분석**하기위한 강력한 도구
- 수백개의 기술들이 모여있는 생태계 -> **서로 어떤 관계와 역할들이 fit 되어 있는지** 아는게 중요!
- 각각의 기술은 관계형/비관계형 DB에서 데이터를 가져와서 쿼리로 분석함

<br>

## 하둡 Eco system 을 본격적으로 공부하기 전 준비운동하기🤸‍♂️

- <del>[Virtualbox](https://www.virtualbox.org/wiki/Downloads) 가상머신 </del>
- <del>[Hortonworks HDP Sandbox 2.6.5 설치](https://www.cloudera.com/downloads/hortonworks-sandbox/hdp.html)</del>
    - sandbox란?
        - 외부 프로그램이나 파일을 보호된 영역인 **가상화 내부에서 실행**시킴으로써 내부 시스템에 악영향을 주는 것을 방지하는 기술
        
    - Hortonworks나 Cloudera 같은 빅데이터 유명 벤더사들은 자사의 **하둡 환경에 대한 패키지를 가상환경에 올려놓고  테스트** 할 수 있도록 만들어놓음.
        
- => **회사의 테스트용 서버(Linux)에 ssh로 접속해서 진행하기로!**

- [Grouplens > older datasets](https://grouplens.org/datasets/movielens/) 에서 데이터set을 다운로드 받을 수 있음

    - `wget http://files.grouplens.org/datasets/movielens/ml-100k.zip`

    - `unzip ml-100k`

- Ambari
        
    - **웹 UI를 제공해서 Hadoop eco 설치, 설정배포, 모니터링, Alert 등의 운영 편의성**을 제공하고  Hadoop 클러스터의 관리를 쉽게 하게 해주는 툴

    - Ambari로 기존에 구축되어있는 하둡 클러스터를 관리할 수는 없다.

    - 강의 예제에서는 Ambari를 통해 웹UI로 테스트 데이터셋을 `hive` 에 insert 하지만, 나는 이미 구축된 하둡 클러스터에서 진행해야하기 때문에 직접 `hive cli`에서 데이터셋을 insert 해야했다.

- `hive cli` 에서 `hiveQL`을 이용해 데이터셋 insert 하기
        
    1. `hive` 명령어로 hive cli 접속
    2. insert할 데이터셋 스키마에 맞는 hive table을 만든다.
            
        - insert할 `ml-100k/u.data` 데이터셋 형태
        ```
        //순서대로 user id, movie id, 별점, 별점을 남긴 timestamp

        196	242	3	881250949
        186	302	3	891717742
        ...
        ...
        ```

        - 테이블 만들기
            
        ```sql
        CREATE TABLE ratings (user_id int, movie_id int, raing int, rating_time int)
        ROW FORMAT DELIMITED FIELDS TERMINATED BY '\t' -- 데이터셋이 한 row안에서 tab으로 구분되어 있음
        LINES TERMINATED BY '\n'; --데이터셋의 라인은 개행으로 구분되어 있음
        ```
        
    3. 생성한 `ratings` 테이블에 데이터셋 파일 insert 하기
        
    ```sql
    LOAD DATA LOCAL INPATH '/home/trendmap/ml-100k/u.data' -- path에 있는 파일을 로드한다.
    OVERWRITE INTO TABLE ratings;
    ```

    - 🐞 Permission denied

        - hdfs 라는 유저(`-u hdfs`) 로 hdfs에 user를 만드는 명령어를 실행

        ```
        sudo -u hdfs hdfs dfs -mkdir /user/trendmap 
        ```

        - 유저를 확인
        ```
        hdfs dfs -ls /user
        //결과
        drwxr-xr-x   - hdfs   supergroup          0 2020-09-09 15:00 /user/trendmap
        ```
        - 유저의 소유자를 `hdfs:supergroup` 에서 `trendmap`으로 변경

        ```
        sudo -u hdfs hadoop fs -chown trendmap:trendmap /user/trendmap
        ```

<br>




## Reference
- https://brownbears.tistory.com/78
- https://datacookbook.kr/55
- https://datacookbook.kr/32
- https://bigmark.tistory.com/39
- https://byline.network/2016/01/1-47/
- https://stackoverflow.com/questions/50101717/hive-insert-permission-denied-user-root-access-write