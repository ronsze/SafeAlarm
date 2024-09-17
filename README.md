server  
https://github.com/ronsze/safeme-server

<H1>소개</H1>
SafeAlarm은 미아 방지를 위해 보호자와 피보호자 기기에 설치하여             

피보호자의 GPS위치를 실시간으로 감시하는 Application입니다.            
App에선 획득한 GPS위치를 토대로 몇 가지 기능을 통해 피보호자의 상태를 감시합니다.

<H1>기능</H1>
<H3>1. GPS위치 실시간 감시</H3>
피보호자는 수 초 간격으로 GPS위치를 보호자에게 전송합니다.                        

보호자는 App내의 지도화면에서 피보호자의 위치를 언제든 확인할 수 있습니다.                                
<img src="https://user-images.githubusercontent.com/45475151/148099463-42369f8a-4e1e-4691-b6d2-4af3a1694d68.png" width="300" height="450"/>
<H3>2. 피보호자 인증 예약</H3>
보호자는 자신이 원하는 시간을 설정할 수 있습니다.

해당 시간이 되면 피보호자는 인증 요청을 받습니다.                
인증 요청을 받은 뒤 일정 시간 내에 인증(패스워드 입력)을 수행하지 않으면 보호자에게 이를 알립니다.                                
<img src="https://user-images.githubusercontent.com/45475151/148099518-d15d6dad-d29f-48c0-9523-7b151807011e.png" width="300" height="450"/>
<img src="https://user-images.githubusercontent.com/45475151/148099536-15cc3416-bc70-4c0f-89fb-dfaee43ec0d6.png" width="300" height="450"/>

<H3>3. 피보호자 정보 공유</H3>
보호자는 사전에 피보호자의 정보(이름, 나이, 성별, 키 등)을 App에 저장합니다.

피보호자가 실종되었다고 판단했을 경우 피보호자의 정보를 App내에 게시할 수 있습니다.                   
다른 사용자들은 이를 확인하고 보호자에게 정보를 전달하여 실종된 피보호자의 동선을 파악하는데 도움을 줍니다.
<img src="https://user-images.githubusercontent.com/45475151/148100178-67c951aa-0aef-4ad2-bfbe-f29e0b381780.png" width="300" height="450"/>
<img src="https://user-images.githubusercontent.com/45475151/148100183-fa8ecde2-165c-4c9f-b108-46b5eddd0900.png" width="300" height="450"/>

<img src="https://user-images.githubusercontent.com/45475151/148100189-f354602a-d055-4761-a0f5-6a71c4097dfa.png" width="300" height="450"/><img src="https://user-images.githubusercontent.com/45475151/148100194-5dc21b71-6288-4737-9e25-9a9d369d85d1.png" width="300" height="450"/>

<H3>4. 피보호자 경로 일치 확인</H3>
지도를 정사각형의 격자 모형으로 된 Cell로 나눕니다.

피보호자가 지나간 Cell의 경로를 하루 단위로 보호자의 App에 저장합니다.

일정 기간동안 데이터를 수집한 뒤, 피보호자가 이동할 때 해당 경로가 수집된 데이터중에 있는지 확인합니다.                    
특별한 일이 없는 경우 사람은 정해진 생활패턴대로 생활하기 때문에 이동하는 경로도 맞춰서 일정해질 수 밖에 없습니다.                     
때문에 피보호자가 수집된 데이터(평소 이동 경로)와 다르게 이동한다면 이상이 생겼다고 판단하여 보호자에게 이를 알립니다.

<img src="https://user-images.githubusercontent.com/45475151/148100406-b1aa2fab-dc81-4e49-b127-33eb7f3f3796.png" width="300" height="300"/><img src="https://user-images.githubusercontent.com/45475151/148100414-300d3750-c980-47d7-87c8-2c84871d5ac8.png" width="300" height="300"/>
<img src="https://user-images.githubusercontent.com/45475151/148100428-76ae4656-5ade-45eb-b945-9f8da4423cdb.png" width="300" height="300"/><img src="https://user-images.githubusercontent.com/45475151/148100441-8293c3e4-4242-454e-b3d9-1f972aced346.png" width="300" height="300"/><img src="https://user-images.githubusercontent.com/45475151/148100448-57ba0aaa-2289-407e-b001-50bae352c09b.png" width="300" height="300"/>
1) 지도상의 큰 지역을 일정한 크기를 가진 여러 개의 작은 Cell로 나눕니다.
2) 피보호자가 움직인 이동 경로를 수집. (화살표대로 움직였다고 가정)
3) 일정 기간 수집 후 기능을 사용하고, 지도화면에 현재 Cell에서 다음으로 이동할 Cell을 표시합니다.
4) 피보호자가 데이터대로 움직였을 경우 계속해서 다음 이동경로 표시, 이를 반복합니다.
5) 경로와 다르게 이동하면 다시 일치하는 경로가 있는지 검토, 없을 경우 보호자에게 알립니다.
   
<img src="https://user-images.githubusercontent.com/45475151/148101219-755eef6c-0d47-4156-af23-bb07e9f3d2fe.png" width="300" height="450"/>


<H1>보안 문제</H1>
위치 데이터는 위도와 경도 데이터로 전송됩니다.

그런데 이 데이터를 제3자가 가로채면 악용될 우려가 있습니다.

때문에 DH키 교환 방식과 AES256암호화 방식을 통해 전송되는 좌표 데이터를 암호화하여 전송합니다.                            
보호자는 암호화된 데이터를 받아서 자신의 Key로 복호화하여 활용합니다.                     
<img src="https://user-images.githubusercontent.com/45475151/148100658-57243dde-87a8-4cf8-ba17-b72ec764f105.png" width="900" height="600"/>


