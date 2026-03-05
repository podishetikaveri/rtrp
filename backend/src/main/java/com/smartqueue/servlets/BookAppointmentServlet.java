package com.smartqueue.servlets;

import com.smartqueue.db.Database;
import com.smartqueue.model.TokenStatus;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@WebServlet(name = "BookAppointmentServlet", urlPatterns = { "/api/book" })
public class BookAppointmentServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String name = request.getParameter("name");
        String phone = request.getParameter("phone");
        String isVipStr = request.getParameter("isVip");
        boolean isVip = "true".equalsIgnoreCase(isVipStr);

        PrintWriter out = response.getWriter();

        if (name == null || name.isBlank() || phone == null || phone.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"success\":false,\"message\":\"Name and phone are required\"}");
            return;
        }

        try (Connection conn = Database.getConnection()) {
            String insertSql = "INSERT INTO appointments (name, phone, status, is_vip) VALUES (?, ?, ?, ?) RETURNING id";
            int tokenId = -1;

            try (PreparedStatement ps = conn.prepareStatement(insertSql)) {
                ps.setString(1, name);
                ps.setString(2, phone);
                ps.setString(3, TokenStatus.WAITING.name());
                ps.setBoolean(4, isVip);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        tokenId = rs.getInt("id");
                    }
                }
            }

            if (tokenId == -1) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                out.write("{\"success\":false,\"message\":\"Failed to generate token\"}");
                return;
            }

            // Position logic: Count how many waiting users are ahead.
            // If this user IS VIP: count other VIPs who arrived before this user.
            // If this user is NOT VIP: count all VIPs + non-VIPs who arrived before this
            // user.
            String positionSql = "SELECT COUNT(*) FROM appointments WHERE status = ? " +
                    "AND ( " +
                    "  (is_vip = true) " + // All VIPs are always ahead of non-VIPs
                    "  OR " +
                    "  (is_vip = ? AND id <= ?) " + // Same tier, earlier or same ID
                    ")";
            if (isVip) {
                // If the current user is VIP, they only wait behind OTHER VIPs who came earlier
                positionSql = "SELECT COUNT(*) FROM appointments WHERE status = ? AND is_vip = true AND id <= ?";
            }

            int position = 0;
            try (PreparedStatement psPos = conn.prepareStatement(positionSql)) {
                psPos.setString(1, TokenStatus.WAITING.name());
                if (isVip) {
                    psPos.setInt(2, tokenId);
                } else {
                    psPos.setBoolean(2, false);
                    psPos.setInt(3, tokenId);
                }
                try (ResultSet rsPos = psPos.executeQuery()) {
                    if (rsPos.next()) {
                        position = rsPos.getInt(1);
                    }
                }
            }

            out.write("{\"success\":true," +
                    "\"token\":" + tokenId + "," +
                    "\"position\":" + position + "," +
                    "\"message\":\"Appointment booked successfully\"}");

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"success\":false,\"message\":\"Database error\"}");
        }
    }
}
