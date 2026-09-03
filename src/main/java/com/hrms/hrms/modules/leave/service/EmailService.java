//package com.hrms.hrms.modules.leave.service;
//
//import com.hrms.hrms.modules.leave.entity.Leave;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.mail.SimpleMailMessage;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.stereotype.Service;
//
//@Service
//public class EmailService {
//    private final JavaMailSender mailSender;
//    @Value("${app.mail.from}")
//    private String fromEmail;
//
//    // CONSTRUCTOR
//    public EmailService(JavaMailSender mailSender) {
//        this.mailSender = mailSender;
//    }
//
//    // SEND LEAVE REQUEST EMAIL TO MANAGER
//    public void sendLeaveRequestEmail(Leave leave) {
//        String managerEmail = leave.getRequestedTo();
//        if (managerEmail == null || managerEmail.isBlank()) {
//            System.err.println("Cannot send leave request email: manager email is missing");
//            return;
//        }
//
//        String employeeName = leave.getEmployee().getFirstName()
//                        + " "
//                        + leave.getEmployee().getLastName();
//
//        String employeeEmail =
//                leave.getEmployee()
//                        .getUser()
//                        .getEmail();
//
//        String subject = "New Leave Request from " + employeeName;
//        String body = "Hello,\n\n"
//                        + "A new leave request has been submitted.\n\n"
//                        + "Employee Name: " + employeeName + "\n"
//                        + "Employee Email: " + employeeEmail + "\n"
//                        + "Leave Type: " + leave.getLeaveType() + "\n"
//                        + "From: " + leave.getFromDate() + "\n"
//                        + "To: " + leave.getToDate() + "\n"
//                        + "Reason: " + leave.getReason() + "\n"
//                        + "Status: " + leave.getStatus() + "\n\n"
//
//                        + "Please review this leave request in the HRMS portal.\n\n"
//
//                        + "Regards,\n"
//                        + "HRMS System";
//
//        sendEmail(managerEmail, subject, body);
//    }
//
//
//    // SEND APPROVED EMAIL TO EMPLOYEE
//    public void sendLeaveApprovedEmail(Leave leave) {
//        String employeeEmail =
//                leave.getEmployee()
//                        .getUser()
//                        .getEmail();
//
//        String employeeName = leave.getEmployee().getFirstName()
//                        + " "
//                        + leave.getEmployee().getLastName();
//
//        String subject = "Leave Request Approved";
//        String body = "Hello " + employeeName + ",\n\n"
//
//                        + "Your leave request has been approved.\n\n"
//
//                        + "Leave Type: " + leave.getLeaveType() + "\n"
//                        + "From: " + leave.getFromDate() + "\n"
//                        + "To: " + leave.getToDate() + "\n"
//                        + "Reason: " + leave.getReason() + "\n"
//                        + "Status: APPROVED\n\n"
//
//                        + "Regards,\n"
//                        + "HRMS System";
//
//        sendEmail(employeeEmail, subject, body);
//    }
//
//    // SEND REJECTED EMAIL TO EMPLOYEE
//    public void sendLeaveRejectedEmail(Leave leave) {
//        String employeeEmail = leave.getEmployee()
//                        .getUser()
//                        .getEmail();
//
//        String employeeName = leave.getEmployee().getFirstName()
//                        + " "
//                        + leave.getEmployee().getLastName();
//
//        String subject = "Leave Request Rejected";
//        String body = "Hello " + employeeName + ",\n\n"
//
//                        + "Your leave request has been rejected.\n\n"
//
//                        + "Leave Type: " + leave.getLeaveType() + "\n"
//                        + "From: " + leave.getFromDate() + "\n"
//                        + "To: " + leave.getToDate() + "\n"
//                        + "Reason: " + leave.getReason() + "\n"
//                        + "Status: REJECTED\n\n"
//
//                        + "Please contact your reporting manager if you need further information.\n\n"
//
//                        + "Regards,\n"
//                        + "HRMS System";
//
//        sendEmail(employeeEmail, subject, body);
//    }
//
//    // COMMON SEND EMAIL METHOD
//    private void sendEmail(String to, String subject, String body) {
//        try {
//            SimpleMailMessage message = new SimpleMailMessage();
//            message.setFrom(fromEmail);
//            message.setTo(to);
//            message.setSubject(subject);
//            message.setText(body);
//            mailSender.send(message);
//
//            System.out.println("Email sent successfully to: " + to);
//        } catch (Exception exception) {
//
//            /*
//             * Email failure should not break
//             * the leave operation.
//             */
//            System.err.println("Failed to send email to "
//                            + to
//                            + ": "
//                            + exception.getMessage()
//            );
//        }
//    }
//}