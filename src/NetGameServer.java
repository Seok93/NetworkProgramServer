import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

public class NetGameServer extends JFrame {
	private JPanel contentPane; // 새로 만든 패널을 contentPane로 이용하고자 생성
	private JTextField textField; // 사용할 PORT번호 입력
	private JButton StartBtn; // 서버를 실행시킨 버튼
	JTextArea textArea; // 클라이언트 및 서버 메시지 출력

	private ServerSocket socket; // 서버소켓
	private Socket soc; // 연결소켓
	private int Port; // 포트번호

	// 관리용 변수들
	private Vector vc = new Vector(); // 연결된 사용자를 저장할 벡터, 가변길이 배열
	//private final List<GameInfo> gameRoom = new ArrayList<GameInfo>(); //게임방 관리를 위한 변수
	private final String ID[] = { "Guest1", "Guest2", "Guest3" };
	private final String PSWD[] = { "1111", "2222", "3333" };

	
	public static void main(String[] args) {
		NetGameServer frame = new NetGameServer(); // 프레임 객체 생성
		frame.setVisible(true);
	}

	public NetGameServer() {
		init(); // 프레임 초기화 목적 메소드
	}

	private void init() { // GUI를 구성하는 메소드
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 280, 400); // 위치와 크기 설정 (x, y, w, h)

		// JPanel 설정 - contentPane을 대체해서 사용할 계획
		contentPane = new JPanel(); // contentPane을 위한 패널 생성
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5)); // JPanel의 내부 공백 경계설정
		setContentPane(contentPane); // 기존의 contentPane 교체
		contentPane.setLayout(null); // 별다른 배치관리자 없음

		JScrollPane js = new JScrollPane();

		// TextArea 설정 - 스크롤바 설정
		textArea = new JTextArea();
		textArea.setColumns(20);
		textArea.setRows(5);
		js.setBounds(0, 0, 264, 254);
		contentPane.add(js);
		js.setViewportView(textArea); // 스크롤 패널 위에 올림
		textArea.setEditable(false); // textArea를 사용자가 수정 못하게끔 막는다.

		// TextField 설정 - port번호를 입력 받기 위해 설정
		textField = new JTextField();
		textField.setBounds(98, 264, 154, 37);
		contentPane.add(textField);
		textField.setColumns(10);

		// JLabel 설정
		JLabel lblNewLabel = new JLabel("Port Number");
		lblNewLabel.setBounds(12, 264, 98, 37);
		contentPane.add(lblNewLabel);

		// JButton 설정
		StartBtn = new JButton("서버 실행");
		Myaction action = new Myaction(); // 버튼 액션리스너 객체 생성
		StartBtn.addActionListener(action); // 내부클래스로 액션 리스너를 상속받은 클래스로
		textField.addActionListener(action);
		StartBtn.setBounds(0, 325, 264, 37);
		contentPane.add(StartBtn);
	}

	// 서버 실행 전 버튼을 위한 이벤트
	class Myaction implements ActionListener // 내부클래스로 액션 이벤트 처리 클래스
	{
		@Override
		public void actionPerformed(ActionEvent e) {

			// 액션 이벤트가 sendBtn일때 또는 textField 에세 Enter key 치면
			if (e.getSource() == StartBtn || e.getSource() == textField) {
				if (textField.getText().equals("") || textField.getText().length() == 0)// textField에 값이 들어있지 않을때
				{
					textField.setText("포트번호를 입력해주세요");
					textField.requestFocus(); // 포커스를 다시 textField에 넣어준다
				} else {
					try {
						Port = Integer.parseInt(textField.getText()); // 숫자로 입력하지 않으면 에러 발생 포트를 열수 없다.
						// 제대로 된 포트 번호 입력시 변수 저장
						server_StartBtn(); // 사용자가 제대로된 포트번호를 넣었을때 서버 실행을위헤 메소드 호출
					} catch (Exception er) {
						// 사용자가 숫자로 입력하지 않았을시에는 재입력을 요구한다
						textField.setText("숫자로 입력해주세요");
						textField.requestFocus(); // 포커스를 다시 textField에 넣어준다
					}
				} // else 문 끝
			}

		}

	}

	// 서버 실행 버튼 클릭 후 제대로 동작시 실행되는 메소드, 소켓 생성 목적
	private void server_StartBtn() {
		try {
			socket = new ServerSocket(Port); // 서버가 포트 여는부분, port번호만 주면 알아서 열어줌
			StartBtn.setText("서버실행중");
			StartBtn.setEnabled(false); // 서버를 더이상 실행시키지 못 하게 막는다
			textField.setEnabled(false); // 더이상 포트번호 수정못 하게 막는다

			if (socket != null) // socket 이 정상적으로 열렸을때
			{
				Connection();
			}

		} catch (IOException e) {
			textArea.append("소켓이 이미 사용중입니다...\n");

		}

	}

	// 서버 소켓이 제대로 생성되면 실행되는 메소드,
	private void Connection() {
		Thread th = new Thread(new Runnable() { // 사용자 접속을 받을 스레드
			@Override
			public void run() {
				while (true) { // 사용자 접속을 계속해서 받기 위해 while문
					try {
						textArea.append("사용자 접속 대기중...\n");
						soc = socket.accept(); // accept가 일어나기 전까지는 무한 대기중
						textArea.append("사용자 접속!!\n");
						UserInfo user = new UserInfo(soc, vc); // 연결된 소켓 정보는 금방 사라지므로, user 클래스 형태로 객체 생성
						// 매개변수로 현재 연결된 소켓과, 벡터를 담아둔다
						vc.add(user); // 해당 벡터에 사용자 객체를 추가
						user.start(); // 만든 객체의 스레드 실행
					} catch (IOException e) {
						textArea.append("!!!! accept 에러 발생... !!!!\n");
					}
				}
			}
		});
		th.start(); // 클라이언트 요청을 받는 쓰레드 실행... 사용자의 요청을 계속 확인한다.
	}

	// ==========================================================================
	class UserInfo extends Thread {
		// 데이터 송수신용 스트림
		private InputStream is;
		private OutputStream os;
		private DataInputStream dis;
		private DataOutputStream dos;

		// 데이터 유지용(소켓, 다른 사용자 접근을 위함)
		private Socket user_socket;
		private Vector user_vc;
		// private String Nickname = "";

		// private boolean existenceID = false;

		public UserInfo(Socket soc, Vector vc) // 생성자메소드
		{
			// 매개변수로 넘어온 자료 저장
			this.user_socket = soc; // 유저 소켓 저장
			this.user_vc = vc; // 다른 유저들에게 접근하기 위해 필요한 벡터 레퍼런스
			User_network();
		}

		public void User_network() {
			try {
				// 스트림 초기화
				is = user_socket.getInputStream();
				dis = new DataInputStream(is);
				os = user_socket.getOutputStream();
				dos = new DataOutputStream(os);
				// Nickname = dis.readUTF(); // 사용자의 닉네임 받는부분

				// byte[] b=new byte[128];
				// dis.read(b);
				// String Nickname = new String(b); //닉네임을 임시로 받음
				// Nickname = Nickname.trim(); // 받은 닉네임을 가공하여 최종본 저장
				// textArea.append("ID :" + Nickname + " 접속\n");
				textArea.append((vc.size() + 1) + "번째 사용자 접속\n");
				textArea.setCaretPosition(textArea.getText().length());

				// send_Message(Nickname + "님 환영합니다."); // 연결된 사용자에게 정상접속을 알림
				send_Message((vc.size() + 1) + "번째 사용자님 환영합니다.");
			} catch (Exception e) {
				textArea.append("스트림 셋팅 에러\n");
				textArea.setCaretPosition(textArea.getText().length());
			}
		}

		// 유저가 보낸 메시지 감독용 메소드, 사용자가 보낸 메세지 검사는 여기!
		public void InMessage(String str) {
			textArea.append("사용자로부터 들어온 메세지 : " + str + "\n");
			// textArea.append(str + "\n");
			textArea.setCaretPosition(textArea.getText().length());

			// 사용자 메세지 처리
			if (str.charAt(0) == '/') {
				int count = 0;
				StringTokenizer token = new StringTokenizer(str, " /");
				String strArr[] = new String[token.countTokens()];
				// System.out.println(token.countTokens());
				// System.out.println(strArr.length);
				while (token.hasMoreTokens()) {
					strArr[count++] = token.nextToken();
				}

				if (strArr[0].equals("confirmID")) {
					// textArea.append("아이디 검사중");
					// textArea.setCaretPosition(textArea.getText().length());
					// textArea.append(Boolean.toString(confirmUser(strArr[1], strArr[2])));
					// textArea.setCaretPosition(textArea.getText().length());

					if (strArr.length == 3) {
						if (confirmUser(strArr[1], strArr[2]))
							send_Message("true");
						else
							send_Message("false");
					}
				} else if (strArr[0].equals("makeGame")) {
					
				} else {

					broad_cast(str); // 모든 사용자에게 전송
				}
			} else {
				broad_cast(str); // 모든 사용자에게 전송
			}
		}

		// 모든 유저들에게 메시지를 전달하기 위한 메소드
		public void broad_cast(String str) {
			for (int i = 0; i < user_vc.size(); i++) {
				UserInfo imsi = (UserInfo) user_vc.elementAt(i);
				imsi.send_Message(str);
			}
		}

		// 실제로 데이터를 전송하는 메소드
		public void send_Message(String str) {
			try {
				// dos.writeUTF(str);
				byte[] bb;
				bb = str.getBytes();
				dos.write(bb); // .writeUTF(str);
			} catch (IOException e) {
				textArea.append("메시지 송신 에러 발생\n");
				textArea.setCaretPosition(textArea.getText().length());
			}
		}

		// 입력받은 아이디, 비번 확인 메소드
		public Boolean confirmUser(String id, String pswd) {
			for (int i = 0; i < ID.length; i++) {
				if (ID[i].equals(id) && PSWD[i].equals(pswd)) {
					return true;
				}
			}

			return false;
		}

		public void run() // 스레드 정의
		{
			// 스레드 실행 내용, 새로운 내용을 여기다 추가하면 된다. 데이터 점검 등
			while (true) {
				try {
					// 사용자에게 받는 메세지
					byte[] b = new byte[128];
					dis.read(b);
					String msg = new String(b);
					msg = msg.trim();
					// String msg = dis.readUTF();

					InMessage(msg);
				} catch (IOException e) {
					try {
						dos.close();
						dis.close();
						user_socket.close();
						vc.removeElement(this); // 에러가난 현재 객체를 벡터에서 지운다
						textArea.append(vc.size() + " : 현재 벡터에 담겨진 사용자 수\n");
						textArea.append("사용자 접속 끊어짐 자원 반납\n");
						textArea.setCaretPosition(textArea.getText().length());

						break;
					} catch (Exception ee) {

					} // catch문 끝
				} // 바깥 catch문끝
			}
		}// run메소드 끝
	} // 내부 userinfo클래스끝
}

// 17-12-08 이후 추가할 할목
// 로그인시 아이디 사용중으로 바꾸기, 누가 또 접속하면 안되니까.
// 로그아웃이 아이디 사용중 해제, 클라이언트에 등록된 id도 삭제