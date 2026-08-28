import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

@WebServlet(name = "Login", urlPatterns = {"/login"})
public class Login extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        processRequest(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {

        processRequest(request, response);
    }

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            String username = request.getParameter("username");
            String password = request.getParameter("password");

            Class.forName("com.mysql.jdbc.Driver");

            String dbPassword = System.getenv("DB_PASSWORD");

            String sql = "SELECT * FROM users WHERE username=? AND password=?";

            try (Connection conn = DriverManager.getConnection(
                    "jdbc:mysql://localhost:3306/webapp",
                    "root",
                    dbPassword
            );
                 PreparedStatement st = conn.prepareStatement(sql)) {

                st.setString(1, username);
                st.setString(2, password);

                try (ResultSet rs = st.executeQuery()) {

                    if (rs.next()) {
                        HttpSession session = request.getSession();
                        session.setAttribute("username", username);
                        response.sendRedirect("search.jsp");
                    } else {
                        out.println("Invalid username and/or password");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            out.close();
        }
    }
}