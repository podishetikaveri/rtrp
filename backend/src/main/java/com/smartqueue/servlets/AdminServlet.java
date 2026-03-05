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

@WebServlet(name = "AdminServlet", urlPatterns = { "/api/admin" })
public class AdminServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (Connection conn = Database.getConnection()) {
            String sql = "SELECT id, name, phone, status, is_vip, created_at " +
                    "FROM appointments ORDER BY id ASC";

            StringBuilder json = new StringBuilder();
            json.append("[");

            boolean first = true;
            try (PreparedStatement ps = conn.prepareStatement(sql);
                    ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    if (!first) {
                        json.append(",");
                    }
                    first = false;
                    json.append("{")
                            .append("\"id\":").append(rs.getInt("id")).append(",")
                            .append("\"name\":\"").append(escape(rs.getString("name"))).append("\",")
                            .append("\"phone\":\"").append(escape(rs.getString("phone"))).append("\",")
                            .append("\"status\":\"").append(rs.getString("status")).append("\",")
                            .append("\"isVip\":").append(rs.getBoolean("is_vip"))
                            .append("}");
                }
            }
            json.append("]");

            PrintWriter out = response.getWriter();
            out.write(json.toString());

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\":false,\"message\":\"Database error\"}");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        String idParam = request.getParameter("id");
        String action = request.getParameter("action");

        PrintWriter out = response.getWriter();

        if (idParam == null || action == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"success\":false,\"message\":\"Missing id or action\"}");
            return;
        }

        int id;
        try {
            id = Integer.parseInt(idParam);
        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"success\":false,\"message\":\"Invalid id\"}");
            return;
        }

        TokenStatus newStatus;
        try {
            newStatus = TokenStatus.valueOf(action.toUpperCase());
        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.write("{\"success\":false,\"message\":\"Invalid status\"}");
            return;
        }

        try (Connection conn = Database.getConnection()) {
            String sql = "UPDATE appointments SET status = ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, newStatus.name());
                ps.setInt(2, id);
                int updated = ps.executeUpdate();

                if (updated == 0) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    out.write("{\"success\":false,\"message\":\"Token not found\"}");
                } else {
                    out.write("{\"success\":true,\"message\":\"Status updated\"}");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.write("{\"success\":false,\"message\":\"Database error\"}");
        }
    }

    private String escape(String s) {
        if (s == null)
            return "";
        return s.replace("\"", "\\\"");
    }
}
