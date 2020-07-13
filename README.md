# OnePoint
![onepoint](https://user-images.githubusercontent.com/33366501/87279771-51cf6280-c52c-11ea-8e6a-170bbf44bfa9.png)
![MSA설계](https://user-images.githubusercontent.com/33366501/87279746-4714cd80-c52c-11ea-9b09-1839421ff584.PNG)

##서비스 개요 
*계약을 맺은 가맹점과 고객간의 포인트를 적립/사용 하는 회원포인트 시스템?? 

##Micro Serive 소개 
* Billing
	* 가맹점 별 월 단위 정산을 도와주는 시스템 
	* 주요기능 : 정산년월, 정산가맹점 번호를 입력받아 해당 정산년월의 정산금액을 반환  
* BillingAmountView
	*  정산을 위해 거래 발생 시, 실패하지 않은 사용 거래에 대해서만 Data를 쌓아 놓은 View 
* Point
	* 회원 별 보유포인트 금액 을 관리하는 시스템 
	* Deal 시스템에서 성공 거래가 일어날 시, 보유포인트를 증가/감소 시켜 줌 
* Member
	* 회원 정보 관리 시스템으로 보유 포인트에 대한 정보가 아닌 전화번호, 이메일주소 등 변경이 잦지 않은 회원정보를 관리하는 시스템
	* 회원가입 시 Point 시스템에 회원-Point 생성을 요청 함 
	* 회원 탈퇴 시 Point 시스템에 회원-Point 삭제를 요청 함 
* Deal 
	* 거래관리 시스템. 성공/실패 거래를 모두 관리하는 시스템. 
	* 적립거래 발생 시 Point 시스템에 적립된 금액 만큼에 대한 보유포인트 증가를 요청함 
	* 사용거래 발생 시 Point 시스템과 동기 통신을 통해 사용가능한 포인트인지 확인하고, 거래 성공 시 Point 시스템에 보유포인트 차감을 요청함  
	* 사용거래 발생 시 거래가 성공인 case에 대해서만 BillingAmountView 시스템에 거래를 누적시킴

##테스트 시나리오 
1. memberId 0001 사용자가 적립거래를 10000원 발생 시킴 , 적립률(0.01)은 고정이고 거래금액*적립률 만큼의 Point가 쌓임 
	요청 | Point |status |dealDate
	http POST http://localhost:8085/deals memberId=0001 merchantId=20 dealAmount=100000 type="save"          |  1000 원  | | 
	http GET http://localhost:8081/points/0001            | 1000원 | |
    	http GET http://localhost:8085/deals/1                   |  1000원  | "success"  |  거래 발생시간

2. memberId 0001 사용자의 point 사용거래 . 포인트 사용 거래 시 거래금액은 상관이 없음. 
	
	http POST http://localhost:8085/deals memberId=0001 merchantId=20 dealAmount=10000 type="use"  point=" 100"
	http GET http://localhost:8081/points/0001  		point 900원
    	http GET http://localhost:8085/deals/2 			point 100원 
	http GET http://localhost:8083/billingAmountViews/2 (Id는 deal을 따라감)

3. memberId 0001 사용자의 point 사용거래 (포인트 부족)
	http POST http://localhost:8085/deals memberId=0001 merchantId=20 dealAmount=10000 type="use"  point="10000"
	http GET http://localhost:8081/points/0001 		point 900원
    	http GET http://localhost:8085/deals/3 			point 10000원 status =fail
	http GET http://localhost:8083/billingAmountViews/3 (I실패했으므로 안나옴)


4. point 증가 테스트 : memberId 0001 의 포인트를 증가시킴 
	http POST http://localhost:8085/deals memberId=0001 merchantId=20 dealAmount=100000 type="save" 
	http GET http://localhost:8081/points/0001 		point 1900원
    	http GET http://localhost:8085/deals/4 			point 1000원


5. memberId 0002 사용자의 Point 적립 거래 발생 후 사용거래 발생 ( 정산기능 테스트를 위해) 
	http POST http://localhost:8085/deals memberId=0002 merchantId=20 dealAmount=100000 type="save" 
	http POST http://localhost:8085/deals memberId=0002 merchantId=20 dealAmount=10000 type="use"  point=" 200"
	http POST http://localhost:8085/deals memberId=0002 merchantId=20 dealAmount=10000 type="use"  point=" 200"
	http GET http://localhost:8085/deals/5
	http GET http://localhost:8085/deals/6
        http GET http://localhost:8081/points/0002 		point 600원


6. 가맹점 20 번의 7월 정산요청 
	http POST http://localhost:8084/billings mercharntId=20 billingMonth="202007"
	test 2, 5 의 사용거래 (100원 , 200원, 200원 ) * 0.98 만큼의 금액이 정산금액으로 책정됨  : 490 원 
