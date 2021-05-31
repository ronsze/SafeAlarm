# OverView
SafeAlarm은 미아 방지 또는 수색을 목적으로 하는 모바일 어플리케이션 입니다.
사용자를 보호자와 피보호자 둘로 나누고 GPS서비스를 통해 일정 시간마다 피보호자의 위치를 받아와
보호자의 지도 화면에서 계속해서 위치를 확인할 수 있습니다.

주요 기능은
1. 피보호자 위치 확인
2. 피보호자 시간 인증
3. 피보호자 동선 예측
4. 실종자 정보 공유(like 앰버 경보 시스템)
5. 피보호자가 보내는 위치 종단간 암호화(AES, 키 교환 - 디피헬만 알고리즘)

디피헬만 알고리즘 도중 중간자 공격 방지를 위해 전자서명 방식 사용.
클라이언트에서 RSA키 쌍 생성 후, 서버로 CSR전송(PKCS#10)
서버는 CSR을 받아 사용자의 인증서를 생성한다.(암호 없음)
이때 서버(CA, Certificate Authority)의 인증서는 자체발급한다.(신뢰할 수 없는 제3자)

SafeAlarm is a mobile application for the purpose of preventing missing children or searching.
Divide the user into two guardians and the guardian, and get the guardian's location every hour through GPS service.
You can continue to check your location on the guardian's map screen.

Key features are:
1. Identifying the location of the guardian
2. Protector Time Certification
3. Predict Unprotected Automation Lines
4. Sharing missing persons information (like Amber Alert System)
5. Location end-to-end encryption sent by the guardian (AES, key exchange - Defihelmann algorithm)

Electronic signature method is used to prevent man in the middle attack during the Diphelman algorithm.
Create RSA key pair from client and send CSR to server (PKCS#10)
The server receives the CSR and generates the user's certificate (no password).
The certificate of the server (CA) is self-issued (non-trusted third party).
