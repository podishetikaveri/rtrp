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

@WebServlet(name = "QueueStatusServlet", urlPatterns = { "/api/queue" })
public class QueueStatusServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
            HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        try (Connection conn = Database.getConnection()) {
            String currentSql = "SELECT id FROM appointments WHERE status = ? ORDER BY id ASC LIMIT 1";
            Integer currentServing = null;
            try (PreparedStatement ps = conn.prepareStatement(currentSql)) {
                ps.setString(1, TokenStatus.SERVING.name());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        currentServing = rs.getInt("id");
                    }
                }
            }

            String waitingSql = "SELECT id, name, phone, status, is_vip, created_at " +
                    "FROM appointments WHERE status = ? ORDER BY is_vip DESC, id ASC";

            StringBuilder json = new StringBuilder();
            json.append("{\"currentServing\":");
            json.append(currentServing == null ? "null" : currentServing);
            json.append(",\"waiting\":[");
            boolean first = true;

            try (PreparedStatement ps = conn.prepareStatement(waitingSql)) {
                ps.setString(1, TokenStatus.WAITING.name());
                try (ResultSet rs = ps.executeQuery()) {
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
            }

            json.append("]}");
            PrintWriter out = response.getWriter();
            out.write(json.toString());

        } catch (SQLException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write("{\"success\":false,\"message\":\"Database error\"}");
        }
    }

    private String escape(String s) {
        if (s == null)
            return "";
        return s.replace("\"", "\\\"");
    }
}
