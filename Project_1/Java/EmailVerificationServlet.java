package model;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.json.JSONObject;

/**
 * Servlet implementation class EmailVerificationServlet
 */
@WebServlet("/sendEmailVerification")
public class EmailVerificationServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private static final String FROM_EMAIL = "hamjang99@gmail.com";
	private static final String FROM_PASSWORD = "변경";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public EmailVerificationServlet() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
		
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		// TODO Auto-generated method stub
        boolean emailSent = false;  // 이메일 전송 성공 여부를 추적할 변수
        String action = request.getParameter("action");
		String getParam_email = request.getParameter("email");
		UserDao userDao = new UserDao();
//		System.out.println("action: " + action); // 디버깅
		
		if("signup".equals(action)) {
			String verificationCode = generateVerificationCode();

			// 세션에 인증번호 저장
			HttpSession session = request.getSession();
			session.setAttribute("verificationCode", verificationCode);

			// 이메일로 인증번호 전송
			emailSent = sendVerificationEmail(getParam_email, verificationCode);
			
			// JSON 응답 생성
	        JSONObject jsonResponse = new JSONObject();
	        if (emailSent) {
	            jsonResponse.put("success", true);
	        } else {
	            jsonResponse.put("success", false);
	            jsonResponse.put("error", "Failed to send email.");
	        }

	        // JSON 응답 전송
	        response.setContentType("application/json");
	        response.setCharacterEncoding("UTF-8");
	        PrintWriter out = response.getWriter();
	        out.print(jsonResponse.toString());
	        out.flush();
		} else if("findId".equals(action)) {
			String name = request.getParameter("name");
//			System.out.println("name: " + name);
			
			ArrayList<User> equal_name_list = userDao.getUserByName(name); // DB에서 유저정보조회
			
			ArrayList<String> matchingUserIDs = new ArrayList<>();
			
			for(User user: equal_name_list) {
//				System.out.println(user);
				if(user.getEmail().equals(getParam_email)) {
					matchingUserIDs.add(user.getId());
				}
			}
			if(!matchingUserIDs.isEmpty()) {
				String verificationCode = generateVerificationCode();

				// 세션에 인증번호 저장
				HttpSession session = request.getSession();
				session.setAttribute("verificationCode", verificationCode);

				// 이메일로 인증번호 전송
				emailSent = sendVerificationEmail(getParam_email, verificationCode);
				
				// JSON 응답 생성
		        JSONObject jsonResponse = new JSONObject();
		        if (emailSent) {
		            jsonResponse.put("success", true);
		        } else {
		            jsonResponse.put("success", false);
		            jsonResponse.put("error", "Failed to send email.");
		        }

		        // JSON 응답 전송
		        response.setContentType("application/json");
		        response.setCharacterEncoding("UTF-8");
		        PrintWriter out = response.getWriter();
		        out.print(jsonResponse.toString());
		        out.flush();
			}else {
				JSONObject jsonResponse = new JSONObject();
			    jsonResponse.put("success", false);
			    jsonResponse.put("error", "No matching user found with the given email.");
			    
			    // JSON 응답 전송
			    response.setContentType("application/json");
			    response.setCharacterEncoding("UTF-8");
			    PrintWriter out = response.getWriter();
			    out.print(jsonResponse.toString());
			    out.flush();
			}
		}
		

	}
	
	private String generateVerificationCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000); // 6자리 인증번호 생성
        return String.valueOf(code);
    }

    private boolean sendVerificationEmail(String recipientEmail, String verificationCode) {
        String host = "smtp.gmail.com";  // SMTP 서버 주소
        String port = "587";  // SMTP 포트
        boolean check = false;

        Properties properties = new Properties();
        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", port);
        properties.put("mail.smtp.auth", "true");
        properties.put("mail.smtp.starttls.enable", "true");

        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(FROM_EMAIL, FROM_PASSWORD);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(FROM_EMAIL));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipientEmail));
            message.setSubject("Your Verification Code");
            message.setText("Your verification code is: " + verificationCode);

            Transport.send(message);
            System.out.println("Verification email sent to " + recipientEmail); //디버깅용
            check = true;
            
            

        } catch (MessagingException e) {
            e.printStackTrace();
            check = false;
        }
        
        return check;
        
    }

}

