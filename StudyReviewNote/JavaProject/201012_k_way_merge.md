# 평점 분포 구하기

### 소스코드
- https://github.com/kwonsye/VAIVCompanyEducationProject/blob/master/JavaProject/src/main/java/RatingDistribution.java

<br>

### Rule
- 한 줄에 하나의 json 이 주어진다.(한줄에 아래의 정보가 들어감)
```json
{
    "user_id":"8842281e1d1347389f2ab933", 
    "book_id": "24375664",
    "review_id":"5cd416f3efc3f944fce40d5e", 
    "rating": 5, 
    "review_text": "Mind blowingly cool.", "date_added": "Fri Aug 25 13:55:02 -0700 2017",
    "date_updated": "Mon Oct 09 08:55:59 -0700 2017",
    "read_at": "Sat Oct 07 00:00:00 -0700 2017", 
    "started_at": "Sat Aug 26 00:00:00 -0700 2017",
    "n_votes": 16,
    "n_comments": 0
}
```  
- 당연히 book_id 별로 정렬되지 않았다.
- rating 의 범위는 0~5의 정수이다.
- 각 `book_id` 의 `rating`(0 ~ 5)별 리뷰수를 추출하여 출력해야한다.
- 출력 형식은 아래처럼 **book_id 순서로** 각 count가 정렬되어야한다.  

```
//book_id 평점0개수 평점1개수 평점2개수 평점3개수 평점4개수 평점5개수

1 99 17 35 196 723 2632
2 107 22 94 332 872 2423
3 251 69 122 596 1696 5724
4 2 0 0 6 13 26
...
```  

<br>

### 첫 접근

- [Word Count 문제](https://github.com/kwonsye/VAIVCompanyEducationProject/blob/master/JavaProject/src/WordCount.java)와 같이 똑같이 두 sub file을 만들고, 두 sub file을 merge 하는 방식으로 구현하면 되지 않을까?

<br>

### 피드백

- 물론 위와 같은 방식(==**2-way merge**)으로 해도 한정된 메모리문제를 해결하면서 잘 돌아간다..
- 하지만 그 과정에서 **너무 많은 파일을 읽고, 쓰고, 지운다.**
- **IO 작업은 시간을 가장 많이 잡아먹는 작업인데**, 이 시간을 최소화하는 방법은 없을까?
- **K-way merge** 방법을 이용해보자!

<br>

### K-way merge 가 뭔데?

- 기존에 **두 sub file을 비교해서 두 개씩 merge하는 방식**은 말그대로 `2-way merge`
- 산출되는 **모든 sub file을 한꺼번에 비교해서 한번에 merge하는 방식**이 `K-way merge`
- 그럼 어떻게 모든 sub file을 한꺼번에 비교할 수 있을까?
- sub file들의 data element 들을 heap 에 한꺼번에 넣고 빼면 알아서 힙정렬 되잖아!

<br>

### IDEA! ✨✨

1. json 데이터를 한줄씩 읽어서 Map에 <book_id : list(평점0개수, 평점1개수, 평점2개수..., 평점 5개수)> 를  저장한다.  

2. Map의 크기가 기준점(10만개)이 되면 Map의 데이터를 **book_id별로 오름차순 정렬한 상태로** sub file에 쓰고 `map.flush()`  

3. 모든 sub file 들이 만들어졌으면, 모든 sub file들의 첫 줄 data를 힙에 넣음 : `PriorityQueue.offer()`  

4. 이때 `PriorityQueue`는 `book_id` 오름차순으로 정렬순서를 compare  

5. 이미 sub file들이 소팅된 상태이므로 첫줄을 쫙 읽었다면 그 중에 하나가 최소 book_id를 가진 data이다.  

6. `PriorityQueue.poll()` 하면 **가장 작은 data 가 폴링**된다. -> final result file 첫줄에 쓴다.  

7. 다음으로 모든 sub file들의 두번째 줄을 읽는다.  

8. 두번째줄에서 읽은 data 들에 대해서 **3번~6번 반복**  

    8-1. data 들을 `PriorityQueue.offer()` 하는 과정에서 **만약 힙에 같은 book_id를 가진 data 가 있다면** 각 counting list 를 합쳐야한다!!  

    8-2. 나는 힙의 모든 data node 들을 돌면서 같은 book_id 가 있다면 `remove` 해주고, counting list 를 합쳐서, 다시 힙에 `offer` 해주는 방법으로 구현했다..(뭔가 조금 하드코딩인듯..? 하지만 `Comparable.compare` 쪽에서 어떻게 기존 노드를 업데이트하는지 알 수 없었음😂😂)  

9. 모든 sub file의 마지막 줄을 읽을때까지 반복한다.  

10. 힙이 `heapify` 되는 과정이 반복되면서 data 중 매번 가장 작은 node 를 쉽게 뽑아낼수있다.  

<br>

### 회고 및 배운점💪

- 리팩토링에 조금 더 신경 써봤다.  
    - 코드가 엄청 길어졌는데, 코드가 길어지더라도 **각 함수는 하나의 기능을 구현하도록 최대한 쪼개고, 그게 힘들다면 함수가 하는 기능들을 명확하게 이름에 명시하도록 하고, 포매팅과 같이 비지니스 로직과 관련없는 코드는 캡슐화하도록 했다..** 아직 정신없고 부족하지만 그래도 나름 만족..    

- 힙 구조가 생각보다 문제를 해결하는데 유용하게 활용될 수 있다는 것을 알았다.    

- **많은 양의 데이터를 캡슐화해서 알아서 소팅할수 있게 할 수 있는 문제가 있다면** 힙을 떠올려보자!  

- Stream 다루기 여전히 어렵다..🤔  

- 고민했던 부분  

    - 구현하는 과정에서 10만개의 데이터를 Map에 저장하지 말고 이거도 PriorityQueue 에 싹 저장해서 한꺼번에 polling 하는 방식으로 구현하면 더 쉬울것 같았는데, 그렇게 하지 않은 이유는  

    - 10만개의 keyset을 Arrays.sort로 **quick sort** 하느냐 vs 전체 데이터를 **insert 할때마다 힙소트로 heapify 하는게 낫느냐의** 성능 고민이 있었다.  

    - 찾아보니..힙소트랑 퀵소트 모두 평균은 O(nlogn) 이고, 퀵소트의 경우 최악이 O(n^2) 이었다.  

    - 그럼 힙소트가 낫겠거니 했는데  

    - 남자친구 얘길 들어보니 **자바7부터 Arrays.sort() 는 그냥 퀵소트가 아니고 dual pivot quicksort 라고 한다.**
        - **피봇이 두개라서 기존 퀵소트보다 피봇 위치가 더 최적화** 되었고,
        - **최악의 경우가 나올 확률이 거의 없으며**,
        - 데이터가 많을수록 데이터 개수에 비해 더 빠르고 효율적(?)인 소팅이 된다고 한다.
    
    - 그래서 기존 방식대로 Map에다 10만개씩 쓰고 key들을 퀵소팅하는 방식을 이용했다.

<br>

### Reference

- [Java의 파일에서 특정 줄 번호를 사용하여 특정 줄을 읽는 방법은 무엇입니까?](https://qastack.kr/programming/2312756/how-to-read-a-specific-line-using-the-specific-line-number-from-a-file-in-java)
- [Java에서 폴더의 모든 파일을 읽는 방법은 무엇입니까?](https://qastack.kr/programming/1844688/how-to-read-all-files-in-a-folder-from-java)
- [Set to Array in Java](https://www.geeksforgeeks.org/set-to-array-in-java/)
- [[JAVA] 우선순위(PriorityQueue) 큐](http://asuraiv.blogspot.com/2015/11/java-priorityqueue.html)
- JavaAPIDoc_Arrays.sort()